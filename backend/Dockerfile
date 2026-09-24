# Build em duas etapas: a primeira compila com Maven (imagem grande,
# fica pra trás), a segunda só carrega o .jar pronto numa imagem JRE
# enxuta - a imagem final não carrega o Maven nem o código-fonte.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copia só o pom.xml primeiro pra cachear as dependências - só baixa de
# novo se o pom.xml mudar, não a cada alteração de código.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Roda os testes automatizados como parte do build - se algum quebrar,
# a imagem nem é gerada (o mesmo `mvn package` que já roda local).
RUN mvn -q -B package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/milscale-backend-*.jar app.jar
EXPOSE 8080
# Perfil mysql fica fixo aqui porque essa imagem é especificamente pra
# rodar contra o container de banco do docker-compose (dev local usa
# H2 direto via `mvn spring-boot:run`, sem passar por este Dockerfile).
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=mysql"]
