# ---- Build (contexto: raiz do repositório) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY smartscale-core/pom.xml smartscale-core/
COPY backend/pom.xml backend/
RUN mvn -q -B -pl backend -am dependency:go-offline -DexcludeGroupIds=br.com.smartscale
COPY smartscale-core/src smartscale-core/src
COPY backend/src backend/src
RUN mvn -q -B -pl backend -am package

# ---- Execução ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/backend/target/milscale-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=mysql"]
