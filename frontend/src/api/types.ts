export interface Usuario {
  id: number;
  login: string;
  perfil: "MILITAR_ESCALADO" | "SD_EP_SARGENTEACAO" | "CABO_SARGENTEACAO" | "SARGENTEANTE";
  militarId: number;
  nomeExibicao: string;
}

export interface PostoGraduacao {
  id: number;
  sigla: string;
  descricao: string;
  nivelHierarquico: number;
}

export interface Subunidade {
  id: number;
  sigla: string;
  nome: string;
  ativo: boolean;
}

export interface Qualificacao {
  id: number;
  nome: string;
  descricao?: string;
}

export interface Militar {
  id: number;
  nomeCompleto: string;
  nomeGuerra: string;
  cpf: string;
  numeroRegistro?: string;
  dataNascimento?: string;
  fusex?: string;
  fotoBase64?: string;
  posto: PostoGraduacao;
  subunidade: Subunidade;
  email?: string;
  telefone?: string;
  situacao: "ATIVO" | "AFASTADO" | "DESLIGADO";
  nomeExibicao: string;
  contadorRodizio: number;
  qualificacoes: Qualificacao[];
}

export interface TipoServico {
  id: number;
  nome: string;
  descricao?: string;
  efetivoNecessario: number;
  horaInicio: string;
  duracaoHoras: number;
  ativo: boolean;
}

export interface RegraEscala {
  id: number;
  tipoServico: TipoServico;
  intervaloMinimo: number;
  diasFolga: number;
  maxServicosMes?: number;
  pesoFimSemana: number;
  pesoFeriado: number;
}

export interface ServicoEscalado {
  id: number;
  data: string;
  tipoServico: TipoServico;
  militar?: Militar;
  posicao?: string;
  situacao: "PREVISTO" | "CUMPRIDO" | "SUBSTITUIDO";
  travado: boolean;
  jaComecou: boolean;
  observacao?: string;
}

export interface Solicitacao {
  id: number;
  servicoOrigem: ServicoEscalado;
  servicoDestino?: ServicoEscalado;
  solicitante: Militar;
  substituto: Militar;
  justificativa: string;
  tipoTroca: "SUBSTITUICAO" | "TROCA_MUTUA";
  situacao: "AGUARDANDO_SUBSTITUTO" | "EM_TRIAGEM" | "AGUARDANDO_AUTORIZACAO" | "AUTORIZADA" | "NEGADA" | "CANCELADA";
  comentarioCabo?: string;
  comentarioSargenteante?: string;
  dataSolicitacao: string;
  dataDecisaoFinal?: string;
}

export interface CandidatoTrocaMutua {
  militar: Militar;
  servicoId: number;
  data: string;
}

export interface Afastamento {
  id: number;
  militar: Militar;
  tipo: "MISSAO" | "DISPENSA" | "FERIAS" | "LICENCA" | "CURSO" | "OUTRO";
  descricao: string;
  dataInicio: string;
  dataFim: string;
  dataRegistro: string;
  loteMissao?: string;
}

export interface Feriado {
  id: number;
  dataInicio: string;
  dataFim: string;
  descricao: string;
  tipo: "NACIONAL" | "MILITAR" | "OM";
}

export interface PerfilAcesso {
  id: number;
  nome: string;
  descricao?: string;
}

export interface UsuarioAdmin {
  id: number;
  login: string;
  ativo: boolean;
  ultimoAcesso?: string;
  militar: Militar;
  perfil: PerfilAcesso;
}

export interface LogAuditoria {
  id: number;
  dataHora: string;
  usuarioLogin?: string;
  usuarioNomeExibicao?: string;
  acao: string;
  descricao?: string;
}

export interface Boletim {
  id: number;
  numero?: string;
  titulo: string;
  conteudoHtml: string;
  autor: Militar;
  dataPublicacao: string;
  dataAtualizacao?: string;
  avisoRelacionado?: string;
  avisoRelacionadoDescricao?: string;
}

export interface Notificacao {
  id: number;
  tipo: string;
  mensagem: string;
  link?: string;
  lida: boolean;
  dataCriacao: string;
}

export interface Aviso {
  chave: string;
  tipo: string;
  dataInicio: string;
  dataFim: string;
  descricao: string;
  militares: Militar[];
}

export interface Escala {
  id: number;
  descricao: string;
  dataInicio: string;
  dataFim: string;
  situacao: "RASCUNHO" | "PUBLICADA" | "ENCERRADA";
  dataGeracao: string;
  dataPublicacao?: string;
  servicos: ServicoEscalado[];
}
