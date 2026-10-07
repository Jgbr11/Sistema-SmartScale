export function PageHeader({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="header-bar">
      <h2>{title}</h2>
      {subtitle && <p>{subtitle}</p>}
    </div>
  );
}
