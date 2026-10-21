package com.vyapaar.dto;

/**
 * One party and its computed balance, filled by Spring Data from the SQL column aliases.
 * The getter names must match the aliases in ReportsRepository's queries exactly.
 */
public interface PartyBalanceDTO {
    Long getId();

    String getName();

    Double getBalance();
}
