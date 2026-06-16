
cd customization/

export jdbc_mssql_driver_version=${jdbc_mssql_driver_version:-8.2.2}
export jdbc_pg_driver_version=${jdbc_pg_driver_version:-42.7.8}

echo "Downloading MSSQL - mssql-jdbc-${jdbc_mssql_driver_version}.jre8.jar Version"
echo "Downloading PGSQL - postgresql-${jdbc_pg_driver_version}.jar Version "
echo "The Oracle driver requires login, so it has already been downloaded."

export jdbc_mssql_driver_download_url=https://github.com/Microsoft/mssql-jdbc/releases/download/v${jdbc_mssql_driver_version}/mssql-jdbc-${jdbc_mssql_driver_version}.jre8.jar
curl -SOLs ${jdbc_mssql_driver_download_url}

export jdbc_pg_driver_download_url=https://jdbc.postgresql.org/download/postgresql-${jdbc_pg_driver_version}.jar
curl -SOLs ${jdbc_pg_driver_download_url}

#oracle driver requires license
# export i2b2_core_version=1
# export i2b2_war_download_url=https://github.com/i2b2-cdi/i2b2-core-server/releases/download/${i2b2_core_version}/i2b2.war
# curl -SOLs ${i2b2_war_download_url}
