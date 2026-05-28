#!/bin/sh
APP_HOME=$(dirname "$(readlink -f "$0" 2>/dev/null || echo "$0")")
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
JAVA_CMD="${JAVA_HOME:-}/bin/java"
[ ! -x "$JAVA_CMD" ] && JAVA_CMD=java
exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
