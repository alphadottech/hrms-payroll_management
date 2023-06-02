FROM openjdk:11-jre-slim-buster
ENV TZ="Asia/Kolkata"
ARG JAR_FILE=./target/payroll-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /usr/app/payroll.jar
WORKDIR /usr/app
ENTRYPOINT ["java","-jar","payroll.jar"]