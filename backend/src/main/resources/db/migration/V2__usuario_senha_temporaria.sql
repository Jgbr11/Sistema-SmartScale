-- ---- Senha temporária ----
alter table usuario add column senha_temporaria boolean not null default false;
