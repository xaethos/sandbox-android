FROM xaethos/android-base

# Install SDK 23 and build tools
RUN echo y | $ANDROID_HOME/tools/android update sdk --all --no-ui --filter \
build-tools-23.0.2,\
android-23

# Warm up Maven dependency cache
COPY . project/

#COPY gradlew settings.gradle build.gradle project/
#COPY gradle/                              project/gradle/
#COPY app/build.gradle                     project/app/
#COPY models/build.gradle                  project/models/

#mkdir -p tmp/project/gradle tmp/project/app tmp/project/models
#ln -snf gradlew settings.gradle build.gradle tmp/project
#ln -snf gradle/ tmp/project/gradle/
#ln -snf app/build.gradle tmp/project/app/
#ln -snf models/
#
#COPY tmp/project project

RUN project/gradlew \
--no-daemon \
--console=plain \
--project-dir=/project \
androidDependencies; \
rm -rf project
