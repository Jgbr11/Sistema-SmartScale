from pathlib import Path

from validar_modelo import carregar

AQUI = Path(__file__).parent
MODELO = AQUI / 'SmartScale' / 'model.xml'

COMUM_MILSCALE = {
    'MotorDeRodizio', 'CatalogoDeCriterios', 'ContratosDoNucleo', 'MaiorFolga',
    'Pessoas', 'Cargos', 'Unidades', 'TiposDeTurno', 'Qualificacoes', 'Feriados',
    'GeracaoAutomatica', 'PorCargo', 'PorUnidade', 'PorQualificacao',
    'IntervaloMinimo', 'MaximoMensal', 'PesoFimDeSemana', 'PesoFeriado',
    'Publicacao', 'TravamentoDeDias', 'ConsultaPessoal',
    'Substituicao', 'TrocaMutua', 'AprovacaoEmDuasEtapas',
    'Missao', 'Dispensa', 'Ferias', 'Licenca', 'Curso', 'OutroAfastamento', 'ReconciliacaoAutomatica',
    'NotificacoesNoSistema', 'LembretePorEmail', 'BoletimInterno', 'Avisos',
    'CsvEscalaDoDia', 'CsvServicosDaPessoa', 'CsvAfastamentosDoMes', 'PdfEscalaDoDia',
    'Autenticacao', 'PerfisEPermissoes', 'Auditoria', 'ProtecaoForcaBruta', 'SenhaTemporaria',
    'PainelWeb', 'CalendarioMensal', 'FitaDoServico', 'LayoutResponsivo',
}

PRODUTOS = {
    'MilScale-Docker': COMUM_MILSCALE | {'MySQL', 'DockerCompose'},
    'MilScale-Desenvolvimento': COMUM_MILSCALE | {'H2', 'ExecucaoLocal'},
    'PlantaoHospitalar': {
        'MotorDeRodizio', 'CatalogoDeCriterios', 'ContratosDoNucleo', 'MenorCarga',
        'Pessoas', 'Cargos', 'Unidades', 'TiposDeTurno', 'Qualificacoes', 'Feriados',
        'GeracaoAutomatica', 'PorCargo', 'PorQualificacao', 'IntervaloMinimo', 'MaximoMensal',
        'Publicacao', 'ConsultaPessoal', 'Substituicao',
        'Ferias', 'Licenca', 'Curso', 'ReconciliacaoAutomatica',
        'NotificacoesNoSistema', 'LembretePorEmail', 'CsvEscalaDoDia', 'CsvServicosDaPessoa',
        'Autenticacao', 'AcessoUnico', 'Auditoria', 'MySQL', 'DockerCompose',
        'PainelWeb', 'CalendarioMensal', 'LayoutResponsivo',
    },
}


def gerar():
    modelo = carregar(MODELO)
    pasta = AQUI / 'SmartScale' / 'configs'
    pasta.mkdir(exist_ok=True)
    for nome, escolhidas in PRODUTOS.items():
        desconhecidas = escolhidas - set(modelo.features)
        if desconhecidas:
            raise ValueError(f'{nome}: features fora do modelo: {sorted(desconhecidas)}')
        selecionadas = set(escolhidas)
        for f in escolhidas:
            pai = modelo.features[f].pai
            while pai:
                selecionadas.add(pai)
                pai = modelo.features[pai].pai
        linhas = [
            f'\t<feature manual="{"selected" if n in selecionadas else "unselected"}" name="{n}"/>'
            for n in modelo.features
        ]
        texto = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n<configuration>\n' + '\n'.join(linhas) + '\n</configuration>\n'
        (pasta / f'{nome}.xml').write_text(texto, encoding='utf-8')
        print(f'{nome}: {len(selecionadas)} de {len(modelo.features)} features selecionadas')


if __name__ == '__main__':
    gerar()
