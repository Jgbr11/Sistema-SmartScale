import { useEffect, useState } from "react";
import { api } from "../api/client";
import type { Afastamento, Militar, TipoServico } from "../api/types";
import { TIPO_AFASTAMENTO_LABEL } from "../utils/afastamentoTipos";

/**
 * Tela sobreposta (não é um alert/popup nativo) com os dados do militar,
 * inspirada na carteira de identidade militar oficial: crachá com foto,
 * nome completo, nome de guerra, posto, cursos e em que funções da
 * escala ele pode servir.
 */
export function MilitarDetalheOverlay({ militarId, onFechar }: { militarId: number | null; onFechar: () => void }) {
  const [militar, setMilitar] = useState<Militar | null>(null);
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
      setFuncoes(f);
      setAfastamento(a);
      setCarregando(false);
    });
  }, [militarId]);

  if (militarId === null) return null;

  return (
    <div
      style={{
        position: "fixed", inset: 0, background: "rgba(28,33,23,0.55)",
        display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1000,
      }}
      onClick={onFechar}
    >
      <div
        style={{ width: 520, maxWidth: "92vw", background: "#fff", borderRadius: 8, overflow: "hidden", border: "1px solid var(--border)" }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ background: "var(--sidebar)", padding: "14px 20px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <div style={{ color: "#fff", fontFamily: "var(--font-display)", fontWeight: 700, fontSize: 13, letterSpacing: 0.5 }}>
              CARTEIRA DE IDENTIDADE MILITAR
            </div>
            <div style={{ color: "var(--sidebar-sub)", fontSize: 10.5 }}>5º Batalhão de Suprimento</div>
          </div>
          <button
            onClick={onFechar}
            style={{ background: "none", border: "none", color: "#fff", fontSize: 18, cursor: "pointer", lineHeight: 1 }}
          >
            ✕
          </button>
        </div>

        {carregando || !militar ? (
          <div style={{ padding: 30 }}>Carregando…</div>
        ) : (
          <div style={{ padding: 20 }}>
            <div style={{ display: "flex", gap: 18 }}>
              <div
                style={{
                  width: 108, height: 130, borderRadius: 4, background: "var(--table-head-bg)",
                  border: "1px solid var(--border)", flexShrink: 0, overflow: "hidden",
                  display: "flex", alignItems: "center", justifyContent: "center",
                }}
              >
                {militar.fotoBase64 ? (
                  <img src={militar.fotoBase64} alt="" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                ) : (
                  <span style={{ fontSize: 32, color: "var(--grey-light)", fontFamily: "var(--font-display)", fontWeight: 700 }}>
                    {militar.nomeGuerra.charAt(0)}
                  </span>
                )}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 10.5, color: "var(--grey)", fontWeight: 600 }}>NOME</div>
                <div style={{ fontSize: 15, fontWeight: 700, marginBottom: 8 }}>{militar.nomeCompleto}</div>
                <div style={{ fontSize: 10.5, color: "var(--grey)", fontWeight: 600 }}>NOME DE GUERRA</div>
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 8 }}>
                  <div style={{ fontSize: 15, fontWeight: 700 }}>{militar.nomeGuerra.toUpperCase()}</div>
                  {afastamento && (
                    <span className="pill pill-amber" title="Motivo completo visível em Missões e Dispensas">
                      {TIPO_AFASTAMENTO_LABEL[afastamento.tipo]}
                    </span>
                  )}
                </div>
                <div style={{ display: "flex", gap: 20 }}>
                  <div>
                    <div style={{ fontSize: 10.5, color: "var(--grey)", fontWeight: 600 }}>POSTO/GRAD</div>
                    <div style={{ fontSize: 13, fontWeight: 600 }}>{militar.posto.descricao}</div>
                  </div>
                  <div>
                    <div style={{ fontSize: 10.5, color: "var(--grey)", fontWeight: 600 }}>SUBUNIDADE</div>
                    <div style={{ fontSize: 13, fontWeight: 600 }}>{militar.subunidade.nome}</div>
                  </div>
                </div>
              </div>
            </div>

            <div className="popup-dados-grid" style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 12, marginTop: 16, paddingTop: 14, borderTop: "1px solid var(--border-2)" }}>
              <CampoDado label="CPF" valor={formatarCpf(militar.cpf)} />
              <CampoDado label="NR REGISTRO" valor={militar.numeroRegistro || "—"} />
              <CampoDado label="DATA NASCIMENTO" valor={militar.dataNascimento ? formatarDataBR(militar.dataNascimento) : "—"} />
              <CampoDado label="FUSEX" valor={militar.fusex || "—"} />
              <CampoDado label="SITUAÇÃO" valor={militar.situacao} />
              <CampoDado label="ÚLTIMO SERVIÇO" valor={formatarContador(militar.contadorRodizio)} />
            </div>

            <div style={{ marginTop: 16, paddingTop: 14, borderTop: "1px solid var(--border-2)" }}>
              <div style={{ fontSize: 10.5, color: "var(--grey)", fontWeight: 600, marginBottom: 6 }}>CURSOS</div>
              {militar.qualificacoes.length === 0 ? (
                <span style={{ fontSize: 12, color: "var(--grey)" }}>Nenhum curso registrado</span>
              ) : (
                militar.qualificacoes.map((q) => (
                  <span key={q.id} className="pill pill-grey" style={{ marginRight: 6 }}>{q.nome}</span>
                ))
              )}
            </div>

            <div style={{ marginTop: 14 }}>
              <div style={{ fontSize: 10.5, color: "var(--grey)", fontWeight: 600, marginBottom: 6 }}>PODE SERVIR EM</div>
              {funcoes.length === 0 ? (
                <span style={{ fontSize: 12, color: "var(--grey)" }}>Nenhuma função elegível no momento</span>
              ) : (
                funcoes.map((f) => (
                  <span key={f.id} className="pill pill-green" style={{ marginRight: 6, marginBottom: 6, display: "inline-block" }}>{f.nome}</span>
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
      <div style={{ fontSize: 10, color: "var(--grey)", fontWeight: 600 }}>{label}</div>
      <div style={{ fontSize: 12.5, fontWeight: 600 }}>{valor}</div>
    </div>
  );
}

function formatarCpf(cpf: string) {
  if (cpf.length !== 11) return cpf;
  return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}`;
}
function formatarDataBR(iso: string) {
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}
function formatarContador(dias: number): string {
  if (dias > 100000) return "nunca serviu";
  return `há ${Math.abs(dias)} dia${Math.abs(dias) === 1 ? "" : "s"}`;
}
