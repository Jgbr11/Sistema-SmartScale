export function Esqueleto({ linhas = 4 }: { linhas?: number }) {
  return (
    <div className="esqueleto" aria-busy="true" aria-label="Carregando">
      {Array.from({ length: linhas }, (_, i) => <span key={i} />)}
    </div>
  );
}
