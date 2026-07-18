export function Loader({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="loader" role="status" aria-live="polite">
      <div className="loader-spinner" />
      <span>{label}</span>
    </div>
  )
}

export function PageLoader({ label }: { label?: string }) {
  return (
    <div className="page-loader">
      <Loader label={label} />
    </div>
  )
}
