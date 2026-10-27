FROM eclipse-temurin:21-jdk

WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x ./mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src src

EXPOSE 8088

CMD ["./mvnw", "spring-boot:run"]
