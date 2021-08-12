#!/usr/bin/env bash

export JAVA_HOME=/Applications/Android\ Studio.app/Contents/jre/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

./gradlew annotation:clean annotation:assemble annotation:generatePomFileForReleasePublication annotation:publishReleasePublicationToOSSRHRepository
./gradlew compiler:clean compiler:assemble compiler:generatePomFileForReleasePublication compiler:publishReleasePublicationToOSSRHRepository
./gradlew runtime:clean runtime:assembleRelease runtime:generatePomFileForReleasePublication runtime:publishReleasePublicationToOSSRHRepository