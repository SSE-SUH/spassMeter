#!/bin/bash

JAVA=$JAVA_HOME/bin/java
CLASSPATH=dist/tests.jar:dist/linux/spass-meter-rt.jar
AGENTPATH=dist/linux/spass-meter-ia.jar
for file in test/*.xml 
do
  CLASS=`basename $file .xml`
  echo $CLASS
  $JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log -Dbla=x -classpath $CLASSPATH test.$CLASS
  #$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log -Dbla=x -classpath $CLASSPATH test.$CLASS

  #$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log -Dbla=x -Dindirect=true -classpath $CLASSPATH test.$CLASS
  #$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log -Dbla=x -Dindirect=true -classpath $CLASSPATH test.$CLASS

  #$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/$CLASS.xml -Dbla=x -classpath $CLASSPATH test.$CLASS
  #$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/$CLASS.xml -Dbla=x -classpath $CLASSPATH test.$CLASS

  #$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/$CLASS.xml -Dbla=x -Dindirect=true -classpath $CLASSPATH test.$CLASS
  #$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/$CLASS.xml -Dbla=x -Dindirect=true -classpath $CLASSPATH test.$CLASS
done

echo PatternTests
$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/PatternTest1.xml -Dbla=x -classpath $CLASSPATH test.PatternTest
$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/PatternTest2.xml -Dbla=x -classpath $CLASSPATH test.PatternTest
$JAVA -javaagent:$AGENTPATH=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/PatternTest3.xml -Dbla=x -classpath $CLASSPATH test.PatternTest
