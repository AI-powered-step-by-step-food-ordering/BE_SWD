FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Run the application
EXPOSE 8080

# Environment variables can be set when running the container:
# docker run -e EMAIL_WEBSITE_URL=http://your-server-url:8080 ...
ENV EMAIL_WEBSITE_URL=http://cinezone.info:4458
ENV EMAIL_COMPANY_NAME="Healthy Food API"
ENV EMAIL_SUPPORT_EMAIL=vaultfood99@gmail.com

CMD ["java", "-jar", "target/*.jar"]
