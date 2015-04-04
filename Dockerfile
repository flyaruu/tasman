FROM java:8-jre
MAINTAINER Frank Lyaruu <frank@dexels.com>
ENV DEBIAN_FRONTEND noninteractive 
EXPOSE 8080
ADD http://apache.mirrors.pair.com/felix/org.apache.felix.main.distribution-4.6.1.tar.gz /tmp/
RUN mkdir -p /opt/felix && cd /opt/felix && tar xzvf /tmp/org.apache.felix.main.distribution-4.6.1.tar.gz
RUN ln -s /opt/felix/felix-framework-4.6.1 /opt/felix/current
ADD cache/* /opt/felix/current/bundle/
ADD http://mvnrepository.com/artifact/org.ops4j.pax.web/pax-web-extender-whiteboard/4.1.1 /opt/felix/current/bundle/
#ADD http://central.maven.org/maven2/org/ops4j/pax/logging/pax-logging-api/1.8.2/pax-logging-api-1.8.2.jar /opt/felix/current/bundle/
#ADD http://central.maven.org/maven2/org/ops4j/pax/logging/pax-logging-service/1.8.2/pax-logging-service-1.8.2.jar /opt/felix/current/bundle/
ADD cnf/localrepo/bcpkix/bcpkix-1.52.0.jar /opt/felix/current/bundle/
ADD cnf/localrepo/bcprov/bcprov-1.52.0.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.felix.metatype/org.apache.felix.metatype-1.0.10.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.felix.scr/org.apache.felix.scr-1.8.0.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.felix.configadmin/org.apache.felix.configadmin-1.8.2.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.felix.fileinstall/org.apache.felix.fileinstall-3.5.0.jar /opt/felix/current/bundle/
ADD cnf/localrepo/com.google.gson/com.google.gson-2.3.1.jar /opt/felix/current/bundle/
ADD cnf/localrepo/jackson-core-lgpl/jackson-core-lgpl-1.9.13.jar /opt/felix/current/bundle/
ADD cnf/localrepo/jackson-mapper-lgpl/jackson-mapper-lgpl-1.9.13.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.commons.logging/org.apache.commons.logging-1.1.3.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.commons.pool2/org.apache.commons.pool2-2.3.0.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.felix.eventadmin/org.apache.felix.eventadmin-1.4.2.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.felix.log/org.apache.felix.log-1.0.1.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.felix.webconsole/org.apache.felix.webconsole-4.2.8.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.felix.webconsole.plugins.ds/org.apache.felix.webconsole.plugins.ds-1.0.0.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.felix.webconsole.plugins.event/org.apache.felix.webconsole.plugins.event-1.1.2.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.httpcomponents.httpclient/org.apache.httpcomponents.httpclient-4.3.6.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.apache.httpcomponents.httpcore/org.apache.httpcomponents.httpcore-4.3.3.jar /opt/felix/current/bundle/
ADD cnf/localrepo/org.ops4j.pax.web.pax-web-jetty-bundle/org.ops4j.pax.web.pax-web-jetty-bundle-4.1.1.jar /opt/felix/current/bundle/
ADD cnf/localrepo/slf4j.api/slf4j.api-1.7.7.jar /opt/felix/current/bundle/
ADD cnf/localrepo/slf4j.simple/slf4j.simple-1.7.7.jar /opt/felix/current/bundle/
CMD cd /opt/felix/current && java -jar bin/felix.jar 
