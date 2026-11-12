#!/bin/sh
# Runs the unmodified whole brain outside Minecraft. Needs only a JDK (17+).
cd "$(dirname "$0")/.." || exit 1
mkdir -p build/headless
javac -cp original/connectome-fly-1.0.0.jar:headless/lib/gson-2.10.1.jar -d build/headless headless/src/BaselineBrainRun.java || exit 1
exec java -Xmx2g -cp build/headless:original/connectome-fly-1.0.0.jar:headless/lib/gson-2.10.1.jar BaselineBrainRun "${1:-8}"
