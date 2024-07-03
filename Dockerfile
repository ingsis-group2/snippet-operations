FROM gradle:8.7.0-jdk17
RUN mkdir -p /usr/local/newrelic
ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build
EXPOSE 8081
ENTRYPOINT ["java","-javaagent:/usr/local/newrelic/newrelic.jar","-jar","/home/gradle/src/build/libs/snippet-ops-0.0.1-SNAPSHOT.jar"]

