-- V1 - schema inicial do MilScale, gerado a partir das entidades JPA
-- (Hibernate, dialeto MySQL) e normalizado pra rodar tambem no H2 em
-- MODE=MySQL. Bancos criados antes do Flyway recebem baseline nesta
-- versao (spring.flyway.baseline-on-migrate) e nao executam este script.
--
-- Ajustes em relacao ao DDL gerado:
--  * colunas de enum (@Enumerated STRING) viram varchar com o tamanho do
--    @Column - bancos antigos ja tem varchar nelas;
--  * foto_base64 e conteudo_html (@Lob String) viram longtext - o Hibernate
--    gerava tinytext (max. 255 bytes), pequeno demais pra foto ou imagem colada;
--  * sem "engine=InnoDB" (padrao do MySQL 8; o H2 nao precisa).

    create table afastamento (
        data_fim date not null,
        data_inicio date not null,
        data_registro datetime(6) not null,
        id_afastamento bigint not null auto_increment,
        id_militar bigint not null,
        id_usuario_registro bigint not null,
        lote_missao varchar(40),
        descricao varchar(150) not null,
        tipo varchar(15) not null,
        primary key (id_afastamento)
    );

    create table boletim (
        data_atualizacao datetime(6),
        data_publicacao datetime(6) not null,
        id_autor bigint not null,
        id_boletim bigint not null auto_increment,
        numero varchar(20),
        aviso_relacionado varchar(60),
        aviso_relacionado_descricao varchar(150),
        titulo varchar(150) not null,
        conteudo_html longtext not null,
        primary key (id_boletim)
    );

    create table escala (
        data_fim date not null,
        data_inicio date not null,
        data_geracao datetime(6) not null,
        data_publicacao datetime(6),
        id_escala bigint not null auto_increment,
        id_subunidade bigint,
        id_usuario_geracao bigint not null,
        descricao varchar(80) not null,
        situacao varchar(15) not null,
        primary key (id_escala)
    );

    create table feriado (
        data_fim date not null,
        data_inicio date not null,
        id_feriado bigint not null auto_increment,
        tipo varchar(15) not null,
        descricao varchar(100) not null,
        primary key (id_feriado)
    );

    create table log_auditoria (
        data_hora datetime(6) not null,
        id_log bigint not null auto_increment,
        usuario_login varchar(20),
        acao varchar(60) not null,
        usuario_nome_exibicao varchar(60),
        descricao varchar(300),
        primary key (id_log)
    );

    create table militar (
        data_nascimento date,
        data_ultimo_servico date,
        id_militar bigint not null auto_increment,
        id_posto bigint not null,
        id_subunidade bigint not null,
        cpf varchar(11) not null,
        fusex varchar(20),
        numero_registro varchar(20),
        telefone varchar(20),
        nome_guerra varchar(40) not null,
        email varchar(120),
        nome_completo varchar(120) not null,
        foto_base64 longtext,
        situacao varchar(15) not null,
        primary key (id_militar)
    );

    create table militar_qualificacao (
        id_militar bigint not null,
        id_qualificacao bigint not null,
        primary key (id_militar, id_qualificacao)
    );

    create table notificacao (
        lida bit not null,
        data_criacao datetime(6) not null,
        id_notificacao bigint not null auto_increment,
        id_usuario_destinatario bigint not null,
        tipo varchar(40) not null,
        link varchar(60),
        mensagem varchar(200) not null,
        primary key (id_notificacao)
    );

    create table perfil_acesso (
        id_perfil bigint not null auto_increment,
        nome varchar(40) not null,
        descricao varchar(150),
        primary key (id_perfil)
    );

    create table posto_graduacao (
        nivel_hierarquico integer not null,
        id_posto bigint not null auto_increment,
        sigla varchar(10) not null,
        descricao varchar(60) not null,
        primary key (id_posto)
    );

    create table qualificacao (
        id_qualificacao bigint not null auto_increment,
        nome varchar(60) not null,
        descricao varchar(120),
        primary key (id_qualificacao)
    );

    create table regra_escala (
        dias_folga integer not null,
        intervalo_minimo integer not null,
        max_servicos_mes integer,
        peso_feriado decimal(3,1) not null,
        peso_fim_semana decimal(3,1) not null,
        id_regra bigint not null auto_increment,
        id_tipo_servico bigint not null,
        primary key (id_regra)
    );

    create table requisito_qualificacao_excluida (
        id_qualificacao bigint not null,
        id_requisito bigint not null,
        primary key (id_qualificacao, id_requisito)
    );

    create table requisito_servico (
        id_posto bigint not null,
        id_qualificacao bigint,
        id_requisito bigint not null auto_increment,
        id_subunidade bigint,
        id_subunidade_excluida bigint,
        id_tipo_servico bigint not null,
        primary key (id_requisito)
    );

    create table servico_escalado (
        data date not null,
        travado bit not null,
        id_escala bigint not null,
        id_militar bigint,
        id_servico_escalado bigint not null auto_increment,
        id_tipo_servico bigint not null,
        posicao varchar(40),
        observacao varchar(150),
        situacao varchar(15) not null,
        primary key (id_servico_escalado)
    );

    create table solicitacao (
        data_decisao_final datetime(6),
        data_solicitacao datetime(6) not null,
        id_servico_destino bigint,
        id_servico_escalado bigint not null,
        id_solicitacao bigint not null auto_increment,
        id_solicitante bigint not null,
        id_substituto bigint not null,
        comentario_cabo varchar(250),
        comentario_sargenteante varchar(250),
        justificativa varchar(250) not null,
        situacao varchar(25) not null,
        tipo_troca varchar(20) not null,
        primary key (id_solicitacao)
    );

    create table subunidade (
        ativo bit not null,
        id_subunidade bigint not null auto_increment,
        sigla varchar(20) not null,
        nome varchar(80) not null,
        primary key (id_subunidade)
    );

    create table tipo_servico (
        ativo bit not null,
        duracao_horas integer not null,
        efetivo_necessario integer not null,
        hora_inicio time(6) not null,
        id_tipo_servico bigint not null auto_increment,
        nome varchar(60) not null,
        descricao varchar(150),
        primary key (id_tipo_servico)
    );

    create table usuario (
        ativo bit not null,
        id_militar bigint not null,
        id_perfil bigint not null,
        id_usuario bigint not null auto_increment,
        ultimo_acesso datetime(6),
        login varchar(20) not null,
        senha_hash varchar(255) not null,
        primary key (id_usuario)
    );

    alter table militar 
       add constraint UK8m58fxe3d6myxpr3m1mbk8okg unique (cpf);

    alter table perfil_acesso 
       add constraint UKphscd09njq650crx6yfjmfsrv unique (nome);

    alter table posto_graduacao 
       add constraint UKjnv51127286mvg1smdhahnv8s unique (nivel_hierarquico);

    alter table posto_graduacao 
       add constraint UKagsa8jg18dt5994ekhsi8p5l8 unique (sigla);

    alter table qualificacao 
       add constraint UKh5bxvldep8n5xjcajr1lgi22k unique (nome);

    alter table regra_escala 
       add constraint UKiyw23fhdwfrk9rp137akb3502 unique (id_tipo_servico);

    alter table subunidade 
       add constraint UKfx7roql2065ty53a93awar81m unique (sigla);

    alter table tipo_servico 
       add constraint UKhs92cjhtu4mqr8lffixlfpgys unique (nome);

    alter table usuario 
       add constraint UKq0ptu75kdt8q41upg7o20eym1 unique (id_militar);

    alter table usuario 
       add constraint UKpm3f4m4fqv89oeeeac4tbe2f4 unique (login);

    alter table afastamento 
       add constraint FK41pirpa08ohr77slfwvd2cejj 
       foreign key (id_militar) 
       references militar (id_militar);

    alter table afastamento 
       add constraint FKto50l4jgcbtwfe8ujvq7qu90j 
       foreign key (id_usuario_registro) 
       references usuario (id_usuario);

    alter table boletim 
       add constraint FKi6dv5wykwqxjewgnlafbemnm 
       foreign key (id_autor) 
       references militar (id_militar);

    alter table escala 
       add constraint FK4r98wdrlbb5chtaar7nnkky2j 
       foreign key (id_subunidade) 
       references subunidade (id_subunidade);

    alter table escala 
       add constraint FK1haevx6sagk6kles0ard9eqap 
       foreign key (id_usuario_geracao) 
       references usuario (id_usuario);

    alter table militar 
       add constraint FK7wg666msik872d8x0vc50rivw 
       foreign key (id_posto) 
       references posto_graduacao (id_posto);

    alter table militar 
       add constraint FKa1f3xh4pbb0sl9tlq86b86ahg 
       foreign key (id_subunidade) 
       references subunidade (id_subunidade);

    alter table militar_qualificacao 
       add constraint FK29t79gy6ggg14kif3gyi43ucj 
       foreign key (id_qualificacao) 
       references qualificacao (id_qualificacao);

    alter table militar_qualificacao 
       add constraint FKg2ysiurpobuhf83i5tsanifga 
       foreign key (id_militar) 
       references militar (id_militar);

    alter table notificacao 
       add constraint FK5seo5on4i9ei53f62b71u2oax 
       foreign key (id_usuario_destinatario) 
       references usuario (id_usuario);

    alter table regra_escala 
       add constraint FKaeu85fkeeo5yrv91j4apbd988 
       foreign key (id_tipo_servico) 
       references tipo_servico (id_tipo_servico);

    alter table requisito_qualificacao_excluida 
       add constraint FKbwxmtbv9ogq1mwjuswovvxlfl 
       foreign key (id_qualificacao) 
       references qualificacao (id_qualificacao);

    alter table requisito_qualificacao_excluida 
       add constraint FKa4d79rdtukttfwksd4tls3dm8 
       foreign key (id_requisito) 
       references requisito_servico (id_requisito);

    alter table requisito_servico 
       add constraint FKcxbstf0sw94o8qaht3k870hwc 
       foreign key (id_posto) 
       references posto_graduacao (id_posto);

    alter table requisito_servico 
       add constraint FKp9rdl6q9a2h2btnbs67om950c 
       foreign key (id_qualificacao) 
       references qualificacao (id_qualificacao);

    alter table requisito_servico 
       add constraint FKjv5dhrcukp3i6l1u5ufxk7qld 
       foreign key (id_subunidade) 
       references subunidade (id_subunidade);

    alter table requisito_servico 
       add constraint FK3ino87ed6ryxf7ya2hofv50dd 
       foreign key (id_subunidade_excluida) 
       references subunidade (id_subunidade);

    alter table requisito_servico 
       add constraint FK1pdpf3yans2hp1mrdhmb5jhok 
       foreign key (id_tipo_servico) 
       references tipo_servico (id_tipo_servico);

    alter table servico_escalado 
       add constraint FK4phcpjd53uj2x92gl1s9ouv14 
       foreign key (id_escala) 
       references escala (id_escala);

    alter table servico_escalado 
       add constraint FKlpx9ayjc3mo4xe8dcsce0daac 
       foreign key (id_militar) 
       references militar (id_militar);

    alter table servico_escalado 
       add constraint FKn1qjigou65kwc5cmea1y4l4w1 
       foreign key (id_tipo_servico) 
       references tipo_servico (id_tipo_servico);

    alter table solicitacao 
       add constraint FKoq3uf8m8iijaewbrpywjh11bn 
       foreign key (id_servico_destino) 
       references servico_escalado (id_servico_escalado);

    alter table solicitacao 
       add constraint FKbowx59efgrd7kwlhh90uqy8bj 
       foreign key (id_servico_escalado) 
       references servico_escalado (id_servico_escalado);

    alter table solicitacao 
       add constraint FK5x5q7n7xuviqcj7b1mce2o57y 
       foreign key (id_solicitante) 
       references militar (id_militar);

    alter table solicitacao 
       add constraint FKof6v8o5jceuk1aa21dneyyvgu 
       foreign key (id_substituto) 
       references militar (id_militar);

    alter table usuario 
       add constraint FK4flqn6yxh8m2pc0nifj8o64bh 
       foreign key (id_militar) 
       references militar (id_militar);

    alter table usuario 
       add constraint FKpb2owcww55ns90kcqbbw4jlrp 
       foreign key (id_perfil) 
       references perfil_acesso (id_perfil);
