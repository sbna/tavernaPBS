#!/usr/bin/bash
# compile the java code
javac -classpath ".:../../../Monitor/bin/lib/ganymed-ssh2-build250.jar" Config.java Extract.java Window.java  PBS.java Job.java PBSConvert.java Merge.java

# create the jar archive
jar cvf tavernaPBS.jar Config.class Extract.class Job.class Merge.class PBS.class PBSConvert.class Window.class ../../../Monitor/bin/lib/ganymed-ssh2-build250.jar

