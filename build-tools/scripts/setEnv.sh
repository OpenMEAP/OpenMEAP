#!/bin/sh

# Ant
declare -x ANT_HOME=/usr/share/ant

# OpenMEAP
declare -x ANDROID_SDK_HOME=/Users/schang/apps/android-sdk-mac_x86
declare -x OPENMEAP_HOME=/Users/schang/openmeap

# Path extending
declare -x PATH=$PATH:$ANT_HOME/bin:$OPENMEAP_HOME/build-tools/scripts


