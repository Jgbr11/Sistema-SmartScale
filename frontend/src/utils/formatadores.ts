/** Formatadores de exibição usados em várias telas — um lugar só. */

/** "2026-09-05" → "05/09/2026". Recebe a data ISO do backend (LocalDate). */
export function formatarDataBR(iso: string): string {
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}

/** Data e hora local (LocalDateTime do backend) no padrão brasileiro. */
export function formatarDataHora(iso: string): string {
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit",
  });
}

export function formatarPeriodo(inicio: string, fim: string): string {
  if (inicio === fim) return formatarDataBR(inicio);
  return `${formatarDataBR(inicio)} a ${formatarDataBR(fim)}`;
}

/** CPF guardado só com dígitos → "000.000.000-00". Qualquer outro tamanho volta como veio. */
export function formatarCpf(cpf: string): string {
  if (cpf.length !== 11) return cpf;
  return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}`;
}

export function capitalizar(s: string): string {
  return s.charAt(0).toUpperCase() + s.slice(1);
}
