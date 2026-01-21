#!/bin/bash

# Usage: execute.sh [WildFly mode] [configuration file]
#
# The default mode is 'standalone' and default configuration is based on the
# mode. It can be 'standalone.xml' or 'domain.xml'.

JBOSS_HOME=/opt/jboss/wildfly
JBOSS_CLI=$JBOSS_HOME/bin/jboss-cli.sh
JBOSS_MODE=${1:-"standalone"}
JBOSS_CONFIG=${2:-"$JBOSS_MODE.xml"}
jndi_connections_file="/opt/jboss/wildfly/customization/custom_datasource.txt"

function wait_for_server() {
  until `$JBOSS_CLI -c ":read-attribute(name=server-state)" 2> /dev/null | grep -q running`; do
    sleep 1
  done
}

function enable_debug() {
  $JBOSS_CLI -c << EOF
    batch
    /subsystem=logging/logger=edu.harvard.i2b2:add ok
    /subsystem=logging/logger=edu.harvard.i2b2:write-attribute(name="level", value="DEBUG")
    run-batch
EOF
}

function create_mssql_ds() {

  $JBOSS_CLI -c << EOF
    # Add MSSQL module
    module add --name=com.mssql --resources=/opt/jboss/wildfly/customization/mssql-jdbc-8.2.2.jre8.jar --dependencies=javax.api,javax.transaction.api

    # Add MSSQL driver
    /subsystem=datasources/jdbc-driver=mssql:add(driver-name="mssql",driver-module-name="com.mssql",driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver)
EOF
  
    if [[ -f "$jndi_connections_file" ]]; 
      then 
        echo "------------------------------------------------------------------"
        echo "Reading Datasources from file: $jndi_connections_file"
        echo "------------------------------------------------------------------"
      $JBOSS_CLI -c <<EOF
      batch 
        $(grep -Ev '^\s*(#|$)' "/opt/jboss/wildfly/customization/custom_datasource.txt")
      run-batch
EOF
    
    else 
    echo "------------------------------------------------------------------"
    echo "Using environment Variables for creating datasources"
    echo "------------------------------------------------------------------"
    $JBOSS_CLI -c << EOF
    batch

    # Add datasources
        data-source add --jndi-name=java:/CRCBootStrapDS --name=CRCBootStrapDS --connection-url=jdbc:sqlserver://${DS_IP}:${DS_PORT};databasename=${DS_HIVE_DB} --driver-name=mssql --user-name=${DS_HIVE_USER} --password=${DS_HIVE_PASS}
        data-source add --jndi-name=java:/QueryToolDemoDS --name=QueryToolDemoDS --connection-url=jdbc:sqlserver://${DS_CRC_IP}:${DS_CRC_PORT};databasename=${DS_CRC_DB} --driver-name=mssql --user-name=${DS_CRC_USER} --password=${DS_CRC_PASS}

        data-source add --jndi-name=java:/IMBootStrapDS --name=IMBootStrapDS --connection-url=jdbc:sqlserver://${DS_IM_IP}:${DS_IM_PORT};databasename=${DS_IM_DB} --driver-name=mssql --user-name=${DS_IM_USER} --password=${DS_IM_PASS}
        data-source add --jndi-name=java:/IMDemoDS --name=IMDemoDS --connection-url=jdbc:sqlserver://${DS_IM_IP}:${DS_IM_PORT};databasename=${DS_IM_DB} --driver-name=mssql --user-name=${DS_IM_USER} --password=${DS_IM_PASS}

        data-source add --jndi-name=java:/OntologyBootStrapDS --name=OntologyBootStrapDS --connection-url=jdbc:sqlserver://${DS_IP}:${DS_PORT};databasename=${DS_HIVE_DB} --driver-name=mssql --user-name=${DS_HIVE_USER} --password=${DS_HIVE_PASS}
        data-source add --jndi-name=java:/OntologyDemoDS --name=OntologyDemoDS --connection-url=jdbc:sqlserver://${DS_ONT_IP}:${DS_ONT_PORT};databasename=${DS_ONT_DB} --driver-name=mssql --user-name=${DS_ONT_USER} --password=${DS_ONT_PASS}

        data-source add --jndi-name=java:/PMBootStrapDS --name=PMBootStrapDS --connection-url=jdbc:sqlserver://${DS_PM_IP}:${DS_PM_PORT};databasename=${DS_PM_DB} --driver-name=mssql --user-name=${DS_PM_USER} --password=${DS_PM_PASS}

        data-source add --jndi-name=java:/WorkplaceBootStrapDS --name=WorkplaceBootStrapDS --connection-url=jdbc:sqlserver://${DS_IP}:${DS_PORT};databasename=${DS_HIVE_DB} --driver-name=mssql --user-name=${DS_HIVE_USER} --password=${DS_HIVE_PASS}
        data-source add --jndi-name=java:/WorkplaceDemoDS --name=WorkplaceDemoDS --connection-url=jdbc:sqlserver://${DS_WD_IP}:${DS_WD_PORT};databasename=${DS_WD_DB} --driver-name=mssql --user-name=${DS_WD_USER} --password=${DS_WD_PASS}

    run-batch
EOF
fi
}

function create_postgres_ds() {
  
  $JBOSS_CLI -c << EOF
    # batch
    # Add Postgres module
    module add --name=org.postgresql --resources=/opt/jboss/wildfly/customization/postgresql-42.7.8.jar --dependencies=javax.api,javax.transaction.api

    # Add Postgres driver
    /subsystem=datasources/jdbc-driver=postgres:add(driver-name="postgres",driver-module-name="org.postgresql",driver-class-name=org.postgresql.Driver)
EOF

  if [[ -f "$jndi_connections_file" ]]; 
        then 
        echo "------------------------------------------------------------------"
        echo "Reading Datasources from file: $jndi_connections_file"
        echo "------------------------------------------------------------------"


        $JBOSS_CLI -c <<EOF
        batch 
          $(grep -Ev '^\s*(#|$)' "/opt/jboss/wildfly/customization/custom_datasource.txt")
        run-batch
EOF

  else 
    echo "------------------------------------------------------------------"
    echo "Using environment Variables for creating datasources"
    echo "------------------------------------------------------------------"
      $JBOSS_CLI -c << EOF
      batch

    # Add datasources
    data-source add --jndi-name=java:/CRCBootStrapDS --name=CRCBootStrapDS --connection-url=jdbc:postgresql://${DS_HIVE_IP}:${DS_HIVE_PORT}/${DS_HIVE_DB}?currentSchema=${DS_HIVE_SCHEMA} --driver-name=postgres --user-name=${DS_HIVE_USER} --password=${DS_HIVE_PASS}
    data-source add --jndi-name=java:/QueryToolDemoDS --name=QueryToolDemoDS --connection-url=jdbc:postgresql://${DS_CRC_IP}:${DS_CRC_PORT}/${DS_CRC_DB}?currentSchema=${DS_CRC_SCHEMA} --driver-name=postgres --user-name=${DS_CRC_USER} --password=${DS_CRC_PASS}

    data-source add --jndi-name=java:/IMBootStrapDS --name=IMBootStrapDS --connection-url=jdbc:postgresql://${DS_IM_IP}:${DS_IM_PORT}/${DS_IM_DB}?currentSchema=${DS_IM_SCHEMA} --driver-name=postgres --user-name=${DS_IM_USER} --password=${DS_IM_PASS}
    data-source add --jndi-name=java:/IMDemoDS --name=IMDemoDS --connection-url=jdbc:postgresql://${DS_IM_IP}:${DS_IM_PORT}/${DS_IM_DB}?currentSchema=${DS_IM_SCHEMA} --driver-name=postgres --user-name=${DS_IM_USER} --password=${DS_IM_PASS}

    data-source add --jndi-name=java:/OntologyBootStrapDS --name=OntologyBootStrapDS --connection-url=jdbc:postgresql://${DS_HIVE_IP}:${DS_HIVE_PORT}/${DS_HIVE_DB}?currentSchema=${DS_HIVE_SCHEMA} --driver-name=postgres --user-name=${DS_HIVE_USER} --password=${DS_HIVE_PASS}
    data-source add --jndi-name=java:/OntologyDemoDS --name=OntologyDemoDS --connection-url=jdbc:postgresql://${DS_ONT_IP}:${DS_ONT_PORT}/${DS_ONT_DB}?currentSchema=${DS_ONT_SCHEMA} --driver-name=postgres --user-name=${DS_ONT_USER} --password=${DS_ONT_PASS}

    data-source add --jndi-name=java:/PMBootStrapDS --name=PMBootStrapDS --connection-url=jdbc:postgresql://${DS_PM_IP}:${DS_PM_PORT}/${DS_PM_DB}?currentSchema=${DS_PM_SCHEMA} --driver-name=postgres --user-name=${DS_PM_USER} --password=${DS_PM_PASS}

    data-source add --jndi-name=java:/WorkplaceBootStrapDS --name=WorkplaceBootStrapDS --connection-url=jdbc:postgresql://${DS_HIVE_IP}:${DS_HIVE_PORT}/${DS_HIVE_DB}?currentSchema=${DS_HIVE_SCHEMA} --driver-name=postgres --user-name=${DS_HIVE_USER} --password=${DS_HIVE_PASS}
    data-source add --jndi-name=java:/WorkplaceDemoDS --name=WorkplaceDemoDS --connection-url=jdbc:postgresql://${DS_WD_IP}:${DS_WD_PORT}/${DS_WD_DB}?currentSchema=${DS_WD_SCHEMA} --driver-name=postgres --user-name=${DS_WD_USER} --password=${DS_WD_PASS}

    # Execute the batch
    run-batch
EOF
fi
}

function create_oracle_ds() {
  
    $JBOSS_CLI -c << EOF
    #String jdbcUrl = "jdbc:oracle:thin:@localhost:1521/${DS_SERVICE_NAME}";
    #jdbc:oracle:thin:@//[HOST][:PORT]/SERVICE

    # batch
    # Add Oracle module
    module add --name=com.oracle --resources=/opt/jboss/wildfly/customization/ojdbc11.jar --dependencies=javax.api,javax.transaction.api

    # Add Oracle driver
    /subsystem=datasources/jdbc-driver=oracle:add(driver-name="oracle",driver-module-name="com.oracle",driver-class-name="oracle.jdbc.OracleDriver",driver-xa-datasource-class-name="oracle.jdbc.xa.client.OracleXADataSource")

    # subsystem=datasources/jdbc-driver=oracle:add(driver-name="oracle",driver-module-name="com.oracle",driver-class-name="oracle.jdbc.OracleDriver",driver-xa-datasource-class-name="oracle.jdbc.xa.client.OracleXADataSource")
EOF

  if [[ -f "$jndi_connections_file" ]]; 
        then 
        echo "------------------------------------------------------------------"
        echo "Reading Datasources from file: $jndi_connections_file"
        echo "------------------------------------------------------------------"

        $JBOSS_CLI -c << EOF
        batch 
          $(grep -Ev '^\s*(#|$)' "/opt/jboss/wildfly/customization/custom_datasource.txt")
        run-batch
EOF
      
  else 
    echo "------------------------------------------------------------------"
    echo "Using environment Variables for creating datasources"
    echo "------------------------------------------------------------------"
      $JBOSS_CLI -c << EOF
      batch
    # Add datasources
    data-source add --jndi-name=java:/CRCBootStrapDS --name=CRCBootStrapDS --connection-url=jdbc:oracle:thin:@//${DS_IP}:${DS_PORT}/${DS_SERVICE_NAME} --driver-name=oracle --user-name=${DS_HIVE_USER} --password=${DS_HIVE_PASS}

    data-source add --jndi-name=java:/QueryToolDemoDS --name=QueryToolDemoDS --connection-url=jdbc:oracle:thin:@//${DS_CRC_IP}:${DS_CRC_PORT}/${DS_SERVICE_NAME} --driver-name=oracle --user-name=${DS_CRC_USER} --password=${DS_CRC_PASS}

    data-source add --jndi-name=java:/IMBootStrapDS --name=IMBootStrapDS --connection-url=jdbc:oracle:thin:@//${DS_IM_IP}:${DS_IM_PORT}/${DS_SERVICE_NAME} --driver-name=oracle --user-name=${DS_IM_USER} --password=${DS_IM_PASS}
    data-source add --jndi-name=java:/IMDemoDS --name=IMDemoDS --connection-url=jdbc:oracle:thin:@//${DS_IM_IP}:${DS_IM_PORT}/${DS_SERVICE_NAME} --driver-name=oracle --user-name=${DS_IM_USER} --password=${DS_IM_PASS}

    data-source add --jndi-name=java:/OntologyBootStrapDS --name=OntologyBootStrapDS --connection-url=jdbc:oracle:thin:@//${DS_IP}:${DS_PORT}/${DS_SERVICE_NAME} --driver-name=oracle --user-name=${DS_HIVE_USER} --password=${DS_HIVE_PASS}
    data-source add --jndi-name=java:/OntologyDemoDS --name=OntologyDemoDS --connection-url=jdbc:oracle:thin:@//${DS_ONT_IP}:${DS_ONT_PORT}/${DS_SERVICE_NAME} --driver-name=oracle --user-name=${DS_ONT_USER} --password=${DS_ONT_PASS}

    data-source add --jndi-name=java:/PMBootStrapDS --name=PMBootStrapDS --connection-url=jdbc:oracle:thin:@//${DS_PM_IP}:${DS_PM_PORT}/${DS_SERVICE_NAME} --driver-name=oracle --user-name=${DS_PM_USER} --password=${DS_PM_PASS}

    data-source add --jndi-name=java:/WorkplaceBootStrapDS --name=WorkplaceBootStrapDS --connection-url=jdbc:oracle:thin:@//${DS_IP}:${DS_PORT}/${DS_SERVICE_NAME} --driver-name=oracle --user-name=${DS_HIVE_USER} --password=${DS_HIVE_PASS}
    data-source add --jndi-name=java:/WorkplaceDemoDS --name=WorkplaceDemoDS --connection-url=jdbc:oracle:thin:@//${DS_WD_IP}:${DS_WD_PORT}/${DS_SERVICE_NAME} --driver-name=oracle --user-name=${DS_WD_USER} --password=${DS_WD_PASS}

    # Execute the batch
    run-batch
EOF
fi
}
chmod +x /opt/jboss/wildfly/bin/jboss-cli.sh

echo "=> Assigning defaults for unspecified variables"
DEBUG_ENABLED="false"
case ${DS_TYPE} in
  "mssql")
    DEFAULT_DS_PORT="1433"
    ;;
  "oracle")
    DEFAULT_DS_PORT="1521"
    ;;
  "postgres")
    DEFAULT_DS_PORT="5432"
    ;;
esac

DS_PORT=${DS_PORT:-${DEFAULT_DS_PORT}}

echo "DS_IP:$DS_IP"
echo "DS_CRC_IP:$DS_CRC_IP"
echo "DS_ONT_IP:$DS_ONT_IP"
echo "DS_PM_IP:$DS_PM_IP"

echo "DS_TYPE:$DS_TYPE"
echo "DS_IP:$DS_IP"
echo "DS_PORT:$DS_PORT"

echo "DS_CRC_IP:$DS_CRC_IP"
echo "DS_CRC_PORT:$DS_CRC_PORT"
echo "DS_CRC_DB:$DS_CRC_DB"

DS_CRC_IP=${DS_CRC_IP:-${DS_IP}}
DS_ONT_IP=${DS_ONT_IP:-${DS_IP}}
DS_PM_IP=${DS_PM_IP:-${DS_IP}}
DS_WD_IP=${DS_WD_IP:-${DS_IP}}

echo "DS_ONT_IP:$DS_ONT_IP"

DS_CRC_PORT=${DS_CRC_PORT:-${DS_PORT}}
DS_ONT_PORT=${DS_ONT_PORT:-${DS_PORT}}
DS_PM_PORT=${DS_PM_PORT:-${DS_PORT}}
DS_WD_PORT=${DS_WD_PORT:-${DS_PORT}}

DS_CRC_PASS=${DS_CRC_PASS:-${DS_PASS}}
DS_ONT_PASS=${DS_ONT_PASS:-${DS_PASS}}
DS_PM_PASS=${DS_PM_PASS:-${DS_PASS}}
DS_WD_PASS=${DS_WD_PASS:-${DS_PASS}}
DS_HIVE_PASS=${DS_HIVE_PASS:-${DS_PASS}}

echo "DS_CRC_IP:$DS_CRC_IP"
echo "DS_CRC_PORT:$DS_CRC_PORT"
echo "DS_CRC_DB:$DS_CRC_DB"
#DS_CRC_IP='i2b2-mssql'
#DS_CRC_PASS='<YourStrong@Passw0rd>'
echo "DS_CRC_IP:$DS_CRC_IP"
echo "DS_CRC_PORT:$DS_CRC_PORT"
echo "DS_CRC_DB:$DS_CRC_DB"


echo "=> Starting WildFly server"
chmod +x $JBOSS_HOME/bin/$JBOSS_MODE.sh
$JBOSS_HOME/bin/$JBOSS_MODE.sh -b 0.0.0.0 -c $JBOSS_CONFIG &

echo "=> Waiting for the server to boot"
wait_for_server

echo "=> Creating data sources"
case ${DS_TYPE} in
  "mssql")
    create_mssql_ds
    ;;
  "oracle")
    create_oracle_ds
    ;;
  "postgres")
    create_postgres_ds
    ;;
esac

# Changing log level
if [ "${DEBUG_ENABLED}" = "true" ]; then
  enable_debug
fi

# Deploy the WAR
cp -r /opt/jboss/wildfly/customization/i2b2.war $JBOSS_HOME/$JBOSS_MODE/deployments/i2b2.war
touch $JBOSS_HOME/$JBOSS_MODE/deployments/i2b2.war.dodeploy

echo "=> Shutting down WildFly"
if [ "$JBOSS_MODE" = "standalone" ]; then
  $JBOSS_CLI -c ":shutdown"
else
  $JBOSS_CLI -c "/host=*:shutdown"
fi

echo "=> Restarting WildFly"
$JBOSS_HOME/bin/$JBOSS_MODE.sh -b 0.0.0.0 -c $JBOSS_CONFIG -Djboss.http.port=${JBOSS_HTTP_PORT:-8080}
