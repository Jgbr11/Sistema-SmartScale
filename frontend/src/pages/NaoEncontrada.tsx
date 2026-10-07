import { Link } from "react-router-dom";
import { PageHeader } from "../components/layout/PageHeader";

export function NaoEncontradaPage() {
  return (
    <>
      <PageHeader title="Página não encontrada" subtitle="O endereço digitado não existe no MilScale" />
      <div className="body">
        <Link to="/" className="btn btn-primary">Ir para o início</Link>
      </div>
    </>
  );
}
