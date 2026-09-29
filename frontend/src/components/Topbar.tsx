import { useTranslation } from 'react-i18next'

export function Topbar() {
  const { i18n } = useTranslation()

  const currentLang = i18n.language.startsWith('hi') ? 'hi' : 'en'

  function changeLanguage(lang: 'en' | 'hi') {
    void i18n.changeLanguage(lang)
  }

  return (
    <header className="flex items-center justify-end border-b border-rule bg-paper px-5 py-3 md:px-8">
      {/* Pill-shaped single-toggle language switch */}
      <div className="inline-flex items-center rounded-full border border-rule bg-paper-raised p-1 shadow-2xs">
        <button
          type="button"
          onClick={() => changeLanguage('en')}
          className={[
            'rounded-full px-3 py-1 text-xs font-medium transition-colors',
            currentLang === 'en'
              ? 'bg-cloth text-paper shadow-2xs'
              : 'text-ink-soft hover:text-ink',
          ].join(' ')}
        >
          EN
        </button>
        <button
          type="button"
          onClick={() => changeLanguage('hi')}
          className={[
            'rounded-full px-3 py-1 text-xs font-medium transition-colors',
            currentLang === 'hi'
              ? 'bg-cloth text-paper shadow-2xs'
              : 'text-ink-soft hover:text-ink',
          ].join(' ')}
        >
          HI
        </button>
      </div>
    </header>
  )
}
