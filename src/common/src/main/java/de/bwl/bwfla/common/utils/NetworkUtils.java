/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.common.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import de.bwl.bwfla.common.exceptions.BWFLAException;

public class NetworkUtils {
    public static List<InetAddress> getPublicHostAddresses() {
        List<InetAddress> result = new ArrayList<InetAddress>();
        
        try {
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface
                    .getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();

                for (Enumeration<InetAddress> addresses = iface
                        .getInetAddresses(); addresses.hasMoreElements();) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress()
                            && !address.isAnyLocalAddress()
                            && !address.isLinkLocalAddress()) {
                        result.add(address);
                    }
                }
            }
        } catch (Throwable t) {
            // the only function that throws is the above block is
            // NetworkInterface.getNetworkInterfaces()
            // if this happens, we can't get a list of network interfaces,
            // so we'll just return an empty list
        }
        return result;
    }
    
	/**
	 * Returns a non-loopback network address the host is reachable on.
	 * If JBoss is bound to a single IP address, this address is preferred.
	 * If JBoss' address is not suitable (e.g. JBoss listens on 0.0.0.0 or ::),
	 * the list of all available network addresses for the host
	 * machine is used to return the first address that is not a loopback
	 * address.
	 * 
	 * @return An InetAddress that this host is available on or null if
	 *         none can be found.
	 * @throws BWFLAException if no public address can be found.
	 */
	public static InetAddress getHostAddress() throws BWFLAException {
	    List<InetAddress> addresses = NetworkUtils.getPublicHostAddresses();
	    if (addresses.size() > 0) {
	        return addresses.get(0);
	    }
	    // we cannot determine a single public host address
	    throw new BWFLAException("Cannot determine at least one public address for this host.");
	}
	
	public static InetAddress getJBossAddress() throws UnknownHostException {
        return InetAddress.getByName(System.getProperty("jboss.bind.address"));
	}
	
	/**
	 * Returns an address that is suitable for external binding a webservice
	 * on this host.
	 * This is either the address that JBoss is explicitly bound to or the
	 * first public IP address if JBoss is bound to 0.0.0.0 or ::.
	 * 
     * @return An InetAddress that is suitable for use in a webservice
     * @throws BWFLAException if no suitable address can be found.
	 */
	public static InetAddress getHostWSAddress(boolean allowlocal) throws BWFLAException {
	    try {
	        InetAddress address = NetworkUtils.getJBossAddress();
	        // loopback or wildcard addresses are not suitable for externally
	        // accessing a WSDL. If the caller is sure that he wants a
	        // loopback address, he should just use one.
	        if ((!allowlocal && address.isLoopbackAddress()) || address.isAnyLocalAddress()) {
	            return NetworkUtils.getHostAddress();
	        }
	        return address;
	    } catch (Exception e) {
	        throw new BWFLAException("Cannot not determine a public WSDL address for this host.");
	    }
	}
	public static InetAddress getHostWSAddress() throws BWFLAException {
	    return NetworkUtils.getHostWSAddress(true);
	}
	
    static final Random rnd = new Random();

	public static String getRandomHWAddress() {
	    // IEEE 802.x hardware addresses are 48bit integers
	    // our random addresses are "locally administered" and "unicast"
	    // addresses indicated by the two least significant bits of the most
	    // significat byte.
	    // this leaves 46 bits of entropy to generate.
	    
	    long random = (long)(Math.random() * (Math.pow(2.0, 46.0) + 1));
	    
	    // our most significant byte only has 6 significant bits!
	    byte[] mac = new byte[6];
	    mac[0] = (byte)((random >> 40) << 2);
	    mac[0] |= 2; // 10 in binary, indicating unicast (lsb = 0) and
	                 // locally administered (lsb << 1 = 1)
	    mac[1] = (byte)(random >> 32);
        mac[2] = (byte)(random >> 24);
        mac[3] = (byte)(random >> 16);
        mac[4] = (byte)(random >>  8);
        mac[5] = (byte)(random >>  0);
        
        StringBuilder result = new StringBuilder(6*2 + 5); // 6 octets + colons
        for (byte b : mac) {
            if (result.length() > 0)
                result.append(":");
            result.append(String.format("%02x", b));
        }
	    return result.toString();
	}
	
	
	protected static InetAddress getJBossAddress2() throws BWFLAException {
        try {
            InetAddress address = InetAddress.getByName(System
                    .getProperty("jboss.bind.address"));

            // note that we allow multicast addresses here in case we want
            // really strange scenarios
            if (address.isLinkLocalAddress()) {
                throw new BWFLAException("Could not determine jboss address: is a link local address");
            }
            if (address.isAnyLocalAddress()) {
                // the IP address is "::" or "0.0.0.0",
                // get a suitable public address
                address = NetworkUtils.getHostAddress();
            }
            return address;
        } catch (UnknownHostException e) {
            throw new BWFLAException(e);
        }
	}
	
	/* grr execptions make things complicated */
	private static boolean isPortOpen(Socket sock, InetSocketAddress isa) {
		try {
			sock.connect(isa, 100);
			sock.close();
			return true;
		} catch (IOException e) {
			// log.info(e.getMessage());
			return false;
		}
	}

	public static boolean waitForTcpPort(String ip, int port, int maxtrys) {
		for (int i = 0; i < maxtrys; i++) {
			Socket sock = new Socket();
			InetSocketAddress isa = new InetSocketAddress(ip, port);
			if (isPortOpen(sock, isa))
				return true;
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		return false;
	}
}
