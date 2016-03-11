cmake_minimum_required (VERSION 2.8)

find_package(Java REQUIRED)
include(UseJava)

project (tavernaPBS)

add_jar(tavernaPBS SOURCES tavernaPBS Config.java Extract.java Window.java  PBS.java Job.java PBSConvert.java Merge.java INCLUDE_JARS ../externalJars/ganymed-ssh2-build250.jar ../externalJars/base64coder.jar)