import { useEffect, useRef } from "react";

/**
 * Editor simples de texto rico: permite colar imagem direto (Ctrl+V)
 * ou inserir por arquivo, misturado com texto normal. O conteúdo vira
 * HTML com as imagens embutidas como data URI — mesma lógica da foto
 * do militar, só que dentro do corpo do texto em vez de um campo à parte.
 */
export function RichEditor({ valorInicial, onChange }: { valorInicial: string; onChange: (html: string) => void }) {
  const ref = useRef<HTMLDivElement>(null);
  const iniciou = useRef(false);

  useEffect(() => {
    if (!iniciou.current && ref.current) {
      ref.current.innerHTML = valorInicial || "";
      iniciou.current = true;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function emitirMudanca() {
    if (ref.current) onChange(ref.current.innerHTML);
  }

  function inserirImagem(file: File) {
    const leitor = new FileReader();
    leitor.onload = () => {
      document.execCommand("insertHTML", false, `<img src="${leitor.result}" style="max-width:100%;display:block;margin:10px 0;border-radius:4px;" />`);
      emitirMudanca();
    };
    leitor.readAsDataURL(file);
  }

  function aoColar(e: React.ClipboardEvent<HTMLDivElement>) {
    const itens = e.clipboardData?.items;
    if (!itens) return;
    for (const item of itens) {
      if (item.type.startsWith("image/")) {
        e.preventDefault();
        const arquivo = item.getAsFile();
        if (arquivo) inserirImagem(arquivo);
        return;
      }
    }
    setTimeout(emitirMudanca, 0);
  }

  function comando(cmd: string) {
    ref.current?.focus();
    document.execCommand(cmd);
    emitirMudanca();
  }

  return (
    <div>
      <div style={{ display: "flex", gap: 6, marginBottom: 8, flexWrap: "wrap" }}>
        <button type="button" className="btn btn-outline" onMouseDown={(e) => e.preventDefault()} onClick={() => comando("bold")} style={{ fontWeight: 700 }}>N</button>
        <button type="button" className="btn btn-outline" onMouseDown={(e) => e.preventDefault()} onClick={() => comando("italic")} style={{ fontStyle: "italic" }}>I</button>
        <button type="button" className="btn btn-outline" onMouseDown={(e) => e.preventDefault()} onClick={() => comando("insertUnorderedList")}>Lista</button>
        <button
          type="button"
          className="btn btn-outline"
          onMouseDown={(e) => e.preventDefault()}
          onClick={() => { ref.current?.focus(); document.execCommand("formatBlock", false, "h3"); emitirMudanca(); }}
        >
          Título
        </button>
        <label className="btn btn-outline" style={{ cursor: "pointer" }}>
          Inserir imagem
          <input
            type="file"
            accept="image/*"
            style={{ display: "none" }}
            onChange={(e) => {
              const arquivo = e.target.files?.[0];
              if (arquivo) inserirImagem(arquivo);
              e.target.value = "";
            }}
          />
        </label>
      </div>
      <p style={{ fontSize: 11, color: "var(--grey)", marginBottom: 8 }}>
        Pode colar uma imagem direto (Ctrl+V) no meio do texto, ou usar o botão "Inserir imagem".
      </p>
      <div
        ref={ref}
        contentEditable
        onInput={emitirMudanca}
        onPaste={aoColar}
        onBlur={emitirMudanca}
        style={{
          minHeight: 220, border: "1px solid var(--border)", borderRadius: 5, padding: 12,
          fontSize: 13.5, lineHeight: 1.5, background: "#fff", outline: "none",
        }}
      />
    </div>
  );
}
