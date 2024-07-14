# Step 1: Build custom JRE image
FROM amazoncorretto:17.0.3-alpine as corretto-jdk

# Install binutils needed for strip-debug
RUN apk add --no-cache binutils

# Set environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto

# Build custom JRE with required modules
RUN /usr/lib/jvm/java-17-amazon-corretto/bin/jlink \
    --verbose \
    --add-modules java.base,java.management,java.naming,java.net.http,java.security.jgss,java.security.sasl,java.sql,jdk.httpserver,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /jre

# Step 2: Install OpenJDK and prepare tools for analysis
RUN apk add --no-cache openjdk17-jdk

# Step 3: Analyze application JAR and determine required modules
FROM corretto-jdk as analyzer

# Copy the application JAR file
COPY ./target/payroll-0.0.1-SNAPSHOT.jar /app/app.jar

# Extract module dependencies from the JAR using jdeps
RUN /usr/lib/jvm/java-17-amazon-corretto/bin/jdeps --list-deps /app/app.jar | grep -v "java.base" | sort | uniq > /tmp/modules.txt

# Step 4: Build application image
FROM alpine:latest

# Copy custom JRE from the build stage
COPY --from=corretto-jdk /jre /jre

# Copy module dependencies file from analyzer stage
COPY --from=analyzer /tmp/modules.txt /tmp/modules.txt

# Set environment variables
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Add a non-root user for running the application
RUN adduser -D -u 1000 appuser

# Create application directory and set permissions
RUN mkdir /app && \
    chown appuser /app

# Switch to the non-root user
USER appuser

# Copy the application JAR file
COPY --chown=appuser:appuser ./target/payroll-0.0.1-SNAPSHOT.jar /app/app.jar

# Set the working directory
WORKDIR /app

# Expose the port that the application listens on
#EXPOSE 8080

# Command to run the application
ENTRYPOINT [ "/jre/bin/java", "-jar", "app.jar" ]
