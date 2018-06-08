Instrumentation: The implementation of SPASS-meter.

Compatibility:
- The libraries provided with this project are prepared for
  - Windows x86 32 bit and 64 bit version
  - Linux
- Java 9

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

How to start this:
- See detailed description in resources/manual.docx or resources/manual.pdf, respectively.

For using the XML configuration schema in Eclipse:
- add the key http://sse.uni-hildesheim.de/instrumentation to the XML Schema catalogue
- enable XML file validation in preferences

Implementation hints:
- Please avoid using $sig, $type, $cflow and new inner classes in instrumentation 
  as they require Javassist runtime support which, in turn, increases runtime and 
  complicates using OSGi. 

Release tasks:
- edit verion file
- edit .MF files
- edit POM files in pom (snapshot -> version -> snapshot)
- edit manual.docx, export PDF

Release history
- version 1.0
  released to GITHUB at the end of FP7 INDENICA
- version 1.1 (Apache STORM contributions for FP7 QualiMaster)
  - monitoring of multiple JVMs on the same machine
  - relaxed thread monitoring for serialized runnables (preliminary)
  - spass-meter-rt.jar not required anymore in classpath (packaging changed)
- version 1.2 (monitoring individual instances)
  - version file in Jar (inspired by H. Knoche)
  - static instrumentation dependency and file name patterns (reported by M. Keunecke)
  - pattern improvement for namespace/module (reported by A. Krafczyk)
- version 1.3 (update to JAVA 9)
  - update of javassist library to version 3.22.00
  - update of ASM libray to version 6.2
  - update of checkstyle specs to checkstyle version 8.8
  - removal of unused BCEL library
  - removal of outdated (Java 8 style) library references to tools.jar and plugin.jar