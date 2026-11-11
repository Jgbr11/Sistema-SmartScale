<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <title>SmartScale · Database First</title>
  <link rel="stylesheet" href="/estilo.css">
</head>
<body>
<h1>SmartScale · Database First</h1>
<p class="origem">Cada link abaixo é uma tabela do banco. As telas e o código foram gerados a partir dela.</p>
<ul class="tabelas">
<#list md.getEntityBindings() as tabela>
<#if !c2j.isComponent(tabela.identifierProperty)>
<#assign entidade = tabela.className?keep_after_last(".")>
  <li><a href="/${entidade?uncap_first}">${entidade}</a> <code>${tabela.table.name}</code></li>
</#if>
</#list>
</ul>
</body>
</html>
