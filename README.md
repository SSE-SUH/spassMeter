SPASS-meter
==========

SPASS-meter monitoring framework (release version 1.23, development version 1.24-SNAPSHOT)

* CodeEraser: annotation-based manipulation and removal of byte code
* gearsBridge: native data gatherer library (initially based on Google Gears due to other project relation, includes translation to Android NDK library)
* gearsBridgeJ: generic binding of native library to Java
* gearsBridgeAndroid: specialized binding of native library to Android
* Instrumentation.ex: the SPASS-meter framework (naming due to "history", may change in future)
  * dist contains the versions for different operating systems (due to the native library)
  * resources/manual.pdf contains the recent manual
* InstrumentationExamples: basic examples applying SPASS-meter
* InstrumentationJMX/gearsBridgeJMX: binding of native data gatherer / SPASS-meter to JMX
* InstrumentationWildCAT/gearsBridgeWildCAT: binding of native data gatherer / SPASS-meter to OW2 WildCAT
* LoadPlugin: SPASS-meter specific extension of the JMX Console

Maven [repository](https://projects.sse.uni-hildesheim.de/qm/maven/):
```
<dependency>
 <groupId>de.uni_hildesheim.sse.spassMeter</groupId>
 <artifactId>spass-meter</artifactId>
 <version>1.23</version>
</dependency>
```
Similarly, the other JARs are available as individual artifacts: spass-meter-annotations, spass-meter-ant, spass-meter-boot, spass-meter-ia, spass-meter-rt, spass-meter-static.