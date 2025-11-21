FROM ubuntu:24.04
RUN apt-get update 
ARG DEBIAN_FRONTEND=noninteractive

# Set timezone to America/New_York (EST/EDT)
ENV TZ=America/New_York
 
RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    dpkg-reconfigure -f noninteractive tzdata
 
    
# ENV jdbc_mssql_driver_version=${jdbc_mssql_driver_version:-12.8.1jre}\
#     jdbc_mssql_driver_download_url=https://github.com/Microsoft/mssql-jdbc/releases/download/v${jdbc_mssql_driver_version}/mssql-jdbc-${jdbc_mssql_driver_version}.jre8.jar


# ENV jdbc_pg_driver_version=${jdbc_pg_driver_version:-42.7.8}
# ENV jdbc_pg_driver_download_url=https://jdbc.postgresql.org/download/postgresql-${jdbc_pg_driver_version}.jar

# Set environment variables for WildFly
ENV JBOSS_HOME=/opt/jboss/wildfly
ENV JBOSS_CLI=$JBOSS_HOME/bin/jboss-cli.sh
ENV PATH=$PATH:$JBOSS_HOME/bin

RUN apt-get install -y openjdk-17-jdk curl wget unzip

ENV WILDFLY_VERSION=37.0.1.Final
# Download and extract WildFly
RUN mkdir -p $JBOSS_HOME && \
   wget https://github.com/wildfly/wildfly/releases/download/37.0.1.Final/wildfly-37.0.1.Final.tar.gz -P /tmp && \
   tar -xzf /tmp/wildfly-$WILDFLY_VERSION.tar.gz -C /tmp && \
   cp -r /tmp/wildfly-$WILDFLY_VERSION/* $JBOSS_HOME && \
   rm -rf /tmp/wildfly-$WILDFLY_VERSION.tar.gz /tmp/wildfly-$WILDFLY_VERSION
ENV JBOSS_HTTP_PORT=8080
ENV DEBUG_ENABLED=true
 
ENV DS_TYPE="postgres" 
 
## database related env variables
ENV DS_CRC_IP="" \
    DS_ONT_IP="" \
    DS_PM_IP="" \
    DS_IM_IP="" \
    DS_WD_IP="" \
    DS_CRC_PORT="" \
    DS_ONT_PORT="" \
    DS_PM_PORT="" \
    DS_IM_PORT="" \
    DS_WD_PORT=""
 
ENV DS_CRC_USER="i2b2demodata" \
    DS_ONT_USER="i2b2metadata" \
    DS_PM_USER="i2b2pm" \
    DS_IM_USER="i2b2imdata" \
    DS_WD_USER="i2b2workdata" \
    DS_HIVE_USER="i2b2hive"
 
ENV DS_DUMMY="" \
    DS_CRC_PASS="" \
    DS_ONT_PASS="" \
    DS_PM_PASS="" \
    DS_IM_PASS="" \
    DS_WD_PASS="" \
    DS_HIVE_PASS=""
 
ENV DS_CRC_DB="i2b2" \
    DS_ONT_DB="i2b2" \
    DS_PM_DB="i2b2" \
    DS_IM_DB="i2b2" \
    DS_WD_DB="i2b2" \
    DS_HIVE_DB="i2b2" 
 
ENV DS_CRC_SCHEMA="public" \
    DS_ONT_SCHEMA="public" \
    DS_PM_SCHEMA="public" \
    DS_IM_SCHEMA="public" \
    DS_WD_SCHEMA="public" \
    DS_HIVE_SCHEMA="public" 

ENV DS_SERVICE_NAME="XEPDB1"    

COPY customization /opt/jboss/wildfly/customization
RUN chmod +x /opt/jboss/wildfly/customization/execute.sh
 
# Fix for Error: Could not rename /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current
USER root
RUN rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history
# Fix for permission error on adding driver module
RUN chgrp -R 0 /opt/jboss/wildfly/standalone \
    && chmod -R g+rwX /opt/jboss/wildfly/standalone \
    && chgrp -R 0 /opt/jboss/wildfly/modules \
    && chmod -R g+rwX /opt/jboss/wildfly/modules
 
CMD ["/bin/bash","-C","/opt/jboss/wildfly/customization/execute.sh"]
