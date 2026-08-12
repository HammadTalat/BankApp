# ==========================================
# Stage 1: Build Stage
# ==========================================
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and configuration files first to leverage Docker layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Grant execution permissions to the wrapper
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build the application executable
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Runtime Stage
# ==========================================
FROM eclipse-temurin:25-jre-alpine AS runner
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the generated JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose standard Spring Boot port
EXPOSE 8081

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]