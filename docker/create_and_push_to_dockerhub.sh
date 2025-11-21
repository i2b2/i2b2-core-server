
     
#download mssql & pgsql driver

# cd configuration/customization/
# export jdbc_mssql_driver_version=${jdbc_mssql_driver_version:-12.8.1}
# export jdbc_mssql_driver_download_url=https://github.com/Microsoft/mssql-jdbc/releases/download/v${jdbc_mssql_driver_version}/mssql-jdbc-${jdbc_mssql_driver_version}.jre8.jar
# curl -SOLs ${jdbc_mssql_driver_download_url}

# export jdbc_pg_driver_version=${jdbc_pg_driver_version:-42.7.8}
# export jdbc_pg_driver_download_url=https://jdbc.postgresql.org/download/postgresql-${jdbc_pg_driver_version}.jar
# curl -SOLs ${jdbc_pg_driver_download_url}
# cd ../


cd configuration/
docker build -t $docker_username/$docker_reponame:i2b2-core-server_$CORE_SERVER_TAG .
docker push $docker_username/$docker_reponame:i2b2-core-server_$CORE_SERVER_TAG

#publish to private docker repo for test
# docker tag local/i2b2-core-server:$CORE_SERVER_TAG-$date $docker_username/$docker_reponame:i2b2-core-server-$CORE_SERVER_TAG-$date-fork
# docker push $docker_username/$docker_reponame:i2b2-core-server-$CORE_SERVER_TAG-$date-fork

#for multi-platform build - it will build and publish the image
# docker buildx build --platform linux/amd64,linux/arm64 -t $docker_username/$docker_reponame:i2b2-core-server-$WILDFLY_TAG-$date --push i2b2-wildfly
