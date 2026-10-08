import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { Militar } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { useAuth } from "../context/AuthContext";
import { PERFIL_LABEL } from "../utils/perfis";
import { formatarCpf, formatarDataBR } from "../utils/formatadores";
import { Esqueleto } from "../components/ui/Esqueleto";

export function MinhaContaPage() {
  const { usuario, recarregar } = useAuth();
  const [militar, setMilitar] = useState<Militar | null>(null);

  useEffect(() => {
    if (!usuario || usuario.trocarSenha) return;
    api.get<Militar>(`/api/militares/${usuario.militarId}`).then(setMilitar);
  }, [usuario]);

  return (
    <>
      <PageHeader title="Minha conta" subtitle="Seus dados e sua senha de acesso" />
      <div className="body">
        {usuario?.trocarSenha && (
          <div className="card card-atencao">
            <p className="nota-alerta texto-13 negrito-medio">
              Sua senha é temporária. Crie uma senha nova para continuar usando o sistema.
            </p>
          </div>
        )}

        {!usuario?.trocarSenha && (
        <div className="card">
          <h3>Meus dados</h3>
          {!militar ? (
            <Esqueleto />
          ) : (
            <div className="form-grid mt-10">
              <CampoLeitura label="Nome completo" valor={militar.nomeCompleto} />
              <CampoLeitura label="Nome de guerra" valor={militar.nomeGuerra} />
              <CampoLeitura label="Posto/graduação" valor={militar.posto.descricao} />
              <CampoLeitura label="Subunidade" valor={militar.subunidade.nome} />
              <CampoLeitura label="CPF" valor={militar.cpf ? formatarCpf(militar.cpf) : "—"} />
              <CampoLeitura label="NR Registro" valor={militar.numeroRegistro || "—"} />
              <CampoLeitura label="Data de nascimento" valor={militar.dataNascimento ? formatarDataBR(militar.dataNascimento) : "—"} />
              <CampoLeitura label="FUSEX" valor={militar.fusex || "—"} />
              <CampoLeitura label="Perfil de acesso" valor={usuario ? (PERFIL_LABEL[usuario.perfil] ?? usuario.perfil) : "—"} />
            </div>
          )}
          <p className="nota-pequena mt-10">
            Pra corrigir algum desses dados, fale com o Cabo da Sargenteação ou o Sargenteante.
          </p>
        </div>
        )}

        <TrocarSenhaForm onTrocou={recarregar} />
      </div>
    </>
  );
}

function CampoLeitura({ label, valor }: { label: string; valor: string }) {
  return (
    <div className="field">
      <label>{label}</label>
      <div className="item-forte">{valor}</div>
    </div>
  );
}

function TrocarSenhaForm({ onTrocou }: { onTrocou: () => Promise<void> }) {
  const [senhaAtual, setSenhaAtual] = useState("");
  const [senhaNova, setSenhaNova] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState(false);

  async function salvar() {
    setErro(null);
    setSucesso(false);
    if (!senhaAtual || !senhaNova || !confirmarSenha) {
      setErro("Preencha todos os campos.");
      return;
    }
    if (senhaNova !== confirmarSenha) {
      setErro("A nova senha e a confirmação não são iguais.");
      return;
    }
    setSalvando(true);
    try {
      await api.post("/api/auth/senha", { senhaAtual, senhaNova });
      setSucesso(true);
      setSenhaAtual("");
      setSenhaNova("");
      setConfirmarSenha("");
      await onTrocou();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível trocar a senha.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Trocar senha</h3>
      <p className="sub">Sua senha atual precisa ser confirmada por segurança</p>
      {erro && <div className="error-box">{erro}</div>}
      {sucesso && (
        <div className="card card-positivo">
          <p className="texto-125 texto-positivo">Senha alterada com sucesso.</p>
        </div>
      )}
      <div className="form-grid">
        <div className="field">
          <label>Senha atual</label>
          <input type="password" value={senhaAtual} onChange={(e) => setSenhaAtual(e.target.value)} />
        </div>
        <div className="field">
          <label>Nova senha</label>
          <input type="password" value={senhaNova} onChange={(e) => setSenhaNova(e.target.value)} />
        </div>
        <div className="field">
          <label>Confirmar nova senha</label>
          <input type="password" value={confirmarSenha} onChange={(e) => setConfirmarSenha(e.target.value)} />
        </div>
      </div>
      <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
        {salvando ? "Salvando…" : "Trocar senha"}
      </button>
    </div>
  );
}
