FROM java:8-jdk

# Need some 32 bit libs for aapt and adb
RUN dpkg --add-architecture i386 \
    && apt-get update && apt-get install -y --no-install-recommends \
        libstdc++6:i386 \
        zlib1g:i386 \
    && rm -rf /var/lib/apt/lists/*

# Environment variables
ENV TERM=dumb \
    GRADLE_USER_HOME=/var/cache/gradle \
    ANDROID_HOME=/usr/local/android-sdk-linux \
    PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Install Android SDK
RUN curl -L http://dl.google.com/android/android-sdk_r24.4.1-linux.tgz \
| tar --no-same-owner -xzC /usr/local/; \
echo y \
| $ANDROID_HOME/tools/android update sdk --force --all --no-ui --filter \
tools,\
platform-tools,\
extra-android-m2repository,\
extra-google-m2repository

# Install Android tools and Maven repositories
#
# Dockers union FS interacts very badly with the Java in the `android`
# command, deleting the content of tools/ during the update, because it
# is on a lower layer.
# Moving the tools directory forces it to be copied up to the writable
# layer, after which updating tools works fine.
#RUN mv $ANDROID_HOME/tools $ANDROID_HOME/foo; \
#    mv $ANDROID_HOME/foo $ANDROID_HOME/tools; \
#echo y | $ANDROID_HOME/tools/android update sdk --force --all --no-ui --filter \
#tools,\
#platform-tools,\
#extra-android-m2repository,\
#extra-google-m2repository
