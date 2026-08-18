#!/bin/sh
# Lightweight Maven Wrapper: downloads the pinned Maven distribution on first use.
set -eu
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_VERSION=3.9.9
MAVEN_HOME="$BASE_DIR/.mvn/wrapper/apache-maven-$MAVEN_VERSION"
ZIP="$BASE_DIR/.mvn/wrapper/apache-maven-$MAVEN_VERSION-bin.zip"
URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$BASE_DIR/.mvn/wrapper"
  if [ ! -f "$ZIP" ]; then
    echo "Downloading Maven $MAVEN_VERSION..."
    if command -v curl >/dev/null 2>&1; then
      curl -fL "$URL" -o "$ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ZIP" "$URL"
    else
      echo "ERROR: curl or wget is required to bootstrap Maven." >&2
      exit 1
    fi
  fi
  rm -rf "$BASE_DIR/.mvn/wrapper/apache-maven-$MAVEN_VERSION"
  unzip -q "$ZIP" -d "$BASE_DIR/.mvn/wrapper"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
