# =========================================================
# BUILD STAGE
# =========================================================
FROM maven:3.9.16-eclipse-temurin-25 AS builder

WORKDIR /app

# Parent Maven POM
COPY pom.xml .

# Copy all Maven modules
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY discovery-server/src discovery-server/src

COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src

COPY course-service/pom.xml course-service/pom.xml
COPY course-service/src course-service/src

COPY learner-service/pom.xml learner-service/pom.xml
COPY learner-service/src learner-service/src

COPY enrollment-service/pom.xml enrollment-service/pom.xml
COPY enrollment-service/src enrollment-service/src

COPY notification-service/pom.xml notification-service/pom.xml
COPY notification-service/src notification-service/src

COPY resource-service/pom.xml resource-service/pom.xml
COPY resource-service/src resource-service/src

# Build all Maven modules
RUN mvn clean package -DskipTests


# =========================================================
# DISCOVERY SERVER
# =========================================================
FROM eclipse-temurin:25-jre AS discovery-server

WORKDIR /app

COPY --from=builder /app/discovery-server/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]


# =========================================================
# API GATEWAY
# =========================================================
FROM eclipse-temurin:25-jre AS api-gateway

WORKDIR /app

COPY --from=builder /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


# =========================================================
# COURSE SERVICE
# =========================================================
FROM eclipse-temurin:25-jre AS course-service

WORKDIR /app

COPY --from=builder /app/course-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]


# =========================================================
# LEARNER SERVICE
# =========================================================
FROM eclipse-temurin:25-jre AS learner-service

WORKDIR /app

COPY --from=builder /app/learner-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]


# =========================================================
# ENROLLMENT SERVICE
# =========================================================
FROM eclipse-temurin:25-jre AS enrollment-service

WORKDIR /app

COPY --from=builder /app/enrollment-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]


# =========================================================
# NOTIFICATION SERVICE
# =========================================================
FROM eclipse-temurin:25-jre AS notification-service

WORKDIR /app

COPY --from=builder /app/notification-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]


# =========================================================
# RESOURCE SERVICE
# =========================================================
FROM eclipse-temurin:25-jre AS resource-service

WORKDIR /app

COPY --from=builder /app/resource-service/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
