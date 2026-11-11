<#if !c2j.isComponent(clazz.identifierProperty)>
<#assign entidade = pojo.getDeclarationName()>
<#assign rota = entidade?uncap_first>
<#assign id = clazz.identifierProperty.name>
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>${entidade}</title>
  <link rel="stylesheet" href="/estilo.css">
</head>
<body>
<nav><a href="/">Todas as tabelas</a></nav>
<h1>${entidade}</h1>
<p class="origem">Tela gerada a partir da tabela <code>${clazz.table.name}</code></p>
<a class="botao" href="/${rota}/novo">Novo registro</a>
<div class="rolagem">
<table>
  <thead>
  <tr>
    <th>${id}</th>
<#list pojo.getAllPropertiesIterator() as campo>
<#if campo.name != id && !c2h.isCollection(campo)>
    <th>${campo.name}</th>
</#if>
</#list>
    <th></th>
  </tr>
  </thead>
  <tbody>
  <tr th:each="r : *{registros}">
    <td th:text="*{r.${id}}"></td>
<#list pojo.getAllPropertiesIterator() as campo>
<#if campo.name != id && !c2h.isCollection(campo)>
<#if c2h.isManyToOne(campo)>
    <td th:text="*{@rotulo.de(r.${campo.name})}"></td>
<#else>
    <td th:text="*{r.${campo.name}}"></td>
</#if>
</#if>
</#list>
    <td class="acoes">
      <a th:href="@{/${rota}/{id}(id=*{r.${id}})}">Editar</a>
      <form method="post" th:action="@{/${rota}/{id}/excluir(id=*{r.${id}})}">
        <button type="submit">Excluir</button>
      </form>
    </td>
  </tr>
  </tbody>
</table>
</div>
</body>
</html>
</#if>
