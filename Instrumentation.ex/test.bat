@echo off
REM RUN ant jar-testAgent before

REM bypass individual tests 
GOTO all
REM check need for -rt.jar

set TEST=ValueMemTest
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log -Dbla=x -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%TEST%
java -Xverify:all -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,iFactory=de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree.Factory -Dbla=x -Dindirect=true -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%TEST%.xml -Dbla=x -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%TEST%.xml -Dbla=x -Dindirect=true -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%TEST%
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%TEST%-native.xml -Dbla=x -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%TEST%

REM end of individual tests
GOTO end

:all

echo events
FOR /F %%f in (src/test/tests) do (
  echo %%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log -Dbla=x -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log -Dbla=x -Dindirect=true -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -Dindirect=true -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%%~nf
)

REM skip for the moment synchronous recorder may stop while shutting down JVM
goto end

:synchronized 
echo synchronized
FOR /F %%f in (src/test/tests) do (
  echo %%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log -Dbla=x -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log -Dbla=x -Dindirect=true -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%%~nf
  java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%%~nf.xml -Dbla=x -Dindirect=true -classpath dist\tests.jar;dist\win32\spass-meter-rt.jar test.%%~nf
)

:end