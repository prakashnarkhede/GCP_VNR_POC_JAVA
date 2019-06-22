FROM openjdk:8
MAINTAINER Prakash Narkhede (aec.prakash@gmail.com)
ARG JAR_FILE=target/VnR_Java-0.0.1-SNAPSHOT-jar-with-dependencies.jar
ADD ${JAR_FILE} VnR_Java-0.0.1-SNAPSHOT-jar-with-dependencies.jar
ENTRYPOINT ["java", "-jar", "VnR_Java-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]