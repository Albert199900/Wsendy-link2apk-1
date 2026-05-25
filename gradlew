#!/usr/bin/env sh

# Gradle wrapper script ya kurun amri kwenye Linux (GitHub Actions)
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Tafuta wapi mafaili yalipo
DIRNAME=`dirname "$0"`
if [ "$DIRNAME" = "." ]; then
    DIRNAME="."
fi

# Washa Gradle kwa kutumia java iliyopo kwenye mfumo
exec java -Xmx64m "-Dorg.gradle.appname=$APP_BASE_NAME" -classpath "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
