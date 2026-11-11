#!/usr/bin/env bash
# Cria o banco milscale_dbfirst, cópia do banco do MilScale, e o banco.properties usado pelo gerador.
set -euo pipefail
AQUI="$(cd "$(dirname "$0")" && pwd)"
RAIZ="${RAIZ:-$(cd "$AQUI/.." && pwd)}"

set -a
source "$RAIZ/.env"
set +a

compose() { docker compose --project-directory "$RAIZ" -f "$RAIZ/docker-compose.yml" "$@"; }
mysql_root() { compose exec -T mysql sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" $1" 2>/dev/null; }

compose up -d mysql
until compose exec -T mysql sh -c 'mysqladmin ping -h localhost -uroot -p"$MYSQL_ROOT_PASSWORD"' 2>/dev/null | grep -q alive; do
  sleep 2
done

tabelas=$(mysql_root "-N -e \"select count(*) from information_schema.tables where table_schema='$MYSQL_DATABASE'\"")
if [ "${tabelas:-0}" -lt 2 ]; then
  echo "O banco '$MYSQL_DATABASE' ainda está vazio. Suba o MilScale uma vez (docker compose up -d) para o Flyway criar as tabelas."
  exit 1
fi

mysql_root "-e \"DROP DATABASE IF EXISTS milscale_dbfirst; CREATE DATABASE milscale_dbfirst; GRANT ALL ON milscale_dbfirst.* TO '$MYSQL_USER'@'%';\""
compose exec -T mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --no-tablespaces "$MYSQL_DATABASE" | mysql -uroot -p"$MYSQL_ROOT_PASSWORD" milscale_dbfirst' 2>/dev/null

cat > "$AQUI/banco.properties" <<EOF
hibernate.connection.driver_class=com.mysql.cj.jdbc.Driver
hibernate.connection.url=jdbc:mysql://localhost:3306/milscale_dbfirst?allowPublicKeyRetrieval=true&useSSL=false
hibernate.connection.username=$MYSQL_USER
hibernate.connection.password=$MYSQL_PASSWORD
hibernate.default_catalog=milscale_dbfirst
EOF

echo "Banco milscale_dbfirst pronto ($tabelas tabelas copiadas de $MYSQL_DATABASE)."
