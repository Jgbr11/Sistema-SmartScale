import { useEffect, useState } from "react";
import { api } from "../../api/client";
import type { Afastamento, Militar, TipoServico } from "../../api/types";
import { useOrganizacao } from "../../hooks/useOrganizacao";
import { TIPO_AFASTAMENTO_LABEL } from "../../utils/afastamentoTipos";
import { formatarCpf, formatarDataBR } from "../../utils/formatadores";
import { Esqueleto } from "../ui/Esqueleto";

export function MilitarDetalheOverlay({ militarId, onFechar }: { militarId: number | null; onFechar: () => void }) {
  const [militar, setMilitar] = useState<Militar | null>(null);
  const [foto, setFoto] = useState<string | null>(null);
  const organizacao = useOrganizacao();
  const [funcoes, setFuncoes] = useState<TipoServico[]>([]);
  const [afastamento, setAfastamento] = useState<Afastamento | null>(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    if (militarId === null) return;
    setCarregando(true);
    setMilitar(null);
    Promise.all([
      api.get<Militar>(`/api/militares/${militarId}`),
      api.get<TipoServico[]>(`/api/militares/${militarId}/funcoes-elegiveis`),
      api.get<Afastamento | null>(`/api/militares/${militarId}/afastamento-atual`),
    ]).then(([m, f, a]) => {
      setMilitar(m);
      setFoto(null);
      if (m.temFoto) {
        api.get<{ fotoBase64: string } | undefined>(`/api/militares/${militarId}/foto`)
          .then((r) => setFoto(r?.fotoBase64 ?? null))
          .catch(() => setFoto(null));
      }
      setFuncoes(f);
      setAfastamento(a);
      setCarregando(false);
    });
  }, [militarId]);

  if (militarId === null) return null;

  return (
    <div
      className="dialogo-fundo"
      onClick={onFechar}
    >
      <div
        className="popup-janela"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="popup-topo">
          <div>
            <div className="popup-titulo">
              CARTEIRA DE IDENTIDADE MILITAR
            </div>
            <div className="popup-sub">{organizacao?.nome ?? ""}</div>
          </div>
          <button
            onClick={onFechar}
            className="popup-fechar"
          >
            ✕
          </button>
        </div>

        {carregando || !militar ? (
          <Esqueleto />
        ) : (
          <div className="p-20">
            <div className="linha-larga">
              <div
                className="foto-3x4"
              >
                {foto ? (
                  <img src={foto} alt="" className="foto-cheia" />
                ) : (
                  <span className="foto-inicial">
                    {militar.nomeGuerra.charAt(0)}
                  </span>
                )}
              </div>
              <div className="flex-1">
                <div className="rotulo-campo">NOME</div>
                <div className="popup-nome mb-8">{militar.nomeCompleto}</div>
                <div className="rotulo-campo">NOME DE GUERRA</div>
                <div className="linha mb-8">
                  <div className="popup-nome">{militar.nomeGuerra.toUpperCase()}</div>
                  {afastamento && (
                    <span className="pill pill-amber" title="Motivo completo visível em Missões e Dispensas">
                      {TIPO_AFASTAMENTO_LABEL[afastamento.tipo]}
                    </span>
                  )}
                </div>
                <div className="linha-larga">
                  <div>
                    <div className="rotulo-campo">POSTO/GRAD</div>
                    <div className="texto-forte">{militar.posto.descricao}</div>
                  </div>
                  <div>
                    <div className="rotulo-campo">SUBUNIDADE</div>
                    <div className="texto-forte">{militar.subunidade.nome}</div>
                  </div>
                </div>
              </div>
            </div>

            <div className="popup-dados-grid popup-grade secao-divisoria">
              {militar.cpf !== undefined && (
                <>
                  <CampoDado label="CPF" valor={formatarCpf(militar.cpf)} />
                  <CampoDado label="NR REGISTRO" valor={militar.numeroRegistro || "—"} />
                  <CampoDado label="DATA NASCIMENTO" valor={militar.dataNascimento ? formatarDataBR(militar.dataNascimento) : "—"} />
                  <CampoDado label="FUSEX" valor={militar.fusex || "—"} />
                </>
              )}
              <CampoDado label="SITUAÇÃO" valor={militar.situacao} />
              <CampoDado label="ÚLTIMO SERVIÇO" valor={formatarContador(militar.contadorRodizio)} />
            </div>

            <div className="secao-divisoria">
              <div className="rotulo-campo mb-6">CURSOS</div>
              {militar.qualificacoes.length === 0 ? (
                <span className="nota">Nenhum curso registrado</span>
              ) : (
                militar.qualificacoes.map((q) => (
                  <span key={q.id} className="pill pill-grey mr-6">{q.nome}</span>
                ))
              )}
            </div>

            <div className="mt-14">
              <div className="rotulo-campo mb-6">PODE SERVIR EM</div>
              {funcoes.length === 0 ? (
                <span className="nota">Nenhuma função elegível no momento</span>
              ) : (
                funcoes.map((f) => (
                  <span key={f.id} className="pill pill-green etiqueta-lista">{f.nome}</span>
                ))
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function CampoDado({ label, valor }: { label: string; valor: string }) {
  return (
    <div>
      <div className="rotulo-mini">{label}</div>
      <div className="texto-125 negrito-medio">{valor}</div>
    </div>
  );
}

function formatarContador(dias: number): string {
  if (dias > 100000) return "nunca serviu";
  return `há ${Math.abs(dias)} dia${Math.abs(dias) === 1 ? "" : "s"}`;
}
