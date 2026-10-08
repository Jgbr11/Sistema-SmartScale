-- ---- Travamento otimista ----
alter table solicitacao add column versao bigint not null default 0;
alter table servico_escalado add column versao bigint not null default 0;
