/**
 * <h3>Internet Service Class</h3>
 * 
 * <p>This class allows the program to check if there is Internet available for
 * retrieving bank holidays. The production environment does not have an
 * Internet connection, so in this environment the user has to input bank holidays
 * manually.</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.service.internet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Internet Service Class
 * 
 * Check Internet availability
 */
public final class InternetService {

	/**
	 * Default Constructor
	 * 
	 * This constructor is declared private to prevent end users from creating objects
	 * of this class.
	 * 
	 * @since 1.0
	 */
	private InternetService() {
		
	}
	
	/**
	 * Is Internet Available Method
	 * 
	 * @return true if Internet is available
	 * @throws IOException
	 * 
	 */
    public static boolean isInternetAvailable() throws IOException
    {
    	/*
    	 * Attempt to open a TCP connection to four common URL's
    	 */
        return isHostAvailable("google.com") || isHostAvailable("amazon.com")
                || isHostAvailable("facebook.com")|| isHostAvailable("apple.com");
    }

    /**
     * Is Host Available Method
     * 
     * This method accepts a URL and attempts to open a network socket
     * 
     * @param host name
     * @return true if host is available
     * @throws IOException
     */
    private static boolean isHostAvailable(String hostName) throws IOException
    {
        try(Socket socket = new Socket())
        {
            int port = 80;
            InetSocketAddress socketAddress = new InetSocketAddress(hostName, port);
            socket.connect(socketAddress, 3000);

            return true;
        }
        catch(UnknownHostException unknownHost)
        {
            return false;
        }
    }
}
