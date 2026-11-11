#!/usr/bin/env bash
# Confere, com a aplicação no ar, que as telas e o CRUD gerados a partir do banco funcionam.
set -euo pipefail
SITE="${SITE:-http://localhost:8081}"
status() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

rotas=$(curl -sf "$SITE/" | grep -oE 'href="/[A-Za-z]+"' | sed 's/href="//; s/"//')
total=$(echo "$rotas" | wc -l)
echo "Tabelas na página inicial: $total"
[ "$total" -eq 17 ]

for rota in $rotas; do
  [ "$(status "$SITE$rota")" = "200" ] && [ "$(status "$SITE$rota/novo")" = "200" ] || { echo "Falhou: $rota"; exit 1; }
done
echo "Listas e formulários das $total tabelas: OK"

campos=(--data-urlencode "dataInicio=2026-12-24" --data-urlencode "dataFim=2026-12-25" --data-urlencode "tipo=NACIONAL")
[ "$(status -X POST "$SITE/feriado" --data-urlencode "idFeriado=" "${campos[@]}" --data-urlencode "descricao=Teste Database First")" = "302" ]
id=$(curl -sf "$SITE/feriado" | grep -oE '/feriado/[0-9]+"' | tail -1 | grep -oE '[0-9]+')
echo "Criar:   feriado $id salvo pelo formulário"

[ "$(status -X POST "$SITE/feriado" --data-urlencode "idFeriado=$id" "${campos[@]}" --data-urlencode "descricao=Teste editado")" = "302" ]
curl -sf "$SITE/feriado" | grep -q "Teste editado"
echo "Editar:  descrição alterada"

[ "$(status -X POST "$SITE/feriado/$id/excluir")" = "302" ]
! curl -sf "$SITE/feriado" | grep -q "Teste editado"
echo "Excluir: feriado removido"

echo "Telas e CRUD gerados a partir do banco: OK ($SITE)"
