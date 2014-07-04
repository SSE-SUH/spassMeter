@echo off
set CP_BIN=bin
set CP_TESTS=dist\tests.jar
REM RUN ant jar-testAgent before

REM bypass individual tests 
GOTO all
REM check need for -rt.jar

set TEST=ValueMemTest
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log -Dbla=x -classpath %CP_TESTS% test.%TEST%
java -Xverify:all -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,iFactory=de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree.Factory -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%TEST%.xml -Dbla=x -classpath %CP_TESTS% test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%TEST%.xml -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%TEST%-native.xml -Dbla=x -classpath %CP_TESTS% test.%TEST%

REM end of individual tests
GOTO end

:all

echo events
FOR /F %%f in (src/test/tests) do (
  echo %%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
)

REM skip for the moment synchronous recorder may stop while shutting down JVM
goto end

:synchronized 
echo synchronized
FOR /F %%f in (src/test/tests) do (
  echo %%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
)

:end

java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/manual/Serialization.xml -Dbla=x -classpath %CP_BIN% test.manual.SerializationTest write
java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/manual/Serialization.xml -Dbla=x -classpath %CP_BIN% test.manual.SerializationTest read
