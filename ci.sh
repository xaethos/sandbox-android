if [ -n "$JENKINS_HOME" ]
then
  echo "This Jenkins build $BUILD_NUMBER!"
  echo "Branch: $GIT_BRANCH"
  BRANCH_TYPE=`echo $GIT_BRANCH | sed -e 's#[^/]*/\([^/]*\).*#\1#'`
  echo "Type: $BRANCH_TYPE"
else
  echo "No Jenkins here. Move along..."
fi

