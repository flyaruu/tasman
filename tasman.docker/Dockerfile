FROM java:8-jre
MAINTAINER Frank Lyaruu <frank@dexels.com>
ENV DEBIAN_FRONTEND noninteractive 
ADD http://apache.mirrors.pair.com/felix/org.apache.felix.main.distribution-4.6.1.tar.gz /tmp/
RUN mkdir -p /opt/felix && cd /opt/felix && tar xzvf /tmp/org.apache.felix.main.distribution-4.6.1.tar.gz
RUN ln -s /opt/felix/felix-framework-4.6.1 /opt/felix/current
ADD http://apache.mirrors.pair.com/felix/org.apache.felix.scr-1.8.0.jar /opt/felix/current/bundle/
ADD http://apache.mirrors.pair.com/felix/org.apache.felix.metatype-1.0.10.jar /opt/felix/current/bundle/
ADD http://apache.mirrors.pair.com/felix/org.apache.felix.fileinstall-3.5.0.jar /opt/felix/current/bundle/
ADD http://apache.mirrors.pair.com/felix/org.apache.felix.configadmin-1.8.2.jar /opt/felix/current/bundle/
ADD http://apache.mirrors.pair.com/felix/org.apache.felix.eventadmin-1.4.2.jar /opt/felix/current/bundle/
ADD http://central.maven.org/maven2/org/ops4j/pax/web/pax-web-jetty-bundle/4.1.1/pax-web-jetty-bundle-4.1.1.jar /opt/felix/current/bundle/
ADD http://mvnrepository.com/artifact/org.ops4j.pax.web/pax-web-extender-whiteboard/4.1.1 /opt/felix/current/bundle/
ADD http://central.maven.org/maven2/org/ops4j/pax/logging/pax-logging-api/1.8.2/pax-logging-api-1.8.2.jar /opt/felix/current/bundle/
ADD http://central.maven.org/maven2/org/ops4j/pax/logging/pax-logging-service/1.8.2/pax-logging-service-1.8.2.jar /opt/felix/current/bundle/
CMD cd /opt/felix/current && java -jar bin/felix.jar
