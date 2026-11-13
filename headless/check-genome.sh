#!/bin/sh
# Needs the JDK the mod was built with (26.3 build: Java 25); set JAVA_HOME if that is not the default.
# Step 2 checks of the mod's own Genome and WholeBrain classes, outside Minecraft. Run ./gradlew build first.
cd "$(dirname "$0")/.." || exit 1
MC=$(find .gradle/loom-cache "$HOME/.gradle/caches/fabric-loom" -name '*.jar' -path '*minecraft*' -name '*named*' ! -name '*sources*' 2>/dev/null | head -1)
CP=build/classes/java/main:build/resources/main:headless/lib/gson-2.10.1.jar:$MC
mkdir -p build/headless-check
"${JAVA_HOME:+$JAVA_HOME/bin/}javac" -cp "$CP" -d build/headless-check headless/src/GenomeCheck.java || exit 1
exec "${JAVA_HOME:+$JAVA_HOME/bin/}java" -Xmx3g -cp "build/headless-check:$CP" GenomeCheck
