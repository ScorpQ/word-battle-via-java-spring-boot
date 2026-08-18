# WordBattle — tek servis imaji.
# React build ciktisi Spring'in static klasorune kopyalaniyor; uygulama hem
# arayuzu hem API'yi ayni porttan sunuyor. Boylece CORS ayari gerekmiyor.

# ---------------------------------------------------------------------------
# 1) Frontend derlemesi
# ---------------------------------------------------------------------------
FROM node:22-alpine AS frontend

WORKDIR /app

# Once sadece bagimlilik dosyalari: kaynak degisince npm ci tekrar calismasin.
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ---------------------------------------------------------------------------
# 2) Backend derlemesi
# ---------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk AS backend

WORKDIR /app

COPY backend/.mvn ./.mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY backend/src ./src
COPY --from=frontend /app/dist ./src/main/resources/static

RUN ./mvnw -B clean package -DskipTests

# ---------------------------------------------------------------------------
# 3) Calisma imaji — derleme araclari tasinmiyor, sadece JRE + jar
# ---------------------------------------------------------------------------
FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=backend /app/target/*.jar app.jar

# Render dinlenecek portu PORT degiskeniyle veriyor; application.properties
# bunu okuyor, burasi yalnizca belgeleme amacli.
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
