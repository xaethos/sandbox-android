if [ -n "$JENKINS_HOME" ]
then
  echo "This is Jenkins build $BUILD_NUMBER!"
  echo "Branch: $GIT_BRANCH"
  IFS=/ read -r GIT_REMOTE BUILD_TYPE BUILD_TOPIC <<<"$GIT_BRANCH"
else
  echo "No Jenkins here. Move along..."
  BUILD_TYPE=develop
  WORKSPACE=`git rev-parse --show-toplevel`
fi

echo "Type: $BUILD_TYPE"
test -n "$BUILD_TOPIC" && echo "Topic: $BUILD_TOPIC"

cd $WORKSPACE
./gradlew assembleDebug
