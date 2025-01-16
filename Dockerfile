FROM openjdk:17

RUN mkdir /opt/code &&\
    mkdir /opt/app
ADD ./ /opt/code/
ENV  M2_HOME '/opt/apache-maven-3.9.6'
ENV PATH "$M2_HOME/bin:$PATH"
ARG APP_ENV=$APP_ENV
ENV APP_ENV=prod

RUN echo "$APP_ENV"
RUN curl https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o apache-maven-3.9.6-bin.tar.gz &&\
    tar xzvf apache-maven-3.9.6-bin.tar.gz -C /opt/ &&\
    rm -rf apache-maven-3.9.6-bin.tar.gz &&\
    cd /opt/code/ &&\
    mvn clean package -P "$APP_ENV" &&\
    mv /opt/code/target/*.jar /opt/app/ &&\
    mv /opt/app/*.jar /opt/app/app.jar &&\
    rm -rfv /opt/code &&\
    chmod 777 -R /opt/app/

ENTRYPOINT ["java","-jar","/opt/app/app.jar"]

