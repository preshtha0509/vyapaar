FROM node:22-alpine AS frontend-build

WORKDIR /workspace/frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend-build

WORKDIR /workspace

COPY backend/ ./backend/
RUN rm -rf /workspace/backend/src/main/resources/static/*
COPY --from=frontend-build /workspace/frontend/dist/ /workspace/backend/src/main/resources/static/

WORKDIR /workspace/backend
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=backend-build /workspace/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
