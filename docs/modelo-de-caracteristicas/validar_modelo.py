import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path

from pysat.solvers import Glucose3

GRUPOS = {'and', 'or', 'alt'}
OPERADORES_EXIGIDOS = {'imp': 'Implies', 'conj': 'And', 'disj': 'Or', 'not': 'Not'}


@dataclass
class Feature:
    nome: str
    pai: str | None
    grupo: str
    abstrata: bool
    obrigatoria: bool
    filhos: list = field(default_factory=list)


@dataclass
class Modelo:
    raiz: str
    features: dict
    restricoes: list


def _ler_expr(elemento):
    tag = elemento.tag
    filhos = [e for e in elemento if e.tag != 'description']
    if tag == 'var':
        return ('var', elemento.text.strip())
    if tag == 'not':
        return ('not', _ler_expr(filhos[0]))
    if tag in ('conj', 'disj'):
        return (tag, [_ler_expr(f) for f in filhos])
    if tag in ('imp', 'eq'):
        return (tag, _ler_expr(filhos[0]), _ler_expr(filhos[1]))
    raise ValueError(f'operador desconhecido: {tag}')


def _ler_feature(elemento, pai, features):
    nome = elemento.get('name')
    if nome in features:
        raise ValueError(f'feature repetida: {nome}')
    grupo = elemento.tag if elemento.tag in GRUPOS else 'and'
    features[nome] = Feature(nome, pai, grupo, elemento.get('abstract') == 'true', elemento.get('mandatory') == 'true')
    if pai:
        features[pai].filhos.append(nome)
    for filho in elemento:
        if filho.tag in GRUPOS or filho.tag == 'feature':
            _ler_feature(filho, nome, features)
    return nome


def carregar(origem):
    texto = Path(origem).read_text(encoding='utf-8') if Path(str(origem)).exists() else origem
    raiz_xml = ET.fromstring(texto)
    features = {}
    struct = raiz_xml.find('struct')
    raiz = _ler_feature(list(struct)[0], None, features)
    restricoes = []
    for regra in raiz_xml.find('constraints') if raiz_xml.find('constraints') is not None else []:
        descricao = regra.findtext('description', default='').strip()
        corpo = [e for e in regra if e.tag != 'description'][0]
        restricoes.append((descricao, _ler_expr(corpo)))
    return Modelo(raiz, features, restricoes)


def _variaveis(expr):
    if expr[0] == 'var':
        return {expr[1]}
    if expr[0] == 'not':
        return _variaveis(expr[1])
    if expr[0] in ('conj', 'disj'):
        return set().union(*(_variaveis(e) for e in expr[1]))
    return _variaveis(expr[1]) | _variaveis(expr[2])


def _operadores(expr):
    if expr[0] == 'var':
        return set()
    if expr[0] == 'not':
        return {'not'} | _operadores(expr[1])
    if expr[0] in ('conj', 'disj'):
        return {expr[0]}.union(*(_operadores(e) for e in expr[1]))
    return {expr[0]} | _operadores(expr[1]) | _operadores(expr[2])


class _Cnf:
    def __init__(self, nomes):
        self.ids = {n: i + 1 for i, n in enumerate(nomes)}
        self.proximo = len(nomes) + 1
        self.clausulas = []

    def novo(self):
        self.proximo += 1
        return self.proximo - 1

    def codificar(self, expr):
        tipo = expr[0]
        if tipo == 'var':
            return self.ids[expr[1]]
        if tipo == 'not':
            return -self.codificar(expr[1])
        if tipo == 'imp':
            return self.codificar(('disj', [('not', expr[1]), expr[2]]))
        if tipo == 'eq':
            return self.codificar(('conj', [('imp', expr[1], expr[2]), ('imp', expr[2], expr[1])]))
        literais = [self.codificar(e) for e in expr[1]]
        t = self.novo()
        if tipo == 'conj':
            self.clausulas += [[-t, l] for l in literais]
            self.clausulas.append([t] + [-l for l in literais])
        else:
            self.clausulas.append([-t] + literais)
            self.clausulas += [[t, -l] for l in literais]
        return t


def _cnf(modelo, ignorar=None):
    cnf = _Cnf(list(modelo.features))
    v = cnf.ids
    cnf.clausulas.append([v[modelo.raiz]])
    for f in modelo.features.values():
        if f.pai:
            cnf.clausulas.append([-v[f.nome], v[f.pai]])
        filhos = [v[c] for c in f.filhos]
        if not filhos:
            continue
        if f.grupo == 'and':
            for c in f.filhos:
                if modelo.features[c].obrigatoria:
                    cnf.clausulas.append([-v[f.nome], v[c]])
        else:
            cnf.clausulas.append([-v[f.nome]] + filhos)
            if f.grupo == 'alt':
                for i, a in enumerate(filhos):
                    for b in filhos[i + 1:]:
                        cnf.clausulas.append([-a, -b])
    for i, (_, expr) in enumerate(modelo.restricoes):
        if i != ignorar:
            cnf.clausulas.append([cnf.codificar(expr)])
    return cnf


def _resolve(clausulas, suposicoes=()):
    with Glucose3(bootstrap_with=clausulas) as s:
        return s.solve(assumptions=list(suposicoes))


def analisar(modelo):
    cnf = _cnf(modelo)
    v = cnf.ids
    satisfazivel = _resolve(cnf.clausulas)
    mortas = [n for n in modelo.features if not _resolve(cnf.clausulas, [v[n]])]
    falso_opcionais = [
        n for n, f in modelo.features.items()
        if f.pai and not (f.obrigatoria and modelo.features[f.pai].grupo == 'and')
        and n not in mortas and not _resolve(cnf.clausulas, [v[f.pai], -v[n]])
    ]
    redundantes = []
    for i, (descricao, expr) in enumerate(modelo.restricoes):
        sem = _cnf(modelo, ignorar=i)
        negacao = sem.codificar(expr)
        if not _resolve(sem.clausulas + [[-negacao]]):
            redundantes.append(descricao or f'restrição {i + 1}')
    return {'satisfazivel': satisfazivel, 'mortas': mortas,
            'falso_opcionais': falso_opcionais, 'redundantes': redundantes}


def verificar_exigencias(modelo):
    problemas = []
    if len(modelo.restricoes) < 10:
        problemas.append(f'são {len(modelo.restricoes)} restrições; a entrega pede pelo menos 10')
    usados = set().union(*(_operadores(e) for _, e in modelo.restricoes)) if modelo.restricoes else set()
    for op, nome in OPERADORES_EXIGIDOS.items():
        if op not in usados:
            problemas.append(f'nenhuma restrição usa {nome}')
    for _, expr in modelo.restricoes:
        for nome in _variaveis(expr) - set(modelo.features):
            problemas.append(f'restrição cita feature inexistente: {nome}')
    fs = [f for f in modelo.features.values() if f.pai]
    tipos = {
        'Abstract': any(f.abstrata for f in modelo.features.values()),
        'Concrete': any(not f.abstrata for f in modelo.features.values()),
        'Mandatory': any(f.obrigatoria and modelo.features[f.pai].grupo == 'and' for f in fs),
        'Optional': any(not f.obrigatoria and modelo.features[f.pai].grupo == 'and' for f in fs),
        'Or Group': any(f.grupo == 'or' and f.filhos for f in modelo.features.values()),
        'Alternative Group': any(f.grupo == 'alt' and f.filhos for f in modelo.features.values()),
    }
    problemas += [f'o modelo não tem nenhuma feature do tipo {t}' for t, ok in tipos.items() if not ok]
    return problemas


def _avaliar(expr, sel):
    tipo = expr[0]
    if tipo == 'var':
        return expr[1] in sel
    if tipo == 'not':
        return not _avaliar(expr[1], sel)
    if tipo == 'conj':
        return all(_avaliar(e, sel) for e in expr[1])
    if tipo == 'disj':
        return any(_avaliar(e, sel) for e in expr[1])
    if tipo == 'imp':
        return not _avaliar(expr[1], sel) or _avaliar(expr[2], sel)
    return _avaliar(expr[1], sel) == _avaliar(expr[2], sel)


def validar_configuracao(modelo, origem):
    texto = Path(origem).read_text(encoding='utf-8') if Path(str(origem)).exists() else origem
    estado = {e.get('name'): e.get('manual') or e.get('automatic') for e in ET.fromstring(texto).iter('feature')}
    problemas = [f'feature fora do modelo: {n}' for n in estado if n not in modelo.features]
    problemas += [f'feature sem decisão: {n}' for n in modelo.features if estado.get(n) not in ('selected', 'unselected')]
    sel = {n for n, s in estado.items() if s == 'selected'}
    if modelo.raiz not in sel:
        problemas.append('a raiz precisa estar selecionada')
    for f in modelo.features.values():
        if f.nome in sel and f.pai and f.pai not in sel:
            problemas.append(f'{f.nome} selecionada sem o pai {f.pai}')
        if f.nome not in sel or not f.filhos:
            continue
        escolhidos = [c for c in f.filhos if c in sel]
        if f.grupo == 'and':
            problemas += [f'{c} é obrigatória em {f.nome}' for c in f.filhos
                          if modelo.features[c].obrigatoria and c not in sel]
        elif f.grupo == 'or' and not escolhidos:
            problemas.append(f'{f.nome} (Or) precisa de ao menos um filho')
        elif f.grupo == 'alt' and len(escolhidos) != 1:
            problemas.append(f'{f.nome} (Alternative) precisa de exatamente um filho, tem {len(escolhidos)}')
    for descricao, expr in modelo.restricoes:
        if not _avaliar(expr, sel):
            problemas.append(f'viola {descricao}')
    return problemas


def main(argv):
    modelo = carregar(argv[1])
    analise = analisar(modelo)
    problemas = verificar_exigencias(modelo)
    if not analise['satisfazivel']:
        problemas.append('o modelo não tem nenhuma configuração válida (void)')
    problemas += [f'feature morta: {n}' for n in analise['mortas']]
    problemas += [f'feature falsamente opcional: {n}' for n in analise['falso_opcionais']]
    problemas += [f'restrição redundante: {d}' for d in analise['redundantes']]
    for config in argv[2:]:
        problemas += [f'{Path(config).name}: {p}' for p in validar_configuracao(modelo, config)]
    fs = list(modelo.features.values())
    print(f'features: {len(fs)} (abstratas {sum(f.abstrata for f in fs)}, concretas {sum(not f.abstrata for f in fs)})')
    print(f'restrições: {len(modelo.restricoes)}')
    for p in problemas:
        print('PROBLEMA:', p)
    print('OK' if not problemas else f'{len(problemas)} problema(s)')
    return 0 if not problemas else 1


if __name__ == '__main__':
    sys.exit(main(sys.argv))
