import { useState } from 'react'
import type { FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { useTranslation } from 'react-i18next'
import { api } from '../api/client'
import type { ChatResponse } from '../types'

type Message =
  | { role: 'user'; text: string }
  | { role: 'assistant'; reply: ChatResponse }

const KEY_STORAGE = 'vyapaar.chatKey'
const MAX_ROWS_SHOWN = 50

const numberFormat = new Intl.NumberFormat('en-IN', { maximumFractionDigits: 2 })

// sessionStorage can throw in a private window, and the page must still work.
function readKey() {
  try {
    return sessionStorage.getItem(KEY_STORAGE) ?? ''
  } catch {
    return ''
  }
}

function writeKey(value: string) {
  try {
    if (value) {
      sessionStorage.setItem(KEY_STORAGE, value)
    } else {
      sessionStorage.removeItem(KEY_STORAGE)
    }
  } catch {
    // Storage blocked; the code still works for this visit.
  }
}

function formatCell(value: string | number | boolean | null) {
  if (value === null) return ''
  if (typeof value === 'number') return numberFormat.format(value)
  return String(value)
}

export function ChatPage() {
  const { t } = useTranslation()
  const [accessCode, setAccessCode] = useState(readKey)
  const [codeInput, setCodeInput] = useState('')
  const [messages, setMessages] = useState<Message[]>([])
  const [question, setQuestion] = useState('')
  const [sending, setSending] = useState(false)
  const [openSql, setOpenSql] = useState<number | null>(null)

  const examples = [t('chat.example1'), t('chat.example2'), t('chat.example3')]

  function saveCode(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const code = codeInput.trim()
    if (!code) return
    writeKey(code)
    setAccessCode(code)
    setCodeInput('')
  }

  async function ask(text: string) {
    const trimmed = text.trim()
    if (!trimmed || sending) return

    setMessages((previous) => [...previous, { role: 'user', text: trimmed }])
    setQuestion('')
    setSending(true)

    try {
      const response = await api.post<ChatResponse>(
        '/api/chat',
        { question: trimmed },
        { headers: { 'X-Chat-Key': accessCode } },
      )
      setMessages((previous) => [...previous, { role: 'assistant', reply: response.data }])
    } catch (error) {
      let answer = t('chat.errorGeneric')
      if (isAxiosError<ChatResponse>(error)) {
        // A 401 means the stored code is wrong, so drop it and ask again.
        if (error.response?.status === 401) {
          writeKey('')
          setAccessCode('')
        }
        answer = error.response?.data?.answer ?? answer
      }
      setMessages((previous) => [
        ...previous,
        { role: 'assistant', reply: { answer, sql: null, columns: [], rows: [] } },
      ])
    } finally {
      setSending(false)
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void ask(question)
  }

  return (
    <main className="px-5 py-6 md:px-8 md:py-8">
      <section className="mb-5">
        <p className="m-0 mb-1 text-sm text-ink-soft">{t('chat.eyebrow')}</p>
        <h2 className="m-0 font-display text-3xl font-medium text-ink">{t('chat.title')}</h2>
        <p className="m-0 mt-2 text-sm text-ink-soft">{t('chat.help')}</p>
      </section>

      {!accessCode ? (
        <form
          onSubmit={saveCode}
          className="grid max-w-md gap-3 rounded-[10px] border border-rule bg-paper-raised px-4 py-4"
        >
          <label className="grid gap-1.5 text-sm">
            <span className="text-xs text-ink-soft">{t('chat.accessCode')}</span>
            <input
              id="chat-access-code"
              type="password"
              required
              value={codeInput}
              onChange={(event) => setCodeInput(event.target.value)}
              className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
            />
          </label>
          <button
            type="submit"
            className="rounded-lg border border-cloth bg-cloth px-4 py-2.5 text-sm font-medium text-paper"
          >
            {t('chat.unlock')}
          </button>
        </form>
      ) : (
        <>
          <section className="mb-4 flex flex-wrap gap-2">
            {examples.map((example) => (
              <button
                key={example}
                type="button"
                disabled={sending}
                onClick={() => void ask(example)}
                className="rounded-lg border border-rule bg-paper px-3 py-1.5 text-xs text-ink-soft disabled:opacity-60"
              >
                {example}
              </button>
            ))}
          </section>

          <section className="mb-4 grid gap-3">
            {messages.length === 0 && (
              <div className="rounded-[10px] border border-rule bg-paper-raised px-4 py-6 text-center text-sm text-ink-soft">
                {t('chat.empty')}
              </div>
            )}

            {messages.map((message, index) =>
              message.role === 'user' ? (
                <div
                  key={index}
                  className="ml-auto max-w-[85%] rounded-[10px] border border-rule bg-paper-raised px-4 py-3 text-sm text-ink"
                >
                  {message.text}
                </div>
              ) : (
                <div
                  key={index}
                  className="max-w-full rounded-[10px] border border-rule bg-paper px-4 py-3 text-sm text-ink"
                >
                  <p className="m-0">{message.reply.answer}</p>

                  {message.reply.rows.length > 0 && (
                    <div className="mt-3 overflow-x-auto rounded-lg border border-rule">
                      <table className="w-full border-collapse text-left text-xs">
                        <thead className="bg-paper-raised text-ink-soft">
                          <tr>
                            {message.reply.columns.map((column) => (
                              <th key={column} className="px-3 py-2 font-medium">
                                {column}
                              </th>
                            ))}
                          </tr>
                        </thead>
                        <tbody>
                          {message.reply.rows.slice(0, MAX_ROWS_SHOWN).map((row, rowIndex) => (
                            <tr key={rowIndex} className="border-t border-rule">
                              {message.reply.columns.map((column) => (
                                <td key={column} className="px-3 py-2 font-mono">
                                  {formatCell(row[column] ?? null)}
                                </td>
                              ))}
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}

                  {message.reply.rows.length > MAX_ROWS_SHOWN && (
                    <p className="m-0 mt-2 text-xs text-ink-soft">
                      {t('chat.rowsShown', {
                        shown: MAX_ROWS_SHOWN,
                        total: message.reply.rows.length,
                      })}
                    </p>
                  )}

                  {message.reply.sql && (
                    <div className="mt-3">
                      <button
                        type="button"
                        onClick={() => setOpenSql(openSql === index ? null : index)}
                        className="rounded-lg border border-rule bg-paper-raised px-2.5 py-1 text-xs text-ink-soft"
                      >
                        {openSql === index ? t('chat.hideSql') : t('chat.showSql')}
                      </button>
                      {openSql === index && (
                        <pre className="mt-2 overflow-x-auto whitespace-pre-wrap rounded-lg border border-rule bg-paper-raised p-3 font-mono text-xs">
                          {message.reply.sql}
                        </pre>
                      )}
                    </div>
                  )}
                </div>
              ),
            )}

            {sending && (
              <div className="text-sm text-ink-soft" role="status">
                {t('chat.thinking')}
              </div>
            )}
          </section>

          <form onSubmit={handleSubmit} className="flex gap-2">
            <input
              id="chat-question"
              value={question}
              maxLength={500}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder={t('chat.placeholder')}
              className="min-w-0 flex-1 rounded-lg border border-rule bg-paper px-3 py-2 text-sm outline-none"
            />
            <button
              type="submit"
              disabled={sending || !question.trim()}
              className="rounded-lg border border-cloth bg-cloth px-4 py-2.5 text-sm font-medium text-paper disabled:opacity-60"
            >
              {t('chat.send')}
            </button>
          </form>
        </>
      )}
    </main>
  )
}
