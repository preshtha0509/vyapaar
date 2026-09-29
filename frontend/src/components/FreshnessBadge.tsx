import { useTranslation } from 'react-i18next'

type FreshnessBadgeProps = {
  ageInDays: number | null
}

const FRESH_THRESHOLD_DAYS = 7
const AGING_THRESHOLD_DAYS = 14

export function FreshnessBadge({ ageInDays }: FreshnessBadgeProps) {
  const { t } = useTranslation()

  if (ageInDays === null) {
    return <span className="text-xs text-ink-soft">{t('products.notStocked')}</span>
  }

  if (ageInDays > AGING_THRESHOLD_DAYS) {
    return (
      <span className="rounded-full bg-debit-bg px-2.5 py-1 text-xs font-medium text-debit">
        {t('products.daysOld', { count: ageInDays })}
      </span>
    )
  }

  if (ageInDays > FRESH_THRESHOLD_DAYS) {
    return (
      <span className="rounded-full bg-[#F4E9D0] px-2.5 py-1 text-xs font-medium text-[#8A6A1F]">
        {t('products.daysOld', { count: ageInDays })}
      </span>
    )
  }

  return (
    <span className="rounded-full bg-credit-bg px-2.5 py-1 text-xs font-medium text-credit">
      {t('products.daysOld', { count: ageInDays })}
    </span>
  )
}
