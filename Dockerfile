FROM openjdk:17

RUN mkdir /opt/code &&\
    mkdir /opt/app
ADD ./ /opt/code/
ENV  M2_HOME '/opt/apache-maven-3.9.6'
ENV PATH "$M2_HOME/bin:$PATH"

RUN curl https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o apache-maven-3.9.6-bin.tar.gz &&\
    tar xzvf apache-maven-3.9.6-bin.tar.gz -C /opt/ &&\
    rm -rf apache-maven-3.9.6-bin.tar.gz &&\
    cd /opt/code/ &&\
    mvn clean package &&\
    mv /opt/code/target/app.jar /opt/app/ &&\
    rm -rfv /opt/code &&\
    rm -rfv $M2_HOME &&\
    chmod 777 -R /opt/app/


#RUN curl https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o apache-maven-3.9.6-bin.tar.gz
#RUN tar xzvf apache-maven-3.9.6-bin.tar.gz -C /opt/
#RUN   rm -rf apache-maven-3.9.6-bin.tar.gz
#RUN   cd /opt/code/
#RUN   mvn clean package -v
#RUN   mv /opt/code/target/app.jar /opt/app/
#RUN   rm -rfv /opt/code
#RUN   chmod 777 -R /opt/app/

ENTRYPOINT ["java","-jar","/opt/app/app.jar"]

