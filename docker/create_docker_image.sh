export CORE_SERVER_TAG=$(echo "$1" | tr '/' '-')
echo "Core Server Tag - " $CORE_SERVER_TAG

CORE_SERVER_REPO=$(pwd)/..

#local build or CI build
if [ "$CI" = "true" ]; then
    echo "Running in GitHub Actions.."
else
    echo "Running Locally.."
    echo "This script requires sudo access to install openjdk-21 & ant ."
    export docker_username="local"
    export docker_reponame="local"
    sudo apt-get update
    sudo apt install -y openjdk-21-jdk ant 
    java --version
    sleep 10
fi

cd "$CORE_SERVER_REPO";

cd edu.harvard.i2b2.server-common && ant clean dist war; 
cp dist/i2b2.war $CORE_SERVER_REPO/docker/configuration/customization/;
cd $CORE_SERVER_REPO/docker/configuration;

bash customization/download_drivers.sh

docker build -t $docker_username/$docker_reponame:i2b2-core-server_$CORE_SERVER_TAG .
docker push $docker_username/$docker_reponame:i2b2-core-server_$CORE_SERVER_TAG

#for multi-platform build - it will build and publish the image
# docker buildx build --platform linux/amd64,linux/arm64 -t $docker_username/$docker_reponame:i2b2-core-server-multibuild --push "configuration/"
