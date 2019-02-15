#!/usr/bin/env bash
set -e

: "${BINTRAY_API_KEY?Need to set BINTRAY_API_KEY}"

./gradlew clean build core:publish core:bintrayUpload --no-build-cache