@echo off

REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/manual/Serialization.xml -Dbla=x -classpath bin;dist\win32\spass-meter-rt.jar test.manual.SerializationTest write
REM java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/test.log,xmlconfig=src/test/manual/Serialization.xml -Dbla=x -classpath bin;dist\win32\spass-meter-rt.jar test.manual.SerializationTest read

java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/multiType.log,xmlconfig=src/test/manual/Serialization.xml -Dbla=x -classpath bin;dist\win32\spass-meter-rt.jar test.manual.MultiTypeTest
java -javaagent:dist/win32/spass-meter-ia.jar=logLevel=SEVERE,overhead=false,configDetect=false,localEventProcessing=false,out=generated/empty.log,xmlconfig=src/test/manual/Empty.xml -Dbla=x -classpath bin;dist\win32\spass-meter-rt.jar test.manual.MultiTypeTest
