see html-files in test for examples

locutor_init.js 
initializes the environment (once)

loc = sse.locutor.factory.create()
creates an object on which the functions of locutor can be called


Funktionen:
- data = loc.gatherData(timeout); 
  reads out relevant system information and returns if timout is exceeded (then without data)
  
  return data structure
  data.accessPoints array with an entry for each accessible access point
    data pro access point (array entries):
    	- macAddress                 : media access control address (string)
		- ssid                       : network identifier (string)
    	- age                        : milliseconds since this access point was detected (int)
		- radioSignalStrength        : radio signal strength in dB (int)
		- channel                    : channel of access point (int)
		- signalToNoise              : ratio in dB (int)
    