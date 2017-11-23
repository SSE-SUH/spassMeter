package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

// DO NOT RENAME OR MOVE THIS CLASS OR ITS MEMBERS AS IT IS REFERENCED 
// FROM NATIVE CODE

/**
 * Represents the physical data of a WiFi access point. Some of the data 
 * in an instance of this class may not be available through a physical
 * interfaces and thus be set to invalid values.<p>
 * Instances of this class are intended to be read only.<p>
 * <b>Do not rename or move this class or its members are referenced
 * from native code.</b>
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_WIFI_DATA)
public class AccessPointData {

    /**
     * Defines the externalized name of {@link #macAddress}.
     */
    public static final String DATANAME_MACADDRESS = "macAddress";

    /**
     * Defines the externalized name of {@link #ssid}.
     */
    public static final String DATANAME_SSID = "ssid";
    
    /**
     * Defines the externalized name of {@link #radioSignalStrength}.
     */
    public static final String DATANAME_RADIOSIGNALSTRENGTH 
        = "radioSignalStrength";

    /**
     * Defines the externalized name of {@link #age}.
     */
    public static final String DATANAME_AGE = "age";
    
    /**
     * Defines the externalized name of {@link #channel}.
     */
    public static final String DATANAME_CHANNEL = "channel";
    
    /**
     * Defines the externalized name of {@link #signalToNoise}.
     */
    public static final String DATANAME_SIGNALTONOISE = "signalToNoise";
    
    /**
     * Stores the MAC address.
     */
    private String macAddress;
    
    /**
     * Stores the radio signal strength.
     */
    private int radioSignalStrength = Integer.MIN_VALUE;
    
    /**
     * Stores the milliseconds since this access point was detected.
     */
    private int age = Integer.MIN_VALUE;
    
    /**
     * Stores the channel identification of this access point.
     */
    private int channel = Integer.MIN_VALUE;
    
    /**
     * Stores the signal to noise ratio in dB.
     */
    private int signalToNoise = Integer.MIN_VALUE;
    
    /**
     * Stores the network identifier of the access point.
     */
    private String ssid;

    /**
     * Constructor for native library.
     * 
     * @since 1.00
     */
    public AccessPointData() {
    }

    /**
     * Constructor which sets all values in this class.
     * 
     * @param macAddress specifies the MAC address
     * @param radioSignalStrength the radio signal strength 
     *        (<code>Integer.MIN_VALUE</code> if not available)
     * @param age the milliseconds since this access point was detected 
     *        (<code>Integer.MIN_VALUE</code> if not available)
     * @param channel channel identification of this access point
     *        (<code>Integer.MIN_VALUE</code> if not available)
     * @param signalToNoise the signal to noise ratio in dB
     *        (<code>Integer.MIN_VALUE</code> if not available)
     * @param ssid the network identification
     * 
     * @since 1.00
     */
    public AccessPointData(String macAddress, int radioSignalStrength, 
        int age, int channel, 
        int signalToNoise, String ssid) {
        this.macAddress = macAddress;
        this.radioSignalStrength = radioSignalStrength;
        this.age = age;
        this.channel = channel;
        this.signalToNoise = signalToNoise;
        this.ssid = ssid;
    }

    /**
     * Returns the MAC address of this access point.
     * 
     * @return the mac address
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Returns the radio signal strength of this access point.
     * 
     * @return the radio signal strength (<code>Integer.MIN_VALUE</code>
     *         if not available)
     */
    public int getRadioSignalStrength() {
        return radioSignalStrength;
    }

    /**
     * Returns the milliseconds since this access point was detected.
     * 
     * @return the age in milliseconds (<code>Integer.MIN_VALUE</code>
     *         if not available)
     */
    public int getAge() {
        return age;
    }

    /**
     * Returns the channel of this access point.
     * 
     * @return the channel (<code>Integer.MIN_VALUE</code>
     *         if not available)
     */
    public int getChannel() {
        return channel;
    }

    /**
     * Returns the signal to noise ratio.
     * 
     * @return the signal to noise ratio in dB (<code>Integer.MIN_VALUE</code>
     *         if not available)
     */
    public int getSignalToNoise() {
        return signalToNoise;
    }

    /**
     * Stores the network identifier of the access point.
     * 
     * @return the ssid of this access point
     */
    public String getSsid() {
        return ssid;
    } 
   
    /**
     * Returns a textual representation of this object.
     * 
     * @return a textual representation of this object
     */
    public String toString() {
        return "ssid " + ssid + " mac " + macAddress + " s/n " + signalToNoise 
            + " channel " + channel + " age " + age + " strength " 
            + radioSignalStrength;
    }
    
}
