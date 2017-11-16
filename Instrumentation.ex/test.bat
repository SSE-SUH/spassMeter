@echo off
set CP_BIN=bin
set CP_TESTS=dist\tests.jar
if not exist generated mkdir generated
echo Hint: run "ANT jar-testAgent" before

REM bypass individual tests 
GOTO all
REM check need for -rt.jar

set TEST=InstanceIdentifierTest2
REM set TEST=MultiRecIdTest
REM java -classpath dist/win32/spass-meter-ant.jar de.uni_hildesheim.sse.monitoring.runtime.preprocess.Preprocess dist/tests.jar generated/tests-static.jar logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log -Dbla=x -classpath %CP_TESTS% test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,iFactory=de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree.Factory -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%TEST%.xml -Dbla=x -classpath %CP_TESTS% test.%TEST%
java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,iFactory=de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree.Factory,xmlconfig=src/test/%TEST%.xml -Dbla=x -classpath %CP_TESTS% test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%TEST%.xml -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%TEST%-native.xml -Dbla=x -classpath %CP_TESTS% test.%TEST%

REM end of individual tests
GOTO end

:all

echo events
FOR /F %%f in (src/test/tests) do (
  echo %%~nf
  REM varContrib in first two lines is needed as default was set to false and the XML is taking care of it
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,varContrib=true -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,varContrib=true -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
)

echo PatternTest
java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/PatternTest3.xml -Dbla=x -classpath %CP_TESTS% test.PatternTest

REM skip for the moment synchronous recorder may stop while shutting down JVM
goto end

:synchronized 
echo synchronized
FOR /F %%f in (src/test/tests) do (
  echo %%~nf
  REM varContrib in first two lines is needed as default was set to false and the XML is taking care of it
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,varContrib=true -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log,varContrib=true -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
)

java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/manual/Serialization.xml -Dbla=x -classpath %CP_BIN% test.manual.SerializationTest write
java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/manual/Serialization.xml -Dbla=x -classpath %CP_BIN% test.manual.SerializationTest read

:end

@pause