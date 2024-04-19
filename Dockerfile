# Use a smaller base image
FROM alpine:latest AS builder

# Set the working directory
WORKDIR /usr/app

# Download and extract OpenJDK 17
RUN wget -q -O openjdk.tar.gz https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jre_x64_alpine-linux_hotspot_17.0.10_7.tar.gz \
    && tar -xzf openjdk.tar.gz \
    && mv jdk-17.0.10+7-jre* /usr/lib/java-17-openjdk \
    && rm openjdk.tar.gz

# Set JAVA_HOME environment variable
ENV JAVA_HOME /usr/lib/java-17-openjdk

# Add Java to PATH
ENV PATH $JAVA_HOME/bin:$PATH

# Copy the JAR file
ARG JAR_FILE=./target/payroll-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /usr/app/payroll.jar

# Final image
FROM alpine:latest

# Set the working directory
WORKDIR /usr/app

# Copy Java and JAR file from the builder image
COPY --from=builder /usr/lib/java-17-openjdk /usr/lib/java-17-openjdk
COPY --from=builder /usr/app/payroll.jar /usr/app/payroll.jar

# Set the entry point
ENTRYPOINT ["/usr/lib/java-17-openjdk/bin/java", "-jar", "/usr/app/payroll.jar"]