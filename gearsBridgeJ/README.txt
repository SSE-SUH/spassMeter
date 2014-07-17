Links gearsBridge (locutor.dll) to Java, i.e. provides a class with native methods and demonstrates the use.

The libraries provided with this project are prepared for
  - Windows x86 32 bit (currently no 64 bit version)
  - Linux (at least GLIBC 2.7)

The dll must be copied from gearsBridge (after building the dll) - this is done by the ANT script.

Define in Eclipse an installed JRE to the current JDK and name it "jdk".

Set 
 - Eclipse User Library JRE_PLUGINS to JAVA_HOME\jre\lib\plugin.jar
 - ANT Properties PLUGIN to JAVA_HOME\jre\lib\plugin.jar
 
Deprecated
 - Eclipse User Library JRE_TOOLS to JAVA_HOME\lib\tools.jar
 - ANT Properties TOOLS to JAVA_HOME\lib\tools.jar

Let Swing Programs end their main method, e.g. using WindowConstants.DISPOSE_ON_CLOSE as default
closing operation of the main window.