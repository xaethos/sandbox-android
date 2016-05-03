#!/usr/bin/env bash

set -e -x
export TERM=dumb
export ANDROID_HOME="$PWD/sandbox-android/android-sdk-linux"
export PATH="$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools"

pushd sandbox-android
#  apt-get update
#  apt-get install -y lib32stdc++6 lib32z1
  dpkg --add-architecture i386
  apt-get -y update
  apt-get -y install libstdc++6:i386 zlib1g:i386
  #apt-get -y install libncurses5:i386 libstdc++6:i386 zlib1g:i386
  sh sdk.dep
  ./gradlew test
popd
