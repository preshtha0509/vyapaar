export function GarlicIcon({ className = '' }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} xmlns="http://www.w3.org/2000/svg">
      <path
        d="M12 3c1 0 1.5 1 1.5 2 2.5 1 4 3.5 4 6.5 0 4-2.5 7.5-5.5 7.5S6.5 15.5 6.5 11.5c0-3 1.5-5.5 4-6.5 0-1 .5-2 1.5-2z"
        stroke="currentColor"
        strokeWidth="1.4"
        strokeLinejoin="round"
      />
      <path
        d="M12 5v14M9 8.5c0 2 1 3 3 3s3-1 3-3"
        stroke="currentColor"
        strokeWidth="1.2"
        strokeLinecap="round"
      />
    </svg>
  )
}
