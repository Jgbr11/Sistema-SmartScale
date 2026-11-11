<#if !c2j.isComponent(clazz.identifierProperty)>
<#assign entidade = pojo.getDeclarationName()>
<#assign rota = entidade?uncap_first>
<#assign tipoId = pojo.getJavaTypeName(clazz.identifierProperty, true)>
<#if tipoId == "long"><#assign tipoId = "Long"><#elseif tipoId == "int"><#assign tipoId = "Integer"></#if>
<#assign relacoes = []>
<#list pojo.getAllPropertiesIterator() as campo>
<#if c2h.isManyToOne(campo)>
<#assign tipo = pojo.getJavaTypeName(campo, true)>
<#if !relacoes?seq_contains(tipo)><#assign relacoes = relacoes + [tipo]></#if>
</#if>
</#list>
${pojo.getPackageDeclaration()}
// Gerado pelo Hibernate Tools a partir da tabela ${clazz.table.name}

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/${rota}")
public class ${entidade}Controller {

    private final ${entidade}Repository repositorio;
<#list relacoes as r>
    private final ${r}Repository repositorio${r};
</#list>

    public ${entidade}Controller(${entidade}Repository repositorio<#list relacoes as r>, ${r}Repository repositorio${r}</#list>) {
        this.repositorio = repositorio;
<#list relacoes as r>
        this.repositorio${r} = repositorio${r};
</#list>
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("registros", repositorio.findAll());
        return "${entidade}/lista";
    }

    @GetMapping({"/novo", "/{id}"})
    public String formulario(@PathVariable(required = false) ${tipoId} id, Model model) {
        ${entidade} registro = id == null ? new ${entidade}() : repositorio.findById(id).orElseThrow();
        model.addAttribute("registro", registro);
<#list relacoes as r>
        model.addAttribute("opcoes${r}", repositorio${r}.findAll());
</#list>
        return "${entidade}/formulario";
    }

    @PostMapping
    public String salvar(@ModelAttribute("registro") ${entidade} registro) {
        repositorio.save(registro);
        return "redirect:/${rota}";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable ${tipoId} id) {
        repositorio.deleteById(id);
        return "redirect:/${rota}";
    }
}
</#if>
