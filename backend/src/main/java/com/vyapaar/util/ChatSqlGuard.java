package com.vyapaar.util;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks SQL written by the AI model before it is run.
 *
 * This is the SECOND line of defence, not the first. The real protection is the read-only MySQL
 * account in database/chat_readonly_user.sql: code checks can be bypassed, database privileges
 * cannot. This guard exists to reject unsafe SQL early, give a clear reason, and let the service
 * ask the model to try again.
 *
 * Rules enforced: one statement only, must start with SELECT or WITH, no comments, no stacked
 * statements, no backslashes or backticks, no dangerous keywords or functions, and only the eight
 * allowed tables and views. String literals are blanked before the keyword checks, so a query
 * like LIKE '%update%' still passes.
 */
public final class ChatSqlGuard {

    public static final Set<String> ALLOWED_TABLES = Set.of(
            "customers", "suppliers", "products", "purchases", "sales", "payments",
            "v_customer_balance", "v_supplier_balance");

    private static final Pattern LITERAL =
            Pattern.compile("'(?:[^']|'')*'|\"(?:[^\"]|\"\")*\"");

    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|truncate|replace|grant|revoke|call|execute|prepare"
                    + "|handler|load|load_file|into|outfile|dumpfile|sleep|benchmark|get_lock|lock|unlock|rename|set"
                    + "|information_schema|performance_schema|mysql|sys)\\b");

    private static final Pattern TABLE_REF =
            Pattern.compile("\\b(?:from|join)\\s+([a-z_][a-z0-9_.]*)");

    private static final Pattern CTE_NAME =
            Pattern.compile("(?:\\bwith|,)\\s*([a-z_][a-z0-9_]*)\\s+as\\s*\\(");

    private ChatSqlGuard() {
    }

    /** Returns the cleaned SQL, or throws IllegalArgumentException explaining why it was rejected. */
    public static String validate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("The query is empty.");
        }

        String sql = raw.trim();
        while (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }

        // Backslashes change how quotes behave depending on the server's SQL mode, so never allow them.
        if (sql.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("The query contains a character that is not allowed: backslash");
        }

        // Look at the query with string literals removed, so words inside 'quotes' do not confuse the checks.
        String bare = LITERAL.matcher(sql).replaceAll("''").toLowerCase(Locale.ROOT);

        for (String bad : new String[]{";", "--", "/*", "*/", "#", "`", "\"", "'"}) {
            if (bare.replace("''", "").contains(bad)) {
                throw new IllegalArgumentException("The query contains a character that is not allowed: " + bad);
            }
        }

        if (!bare.matches("(?s)^(select|with)\\s.*")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed.");
        }

        Matcher forbidden = FORBIDDEN.matcher(bare);
        if (forbidden.find()) {
            throw new IllegalArgumentException("The query uses a word that is not allowed: " + forbidden.group(1));
        }

        Set<String> allowed = new HashSet<>(ALLOWED_TABLES);
        Matcher cte = CTE_NAME.matcher(bare);
        while (cte.find()) {
            allowed.add(cte.group(1));
        }

        Matcher tables = TABLE_REF.matcher(bare);
        while (tables.find()) {
            String table = tables.group(1);
            if (!allowed.contains(table)) {
                throw new IllegalArgumentException("The query uses a table that is not allowed: " + table);
            }
        }

        return sql;
    }
}
