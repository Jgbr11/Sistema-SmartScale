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
<nav><a href="/${rota}">Voltar para ${entidade}</a></nav>
<h1>${entidade}</h1>
<p class="origem">Formulário gerado a partir da tabela <code>${clazz.table.name}</code></p>
<form method="post" action="/${rota}">
  <input type="hidden" name="${id}" th:value="*{registro.${id}}">
<#list pojo.getAllPropertiesIterator() as campo>
<#if campo.name != id && !c2h.isCollection(campo)>
<#assign tipo = pojo.getJavaTypeName(campo, true)>
  <label>${campo.name}
<#if c2h.isManyToOne(campo)>
<#assign alvo = md.getEntityBinding(campo.value.referencedEntityName)>
<#assign idAlvo = alvo.identifierProperty.name>
    <select name="${campo.name}">
      <option value="">(nenhum)</option>
      <option th:each="o : *{opcoes${tipo}}" th:value="*{o.${idAlvo}}" th:text="*{@rotulo.de(o)}"
              th:selected="*{registro.${campo.name} != null and registro.${campo.name}.${idAlvo} == o.${idAlvo}}"></option>
    </select>
<#elseif tipo == "boolean" || tipo == "Boolean">
    <input type="hidden" name="_${campo.name}" value="on">
    <input type="checkbox" name="${campo.name}" th:checked="*{registro.${campo.name}}">
<#elseif tipo == "LocalDate">
    <input type="date" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#elseif tipo == "LocalDateTime">
    <input type="datetime-local" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#elseif tipo == "LocalTime">
    <input type="time" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#elseif tipo == "long" || tipo == "Long" || tipo == "int" || tipo == "Integer" || tipo == "short" || tipo == "Short" || tipo == "byte" || tipo == "Byte">
    <input type="number" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#elseif tipo == "BigDecimal" || tipo == "double" || tipo == "Double" || tipo == "float" || tipo == "Float">
    <input type="number" step="any" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#else>
    <input type="text" name="${campo.name}" th:value="*{registro.${campo.name}}">
</#if>
  </label>
</#if>
</#list>
  <button type="submit">Salvar</button>
</form>
</body>
</html>
</#if>
