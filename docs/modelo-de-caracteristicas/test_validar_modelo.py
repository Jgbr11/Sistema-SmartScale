import unittest
from pathlib import Path

from validar_modelo import analisar, carregar, validar_configuracao, verificar_exigencias

AQUI = Path(__file__).parent

MINIMO = """<featureModel><struct>
<and abstract="true" mandatory="true" name="Raiz">
  <alt abstract="true" mandatory="true" name="Banco"><feature name="A"/><feature name="B"/></alt>
  <feature name="C"/>
  <feature mandatory="true" name="D"/>
</and></struct><constraints>{regras}</constraints></featureModel>"""


def modelo(regras=''):
    return carregar(MINIMO.format(regras=regras))


def regra(corpo):
    return f'<rule><description>teste</description>{corpo}</rule>'


class AnaliseTest(unittest.TestCase):
    def test_modelo_sem_restricoes_e_valido(self):
        a = analisar(modelo())
        self.assertTrue(a['satisfazivel'])
        self.assertEqual([], a['mortas'])
        self.assertEqual([], a['falso_opcionais'])

    def test_detecta_feature_morta(self):
        a = analisar(modelo(regra('<imp><var>A</var><var>B</var></imp>')))
        self.assertEqual(['A'], a['mortas'])

    def test_detecta_falso_opcional(self):
        a = analisar(modelo(regra('<imp><var>D</var><var>C</var></imp>')))
        self.assertIn('C', a['falso_opcionais'])

    def test_detecta_restricao_redundante(self):
        a = analisar(modelo(regra('<imp><var>A</var><not><var>B</var></not></imp>')))
        self.assertEqual(['teste'], a['redundantes'])

    def test_modelo_contraditorio_nao_e_satisfazivel(self):
        a = analisar(modelo(regra('<not><var>D</var></not>')))
        self.assertFalse(a['satisfazivel'])


class ExigenciasTest(unittest.TestCase):
    def test_cobra_dez_restricoes_e_os_quatro_operadores(self):
        problemas = verificar_exigencias(modelo(regra('<imp><var>C</var><var>A</var></imp>')))
        self.assertIn('são 1 restrições; a entrega pede pelo menos 10', problemas)
        self.assertIn('nenhuma restrição usa And', problemas)
        self.assertIn('nenhuma restrição usa Or', problemas)
        self.assertIn('nenhuma restrição usa Not', problemas)
        self.assertNotIn('nenhuma restrição usa Implies', problemas)

    def test_cobra_or_group(self):
        problemas = verificar_exigencias(modelo())
        self.assertIn('o modelo não tem nenhuma feature do tipo Or Group', problemas)

    def test_acusa_variavel_inexistente(self):
        problemas = verificar_exigencias(modelo(regra('<imp><var>C</var><var>Z</var></imp>')))
        self.assertIn('restrição cita feature inexistente: Z', problemas)


class ConfiguracaoTest(unittest.TestCase):
    def config(self, selecionadas):
        nomes = ['Raiz', 'Banco', 'A', 'B', 'C', 'D']
        linhas = ''.join(
            f'<feature manual="{"selected" if n in selecionadas else "unselected"}" name="{n}"/>' for n in nomes)
        return f'<configuration>{linhas}</configuration>'

    def test_configuracao_valida(self):
        self.assertEqual([], validar_configuracao(modelo(), self.config({'Raiz', 'Banco', 'A', 'D'})))

    def test_alternativa_com_dois_filhos(self):
        problemas = validar_configuracao(modelo(), self.config({'Raiz', 'Banco', 'A', 'B', 'D'}))
        self.assertIn('Banco (Alternative) precisa de exatamente um filho, tem 2', problemas)

    def test_obrigatoria_ausente_e_restricao_violada(self):
        m = modelo(regra('<imp><var>C</var><var>B</var></imp>'))
        problemas = validar_configuracao(m, self.config({'Raiz', 'Banco', 'A', 'C'}))
        self.assertIn('D é obrigatória em Raiz', problemas)
        self.assertIn('viola teste', problemas)


class ModeloRealTest(unittest.TestCase):
    def test_modelo_do_smartscale_cumpre_a_entrega(self):
        m = carregar(AQUI / 'SmartScale' / 'model.xml')
        a = analisar(m)
        self.assertEqual([], verificar_exigencias(m))
        self.assertTrue(a['satisfazivel'])
        self.assertEqual([], a['mortas'])
        self.assertEqual([], a['falso_opcionais'])
        self.assertEqual([], a['redundantes'])

    def test_configuracoes_dos_produtos_sao_validas(self):
        m = carregar(AQUI / 'SmartScale' / 'model.xml')
        for config in sorted((AQUI / 'SmartScale' / 'configs').glob('*.xml')):
            with self.subTest(config=config.name):
                self.assertEqual([], validar_configuracao(m, config))


if __name__ == '__main__':
    unittest.main()
