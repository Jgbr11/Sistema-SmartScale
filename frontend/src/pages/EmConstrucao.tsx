import { PageHeader } from "../components/Shell";

export function EmConstrucaoPage({ titulo }: { titulo: string }) {
  return (
    <>
      <PageHeader title={titulo} />
      <div className="body">
        <div className="card" style={{ background: "var(--amber-bg)", border: "none" }}>
          <h3 style={{ marginBottom: 6 }}>Ainda não construído nesta fatia</h3>
          <p style={{ fontSize: 12, color: "var(--amber-text)" }}>
            Essa tela já está mapeada no Figma e no menu, mas a funcionalidade em si é
            de uma próxima fatia de implementação — ver README do projeto.
          </p>
        </div>
      </div>
    </>
  );
}
