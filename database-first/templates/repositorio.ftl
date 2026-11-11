${pojo.getPackageDeclaration()}
// Gerado pelo Hibernate Tools a partir da tabela ${clazz.table.name}

import org.springframework.data.jpa.repository.JpaRepository;

<#assign entidade = pojo.getDeclarationName()>
<#assign tipoId = pojo.getJavaTypeName(clazz.identifierProperty, true)>
<#if tipoId == "long"><#assign tipoId = "Long"><#elseif tipoId == "int"><#assign tipoId = "Integer"></#if>
public interface ${entidade}Repository extends JpaRepository<${entidade}, ${tipoId}> {
}
