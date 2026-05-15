# Dockerfile pour Micro Investment Portfolio - Java 21 + Spring Boot 3.5.0

# Stage 1: Build de l'application
FROM maven:3.9.6-amazoncorretto-21 AS builder

# Définit le répertoire de travail
WORKDIR /app

# Copie les fichiers de configuration Maven
COPY pom.xml .
COPY src ./src

# Build de l'application avec les profils Maven
RUN mvn clean package -DskipTests -Dspring-boot.build-info.enabled=true

# Stage 2: Image de runtime
FROM eclipse-temurin:21-jdk-jammy AS runtime

# Installation des dépendances système
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Crée un utilisateur non-root pour la sécurité
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Définit le répertoire de travail
WORKDIR /app

# Crée les dossiers nécessaires avec les bonnes permissions
RUN mkdir -p /app/logs  && \
    chown -R spring:spring /app

# Copie le JAR depuis le stage de build
COPY --from=builder /app/target/*.jar micro-investment.jar

# Change le propriétaire du fichier
RUN chown spring:spring micro-investment.jar

# Utilise l'utilisateur non-root
USER spring:spring

# Expose le port configuré
EXPOSE 6100

# Variables d'environnement pour optimiser la JVM Java 21
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true"



# Point d'entrée pour démarrer l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar micro-investment.jar"]