FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=America/New_York
# WildFly setup
ENV JBOSS_HOME=/opt/jboss/wildfly
ENV PATH=$PATH:$JBOSS_HOME/bin
ENV WILDFLY_VERSION=37.0.1.Final

# Install everything in one layer (faster + smaller)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        tzdata \
        openjdk-21-jdk \
        curl \
        wget \
        unzip && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p $JBOSS_HOME && \
    wget -q https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.tar.gz -O /tmp/wildfly.tar.gz && \
    tar -xzf /tmp/wildfly.tar.gz -C /tmp && \
    cp -r /tmp/wildfly-${WILDFLY_VERSION}/* $JBOSS_HOME && \
    rm -rf /tmp/*

WORKDIR /opt/jboss/wildfly

# Copy only what you need (important for cache)
COPY customization /opt/jboss/wildfly/customization
RUN chmod +x /opt/jboss/wildfly/customization/execute.sh

# Healthcheck (useful in CI/CD)
HEALTHCHECK CMD curl -f http://localhost:8080 || exit 1

CMD ["/bin/bash","-c","/opt/jboss/wildfly/customization/execute.sh"]