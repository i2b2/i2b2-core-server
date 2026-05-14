export CORE_SERVER_TAG=$1
CORE_SERVER_REPO="/home/runner/work/i2b2-core-server/i2b2-core-server/"

cd $CORE_SERVER_REPO;
# echo "cloning the i2b2-core-server repo, branch/tag - "
# echo $CORE_SERVER_TAG

# git clone --depth=1 --branch $CORE_SERVER_TAG  https://github.com/i2b2/i2b2-core-server.git;
# cd i2b2-core-server/edu.harvard.i2b2.server-common && ant clean dist war;
cd edu.harvard.i2b2.server-common && ant clean dist war; #for push/commit  branch
cp dist/i2b2.war $CORE_SERVER_REPO/docker/configuration/customization/;
cd $CORE_SERVER_REPO/docker;
sh create_and_push_to_dockerhub.sh
