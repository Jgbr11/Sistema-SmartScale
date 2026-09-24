const BASE_URL = "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    credentials: "include",
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(options.headers || {}),
    },
  });

  if (!res.ok) {
    let mensagem = `Erro ${res.status} ao chamar ${path}`;
    try {
      const corpo = await res.json();
      if (corpo?.erro) mensagem = corpo.erro;
    } catch {
      // corpo nao era JSON com {erro: ...} - mantem a mensagem generica
    }
    throw new ApiError(res.status, mensagem);
  }
  const text = await res.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}

export const api = {
  get: <T,>(path: string) => request<T>(path),
  post: <T,>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body: body ? JSON.stringify(body) : undefined }),
  put: <T,>(path: string, body?: unknown) =>
    request<T>(path, { method: "PUT", body: body ? JSON.stringify(body) : undefined }),
  delete: <T,>(path: string) => request<T>(path, { method: "DELETE" }),

  // login usa application/x-www-form-urlencoded, como o Spring Security espera
  login: async (login: string, senha: string) => {
    const res = await fetch(`${BASE_URL}/api/auth/login`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ username: login, password: senha }),
    });
    if (!res.ok) throw new ApiError(res.status, "Usuário ou senha inválidos");
  },
  logout: async () => {
    await fetch(`${BASE_URL}/api/auth/logout`, { method: "POST", credentials: "include" });
  },
};
