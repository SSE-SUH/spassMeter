package de.uni_hildesheim.sse.monitoring.runtime.configuration;

/**
 * Stores TCP connection information, e.g., to connect to the SPASS-meter measurement server.
 * 
 * @author Holger Eichelberger
 * @since 1.21
 * @version 1.21
 */
public class TcpConnectionInfo {
    
    /**
     * The host name.
     */
    private String hostname;
    
    /**
     * The port number.
     */
    private int port;

    /**
     * Creates a TCP connection information object.
     * 
     * @param host the host name
     * @param port the port name
     * @throws IllegalArgumentException if host/port are not valid
     * 
     * @since 1.21
     */
    public TcpConnectionInfo(String host, int port) throws IllegalArgumentException {
        setHostPort(host, port);
    }

    /**
     * Creates a TCP connection information object.
     * 
     * @param connectString a string containing host and port in format <code>host:port</code>
     * @throws IllegalArgumentException if the connect string or contained host/port are not valid
     * 
     * @since 1.21
     */
    public TcpConnectionInfo(String connectString) throws IllegalArgumentException {
        if (null == connectString || 0 == connectString.length()) {
            throw new IllegalArgumentException("No connect string given");
        }
        String[] tcp = connectString.split(":");
        String error = null;
        try {
            if (tcp.length == 2) {
                setHostPort(tcp[0], Integer.parseInt(tcp[1]));
            } else {
                error = "Connect string not in format host:port";
            }
        } catch (NumberFormatException e) {
            error = "Wrong connect string: " + e.getMessage();
        }
        if (null != error) {
            throw new IllegalArgumentException(error);
        }
    }

    /**
     * Changes the TCP connection information.
     * 
     * @param hostname the host name
     * @param port the port name
     * @throws IllegalArgumentException if host/port are not valid
     * 
     * @since 1.21
     */
    public void setHostPort(String hostname, int port) throws IllegalArgumentException {
        this.hostname = hostname;
        this.port = port;
        if (null == hostname || 0 == hostname.length()) {
            throw new IllegalArgumentException("Connect string: No host given.");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Connect string: No valid port given (" + port + ")");
        }
    }
    
    /**
     * Returns the host name.
     * 
     * @return the host name
     * 
     * @since 1.21
     */
    public String getHostname() {
        return hostname;
    }
    
    /**
     * Returns the port number.
     * 
     * @return the port number
     * 
     * @since 1.21
     */
    public int getPort() {
        return port;
    }

}
