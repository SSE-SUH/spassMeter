Instrumentation: The implementation of SPASS-meter.

Compatibility:
- Even if SPASS-meter runs under Java 7 there is currently a problem instrumenting Swing 
  Programs (caused by javassist).
- The libraries provided with this project are prepared for
  - Windows x86 32 bit (currently no 64 bit version)
  - Linux

SVN updating and Eclipse
- However, Eclipse seems not always to properly consider changed classpaths which
  should be enabled in course of a SVN update. In this case we suggest to update
  the local workspace / project manually (using a non-Eclipse SVN client) and
  updating the project in Eclipse afterwards (update, clean, restart Eclipse if
  required).

Dependencies (in build.xml):
- gearsBridgeJ
- CodeEraser

Prerequisites:
- Define the environment variable JAVA_HOME properly. If needed, set ANT_HOME.
- In Eclipse create 
  - a installed JRE for your JDK and name it "jdk".
  - user library JRE_PLUGINS pointing to jre/lib/plugin.jar
  - user library JRE_TOOLS pointing to jdk/lib/tools.jar

How to start this:
- See detailed description in resources/manual.docx or resources/manual.pdf, respectively.

For using the XML configuration schema in Eclipse:
- add the key http://sse.uni-hildesheim.de/instrumentation to the XML Schema catalogue
- enable XML file validation in preferences

Implementation hints:
- Please avoid using $sig, $type, $cflow and new inner classes in instrumentation 
  as they require Javassist runtime support which, in turn, increases runtime and 
  complicates using OSGi. 

Stable versions (SVN)
- before introducing ASM: 
  - Adaptivity 1493
  - CodeEraser 1396
  - gearsBridge 1396
  - gearsBridgeAndroid 1355
  - gearsBridgeJ 1835
  - Instrumentation 1882
  - InstrumentationJMX 1883
  - InstrumentationWildCAT 1883
  - LoBaRIS 392
  - LoBaRIS_Android 1835
  - LoBaRISv2 1887