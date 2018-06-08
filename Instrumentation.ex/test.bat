@echo off
set CP_BIN=bin
set CP_TESTS=dist\tests.jar
set INSTALLDIR=%CD%\dist\win
set AGENTPATH=%INSTALLDIR%\spass-meter-ia.jar
set FACTORY=
REM UNCOMMENT FOR ASM Tests
REM set FACTORY=,iFactory=de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree.Factory
if not exist generated mkdir generated
echo Hint: run "ANT jar-testAgent" before

REM bypass individual tests, uncomment for individual tests
GOTO all

set TEST=MemoryAllocationTest
java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%TEST%.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_TESTS% test.%TEST%

REM end of individual tests
GOTO end

:all

echo events
FOR /F %%f in (src/test/tests) do (
  echo %%~nf
  REM varContrib in first two lines is needed as default was set to false and the XML is taking care of it
  java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,varContrib=true,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,varContrib=true,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
  java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/%%~nf.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%%~nf.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
)

echo PatternTests
java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/PatternTest1.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_TESTS% test.PatternTest
java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/PatternTest2.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_TESTS% test.PatternTest
java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=true,out=generated/test.log,xmlconfig=src/test/PatternTest3.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_TESTS% test.PatternTest

REM skip for the moment synchronous recorder may stop while shutting down JVM
goto end

:synchronized 
echo synchronized
FOR /F %%f in (src/test/tests) do (
  echo %%~nf
  REM varContrib in first two lines is needed as default was set to false and the XML is taking care of it
  java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,varContrib=true,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log,varContrib=true,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
  java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/%%~nf.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_TESTS% test.%%~nf
  java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,groupAccounting=INDIRECT,out=generated/test.log,xmlconfig=src/test/%%~nf.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -Dindirect=true -classpath %CP_TESTS% test.%%~nf
)

java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/manual/Serialization.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_BIN% test.manual.SerializationTest write
java -javaagent:%AGENTPATH%=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/manual/Serialization.xml,installDir=%INSTALLDIR%%FACTORY% -Dbla=x -classpath %CP_BIN% test.manual.SerializationTest read

:end

@pause