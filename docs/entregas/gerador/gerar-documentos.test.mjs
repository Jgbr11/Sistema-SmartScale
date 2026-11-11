import assert from "node:assert/strict";
import { mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { test } from "node:test";
import { Packer } from "docx";
import JSZip from "jszip";
import { CORES_SITUACAO, largurasDasColunas, montarDocumento } from "./gerar-documentos.mjs";

const PNG_2X1 = Buffer.from(
  "iVBORw0KGgoAAAANSUhEUgAAAAIAAAABCAYAAAD0In+KAAAAEUlEQVR4nGNgYGD4z8DAwMAAAAQAAWnF0Q8AAAAASUVORK5CYII=",
  "base64",
);

async function xmlDe(markdown, arquivos = {}) {
  const pasta = mkdtempSync(join(tmpdir(), "gerador-"));
  for (const [nome, dados] of Object.entries(arquivos)) writeFileSync(join(pasta, nome), dados);
  const zip = await JSZip.loadAsync(await Packer.toBuffer(montarDocumento(markdown, pasta)));
  return {
    documento: await zip.file("word/document.xml").async("string"),
    midias: Object.keys(zip.files).filter((n) => n.startsWith("word/media/")),
  };
}

test("pinta as células de situação com a cor certa e tira o emoji", async () => {
  const { documento } = await xmlDe(
    "| Item | Situação |\n|---|---|\n| GRU 1 | 🟢 Verde |\n| GRU 2 | Amarelo |\n| GRU 3 | 🔴 Vermelho |\n",
  );
  for (const cor of Object.values(CORES_SITUACAO)) assert.match(documento, new RegExp(`w:fill="${cor}"`));
  assert.doesNotMatch(documento, /🟢|🔴/u);
  assert.match(documento, />Verde</);
});

test("títulos viram estilos de título do Word", async () => {
  const { documento } = await xmlDe("# Título\n\n## Seção\n\n### Subseção\n");
  assert.match(documento, /w:pStyle w:val="Heading1"/);
  assert.match(documento, /w:pStyle w:val="Heading2"/);
  assert.match(documento, /w:pStyle w:val="Heading3"/);
});

test("imagem do markdown é embutida no documento", async () => {
  const { documento, midias } = await xmlDe("![modelo](modelo.png)\n", { "modelo.png": PNG_2X1 });
  assert.equal(midias.length, 1);
  assert.match(documento, /<pic:pic/);
});

test("negrito, código e listas são preservados", async () => {
  const { documento } = await xmlDe("Texto **forte** e `codigo`.\n\n- um\n- dois\n");
  assert.match(documento, /<w:b\/>/);
  assert.match(documento, /Consolas/);
  assert.equal((documento.match(/<w:numPr>/g) ?? []).length, 2);
});

test("coluna curta não quebra palavra e a tabela ocupa a largura útil", () => {
  const larguras = largurasDasColunas([
    ["Código", "R01", "R02"],
    ["Operadores", "Implies, And", "Implies"],
    ["Justificativa", "A troca precisa conferir o descanso de quem assume e avisar o substituto pelo sistema"],
  ]);
  assert.equal(larguras.reduce((a, b) => a + b, 0), 9638);
  assert.ok(larguras[1] >= 12 * 115, `Operadores ficou com ${larguras[1]}`);
  assert.ok(larguras[2] > larguras[1], "a coluna de texto longo recebe a sobra");
});

test("imagem alta é reduzida para caber na altura da página", async () => {
  const alta = Buffer.from(PNG_2X1);
  alta.writeUInt32BE(400, 16);
  alta.writeUInt32BE(3000, 20);
  const { documento } = await xmlDe("![alta](alta.png)\n", { "alta.png": alta });
  const [, largura, altura] = documento.match(/<wp:extent cx="(\d+)" cy="(\d+)"/);
  assert.ok(Number(altura) <= 900 * 9525, `altura ${altura} passa da página`);
  assert.equal(Math.round((Number(largura) / Number(altura)) * 3000), 400, "mantém a proporção");
});

test("o parágrafo antes de uma imagem fica na mesma página que ela", async () => {
  const { documento } = await xmlDe("Legenda da figura\n\n![figura](figura.png)\n\nTexto solto\n", { "figura.png": PNG_2X1 });
  const paragrafos = documento.split("<w:p>").slice(1);
  const legenda = paragrafos.find((p) => p.includes("Legenda da figura"));
  const solto = paragrafos.find((p) => p.includes("Texto solto"));
  assert.match(legenda, /<w:keepNext\/>/);
  assert.doesNotMatch(solto, /<w:keepNext\/>/);
});
