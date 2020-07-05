#!/usr/bin/env bash

mvn versions:set -DremoveSnapshot
git add --all
git commit -m "[release] set release version"
git tag $1
git push --tags
mvn clean deploy -DskipTests -Prelease
mvn versions:set -DnextSnapshot
git add --all
git commit -m "[release] set next developement version"
git push
