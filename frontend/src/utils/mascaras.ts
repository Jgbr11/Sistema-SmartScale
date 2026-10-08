

export function mascararCpf(valor: string): string {
  const digitos = valor.replace(/\D/g, "").slice(0, 11);
  let saida = digitos;
  if (digitos.length > 9) saida = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
  else if (digitos.length > 6) saida = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6)}`;
  else if (digitos.length > 3) saida = `${digitos.slice(0, 3)}.${digitos.slice(3)}`;
  return saida;
}

export function mascararTelefone(valor: string): string {
  const digitos = valor.replace(/\D/g, "").slice(0, 11);
  const celular = digitos.length > 10;
  let saida = digitos;
  if (digitos.length > 6) {
    saida = celular
      ? `(${digitos.slice(0, 2)}) ${digitos.slice(2, 7)}-${digitos.slice(7)}`
      : `(${digitos.slice(0, 2)}) ${digitos.slice(2, 6)}-${digitos.slice(6)}`;
  } else if (digitos.length > 2) {
    saida = `(${digitos.slice(0, 2)}) ${digitos.slice(2)}`;
  } else if (digitos.length > 0) {
    saida = `(${digitos}`;
  }
  return saida;
}

export function mascararFusex(valor: string): string {
  const digitos = valor.replace(/\D/g, "").slice(0, 5);
  return digitos.length > 3 ? `${digitos.slice(0, 3)}-${digitos.slice(3)}` : digitos;
}

export function mascararCep(valor: string): string {
  const digitos = valor.replace(/\D/g, "").slice(0, 8);
  return digitos.length > 5 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : digitos;
}

export function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, "");
}
