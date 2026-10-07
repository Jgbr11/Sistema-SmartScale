-- ---- Fotografia do serviço na solicitação ----
alter table solicitacao add column servico_origem_data date;
alter table solicitacao add column servico_origem_tipo varchar(60);
alter table solicitacao add column servico_destino_data date;

update solicitacao set
    servico_origem_data = (select se.data from servico_escalado se where se.id_servico_escalado = solicitacao.id_servico_escalado),
    servico_origem_tipo = (select ts.nome from servico_escalado se join tipo_servico ts on ts.id_tipo_servico = se.id_tipo_servico
                           where se.id_servico_escalado = solicitacao.id_servico_escalado),
    servico_destino_data = (select se.data from servico_escalado se where se.id_servico_escalado = solicitacao.id_servico_destino);

alter table solicitacao modify column id_servico_escalado bigint null;
