import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { mascararCpf, somenteDigitos } from "../utils/mascaras";

export function LoginPage() {
  const { entrar } = useAuth();
  const navigate = useNavigate();
  const [login, setLogin] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      // Aceita com ou sem formatação — só os números importam pro login.
      await entrar(somenteDigitos(login), senha);
      navigate("/");
    } catch {
      setErro("CPF ou senha inválidos.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="login-wrap">
      <div className="login-ident">
        <h1>MilScale</h1>
        <p style={{ fontSize: 20, fontWeight: 500, color: "#c7d2ba" }}>
          Escala de serviço do batalhão
        </p>
        <div className="rule" />
        <p>
          A escala de 24 horas do batalhão, montada automaticamente pela ordem
          de quem está há mais tempo sem tirar serviço.
        </p>
      </div>
      <div className="login-form-wrap">
        <form className="login-card" onSubmit={handleSubmit}>
          <h2>Entrar</h2>
          <p className="sub">Use seu CPF e a senha do batalhão.</p>

          {erro && <div className="error-box">{erro}</div>}

          <div className="field">
            <label>CPF</label>
            <input
              value={login}
              onChange={(e) => setLogin(mascararCpf(e.target.value))}
              placeholder="000.000.000-00"
              autoFocus
            />
          </div>
          <div className="field">
            <label>Senha</label>
            <input
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              placeholder="••••••••"
            />
          </div>
          <button className="btn btn-primary" style={{ width: "100%" }} disabled={enviando}>
            {enviando ? "Entrando…" : "Entrar"}
          </button>
          <p style={{ fontSize: 11, color: "#8a9188", marginTop: 14 }}>
            Contas de demonstração (senha <code>milscale123</code>): 000.000.000-01
            (sargenteante), 000.000.000-02 (cabo), 000.000.000-03 (soldado),
            000.000.000-04 (nogueira).
          </p>
        </form>
      </div>
    </div>
  );
}
