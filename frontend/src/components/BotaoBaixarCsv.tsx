import { BASE_URL } from "../api/client";
import { useAuth } from "../context/AuthContext";

const PERFIS_COM_RELATORIO = ["CABO_SARGENTEACAO", "SD_EP_SARGENTEACAO", "SARGENTEANTE"];

export function BotaoBaixarCsv({ caminho, rotulo = "Baixar CSV" }: { caminho: string; rotulo?: string }) {
  const { usuario } = useAuth();
  if (!usuario || !PERFIS_COM_RELATORIO.includes(usuario.perfil)) return null;

  return (
    <a className="btn btn-outline" href={`${BASE_URL}/api/relatorios/${caminho}`} download>
      {rotulo}
    </a>
  );
}
