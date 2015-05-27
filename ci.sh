if [ -n "$JENKINS_HOME" ]
then
  echo "This is Jenkins build $BUILD_NUMBER!"
  echo "Branch: $GIT_BRANCH"
  IFS=/ read -r GIT_REMOTE BUILD_TYPE BUILD_TOPIC <<<"$GIT_BRANCH"
  curl -s --data-urlencode "description=$BUILD_TYPE $BUILD_TOPIC" "${BUILD_URL}submitDescription"
else
  echo "No Jenkins here. Move along..."
  BUILD_TYPE=develop
  WORKSPACE=`git rev-parse --show-toplevel`
fi

echo "Build type: $BUILD_TYPE"

cd $WORKSPACE
./gradlew assembleDebug
