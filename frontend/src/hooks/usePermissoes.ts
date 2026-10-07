import { useAuth } from "../context/AuthContext";

export function usePermissoes() {
  const perfil = useAuth().usuario?.perfil;
  const sargenteante = perfil === "SARGENTEANTE";
  const cabo = perfil === "CABO_SARGENTEACAO";
  return {
    perfil,
    gerenciaCadastros: sargenteante || cabo,
    geraEscala: sargenteante || cabo,
    fazTriagem: sargenteante || cabo,
    publicaEscala: sargenteante,
    autorizaTrocas: sargenteante,
    mantemConfiguracoes: sargenteante,
    daSargenteacao: sargenteante || cabo || perfil === "SD_EP_SARGENTEACAO",
  };
}
