#!/usr/bin/bash
# compile the java code
cd ./tavernaPBS
javac -classpath ".:../ganymed-ssh2-build250.jar" Config.java Extract.java Window.java  PBS.java Job.java PBSConvert.java Merge.java
cd ..

# create the jar archive
jar cvf tavernaPBS.jar ./tavernaPBS/*.class

