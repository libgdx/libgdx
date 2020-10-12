#!/bin/zsh

targetArtifact=$1
targetURL=$2

curlOutput=$(curl -s $targetURL)

echo "$curlOutput" | grep "<a href=.*\/${targetArtifact}.*[0-9]*\.jar<" | sed -E 's/.*href="(.*)".*/\1/' | sed -E '/.*(sources.jar|javadoc.jar)/d' | sort | tail -n 1


