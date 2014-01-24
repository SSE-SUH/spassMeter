package test;

/**
 * Defines the ids for annotations and testing.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class AnnotationId {

    /**
     * The pseudo-variability "testing".
     */
    public static final String VAR_TESTING = "testing";

    /**
     * The recorder id for {@link CloneTest}.
     */
    public static final String ID_CLONE = "cloneTest";

    /**
     * The recorder id for {@link IndividualMethodTest}.
     */
    public static final String ID_INDIVIDUALMETHOD = "individualMethod";

    /**
     * The recorder id for {@link ExcludedMethodTest}.
     */
    public static final String ID_EXCLUDEMETHOD = "excludedMethod";

    /**
     * The recorder id for {@link FileIoTest}.
     */
    public static final String ID_FILE_IO = "fileIoTest";

    /**
     * The recorder id for {@link NetIoTest}.
     */
    public static final String ID_URL = "urlTest";
    
    /**
     * The recorder id for {@link NetIoTest}.
     */
    public static final String ID_NET_IO = "netIoTest";

    /**
     * The recorder id for {@link UdpIoTest}.
     */
    public static final String ID_UDP_IO = "udpIoTest";

    /**
     * The recorder id for the server in {@link UdpIoTest}.
     */
    public static final String ID_UDP_IO_SERVER = "udpIoTest-Server";
    
    /**
     * An additional recorder id for the server in {@link NetIoTest}.
     */
    public static final String ID_NET_IO_SERVER = "netIoTest-Server";
    
    /**
     * The recorder id for {@link TimerTest}.
     */
    public static final String ID_TIMER = "timerTest";

    /**
     * The recorder id for {@link FieldAccessTest}.
     */
    public static final String ID_FIELDACCESS = "fieldAccess";

    /**
     * The recorder id for {@link RandomIoTest}.
     */
    public static final String ID_RANDOMIO = "randomIoTest";

    /**
     * The recorder id for {@link IndirectTest}.
     */
    public static final String ID_INDIRECT = "indirectTest";

    /**
     * The recorder id for {@link IndirectTestFile}.
     */
    public static final String ID_INDIRECT_FILE = "indirectTestFile";
    
    /**
     * The recorder id for the indirect part in {@link IndirectTest}.
     */
    public static final String ID_INDIRECT_SUB = "indirectTest-sub";

    /**
     * The recorder id for the first part of {@link MultiRecIdTest}.
     */
    public static final String ID_MULTI_1 = "multi1";
    
    /**
     * The recorder id for the second part of {@link MultiRecIdTest}.
     */
    public static final String ID_MULTI_2 = "multi2";
    
    /**
     * The recorder id for the second part of {@link InterfaceTest}.
     */
    public static final String ID_INTERFACE = "interfaceTest";

    /**
     * The recorder id for the {@link ConfigurationTest}.
     */
    public static final String ID_CONFIG = "comp";
    
    /**
     * The recorder id for the first component in {@link ConfigurationTest}.
     */
    public static final String ID_CONFIG_FILE_COMPONENT = "comp@file";

    /**
     * The recorder id for the second component in {@link ConfigurationTest}.
     */
    public static final String ID_CONFIG_URL_COMPONENT = "comp@url";

    /**
     * Prevents this class from begin instantiated from outside.
     * 
     * @since 1.00
     */
    private AnnotationId() {
    }
        
}
