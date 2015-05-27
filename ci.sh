if [ -n "$JENKINS_HOME" ]
then
  echo "This seems to be a jenkins build!"

  echo "BUILD_NUMBER=$BUILD_NUMBER"
  echo "GIT_BRANCH=$GIT_BRANCH"
else
  echo "No Jenkins here. Move along..."
fi

