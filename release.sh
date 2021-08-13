#!/usr/bin/env bash
set -e

./gradlew clean build
echo Now OSS_PWD=*** ./gradlew clean build publishMavenJavaPublicationToSonatypeStagingRepository -Psigning.password=***