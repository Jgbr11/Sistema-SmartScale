import { readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import {
  AlignmentType, BorderStyle, Document, Footer, HeadingLevel, ImageRun, LevelFormat, Packer,
  PageNumber, Paragraph, ShadingType, Table, TableCell, TableLayoutType, TableRow, TextRun, WidthType,
} from "docx";
import { marked } from "marked";

const LARGURA_UTIL = 9638;
const LARGURA_IMAGEM_MAX = 640;
const ALTURA_IMAGEM_MAX = 900;
const LARGURA_CARACTERE = 120;
const PALAVRA_MAXIMA = 18;
const FONTE = "Arial";

export const CORES_SITUACAO = {
  Verde: "C6EFCE",
  Amarelo: "FFEB9C",
  Vermelho: "FFC7CE",
};

const SITUACAO = /^(?:🟢|🟡|🔴)?\s*(Verde|Amarelo|Vermelho)$/u;

function decodificar(texto) {
  return texto
    .replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"').replace(/&#39;/g, "'");
}

function trechos(tokens, estilo = {}) {
  const runs = [];
  for (const t of tokens ?? []) {
    if (t.type === "strong") runs.push(...trechos(t.tokens, { ...estilo, bold: true }));
    else if (t.type === "em") runs.push(...trechos(t.tokens, { ...estilo, italics: true }));
    else if (t.type === "codespan") runs.push(new TextRun({ text: decodificar(t.text), font: "Consolas", ...estilo }));
    else if (t.type === "link") runs.push(...trechos(t.tokens, { ...estilo, color: "1F4E79", underline: {} }));
    else if (t.type === "br") runs.push(new TextRun({ break: 1 }));
    else if (t.tokens) runs.push(...trechos(t.tokens, estilo));
    else runs.push(new TextRun({ text: decodificar(t.text ?? t.raw ?? ""), font: FONTE, ...estilo }));
  }
  return runs;
}

function tamanhoPng(buffer) {
  return { largura: buffer.readUInt32BE(16), altura: buffer.readUInt32BE(20) };
}

function imagem(token, pastaBase) {
  const dados = readFileSync(resolve(pastaBase, decodeURI(token.href)));
  const { largura, altura } = tamanhoPng(dados);
  const escala = Math.min(1, LARGURA_IMAGEM_MAX / largura, ALTURA_IMAGEM_MAX / altura);
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 120, after: 120 },
    children: [new ImageRun({
      type: "png",
      data: dados,
      transformation: { width: Math.round(largura * escala), height: Math.round(altura * escala) },
    })],
  });
}

function celula(token, largura, cabecalho) {
  const texto = decodificar(token.text).trim();
  const situacao = texto.match(SITUACAO);
  const fundo = cabecalho ? "D9E2F3" : situacao ? CORES_SITUACAO[situacao[1]] : undefined;
  const filhos = situacao
    ? [new TextRun({ text: situacao[1], bold: true, font: FONTE })]
    : trechos(token.tokens, cabecalho ? { bold: true } : {});
  return new TableCell({
    width: { size: largura, type: WidthType.DXA },
    shading: fundo ? { type: ShadingType.CLEAR, color: "auto", fill: fundo } : undefined,
    margins: { top: 60, bottom: 60, left: 100, right: 100 },
    children: [new Paragraph({ children: filhos })],
  });
}

export function largurasDasColunas(colunasDeTexto) {
  const soma = (lista) => lista.reduce((a, b) => a + b, 0);
  const medidas = colunasDeTexto.map((textos) => {
    const maiorPalavra = Math.max(...textos.flatMap((t) => t.split(/\s+/)).map((p) => p.length));
    const maiorTexto = Math.max(...textos.map((t) => t.length));
    return {
      base: (Math.min(maiorPalavra, PALAVRA_MAXIMA) + 3) * LARGURA_CARACTERE,
      folga: Math.max(0, Math.min(maiorTexto, 160) - maiorPalavra),
    };
  });
  const bases = medidas.map((m) => m.base);
  const sobra = LARGURA_UTIL - soma(bases);
  const folgas = medidas.map((m) => m.folga);
  const partes = soma(folgas) > 0 ? folgas : medidas.map(() => 1);
  const larguras = sobra < 0
    ? bases.map((b) => Math.floor((b * LARGURA_UTIL) / soma(bases)))
    : bases.map((b, i) => Math.floor(b + (sobra * partes[i]) / soma(partes)));
  larguras[larguras.length - 1] += LARGURA_UTIL - soma(larguras);
  return larguras;
}

function tabela(token) {
  const colunasDeTexto = token.header.map((cabecalho, i) =>
    [cabecalho, ...token.rows.map((linha) => linha[i])].map((c) => decodificar(c.text).trim()));
  const larguras = largurasDasColunas(colunasDeTexto);
  const linhas = [
    new TableRow({ tableHeader: true, children: token.header.map((c, i) => celula(c, larguras[i], true)) }),
    ...token.rows.map((linha) => new TableRow({ children: linha.map((c, i) => celula(c, larguras[i], false)) })),
  ];
  return new Table({
    width: { size: LARGURA_UTIL, type: WidthType.DXA },
    columnWidths: larguras,
    layout: TableLayoutType.FIXED,
    rows: linhas,
  });
}

const NIVEL = { 1: HeadingLevel.HEADING_1, 2: HeadingLevel.HEADING_2, 3: HeadingLevel.HEADING_3 };

const temImagem = (t) => t?.type === "paragraph" && t.tokens.some((x) => x.type === "image");

function blocos(tokens, pastaBase, nivelLista = 0) {
  const saida = [];
  const conteudo = tokens.filter((t) => t.type !== "space");
  for (const [i, t] of conteudo.entries()) {
    if (t.type === "heading") {
      saida.push(new Paragraph({ heading: NIVEL[Math.min(t.depth, 3)], children: trechos(t.tokens) }));
    } else if (t.type === "paragraph") {
      const imagens = t.tokens.filter((x) => x.type === "image");
      if (imagens.length) imagens.forEach((img) => saida.push(imagem(img, pastaBase)));
      else saida.push(new Paragraph({ spacing: { after: 120 }, keepNext: temImagem(conteudo[i + 1]), children: trechos(t.tokens) }));
    } else if (t.type === "list") {
      for (const item of t.items) {
        const [primeiro, ...resto] = item.tokens;
        const texto = primeiro?.tokens ?? [];
        saida.push(new Paragraph({
          numbering: { reference: t.ordered ? "numeros" : "marcadores", level: nivelLista },
          children: trechos(texto),
        }));
        saida.push(...blocos(resto.filter((x) => x.type === "list"), pastaBase, nivelLista + 1));
      }
    } else if (t.type === "table") {
      saida.push(tabela(t), new Paragraph({ spacing: { after: 120 }, children: [] }));
    } else if (t.type === "blockquote") {
      for (const p of t.tokens.filter((x) => x.type === "paragraph")) {
        saida.push(new Paragraph({
          indent: { left: 360 },
          border: { left: { style: BorderStyle.SINGLE, size: 12, color: "8EAADB", space: 8 } },
          children: trechos(p.tokens, { italics: true }),
        }));
      }
    } else if (t.type === "code") {
      for (const linha of t.text.split("\n")) {
        saida.push(new Paragraph({
          shading: { type: ShadingType.CLEAR, color: "auto", fill: "F2F2F2" },
          spacing: { after: 0 },
          children: [new TextRun({ text: linha || " ", font: "Consolas", size: 18 })],
        }));
      }
      saida.push(new Paragraph({ children: [] }));
    }
  }
  return saida;
}

const ESTILO_TITULO = (tamanho, antes) => ({
  run: { font: FONTE, size: tamanho, bold: true, color: "1F3864" },
  paragraph: { spacing: { before: antes, after: 120 }, keepNext: true },
});

export function montarDocumento(markdown, pastaBase) {
  const tokens = marked.lexer(markdown);
  return new Document({
    styles: {
      default: {
        document: { run: { font: FONTE, size: 22 } },
        heading1: ESTILO_TITULO(32, 360),
        heading2: ESTILO_TITULO(26, 280),
        heading3: ESTILO_TITULO(23, 200),
      },
    },
    numbering: {
      config: ["marcadores", "numeros"].map((reference) => ({
        reference,
        levels: [0, 1, 2].map((level) => ({
          level,
          format: reference === "numeros" ? LevelFormat.DECIMAL : LevelFormat.BULLET,
          text: reference === "numeros" ? `%${level + 1}.` : ["•", "◦", "▪"][level],
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720 * (level + 1), hanging: 360 } } },
        })),
      })),
    },
    sections: [{
      properties: { page: { margin: { top: 1134, bottom: 1134, left: 1134, right: 1134 } } },
      footers: {
        default: new Footer({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [new TextRun({ children: [PageNumber.CURRENT], font: FONTE, size: 18 })],
          })],
        }),
      },
      children: blocos(tokens, pastaBase),
    }],
  });
}

export async function gerar(origem, destino) {
  const documento = montarDocumento(readFileSync(origem, "utf8"), dirname(origem));
  writeFileSync(destino, await Packer.toBuffer(documento));
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const raiz = resolve(dirname(fileURLToPath(import.meta.url)), "..", "..", "..");
  const entregas = [
    ["docs/MODELO_DE_CARACTERISTICAS.md", "docs/entregas/01-modelo-de-caracteristicas.docx"],
    ["docs/NIVEIS_DE_REUSO_MPSBR.md", "docs/entregas/02-niveis-de-reuso-gru-dru.docx"],
  ];
  for (const [origem, destino] of entregas) {
    await gerar(resolve(raiz, origem), resolve(raiz, destino));
    console.log(`gerado: ${destino}`);
  }
}
