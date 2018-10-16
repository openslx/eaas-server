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
import java.net.Socket;
import java.net.UnknownHostException;

public class VncUtils {

	public static final int VNC_PORT_MIN = 5900;
	public static final int VNC_PORT_MAX = 6000;

	private static final int MAX_TRIES = 50;



	public static int allocateVncPort() {
		int vncPort = VncUtils.VNC_PORT_MIN;

		// get localhost ip
		String localIp = null;
		try {
			localIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return -1;
		}

		// open port until no failure occurs, increment static variable if successful
		for (int i = 0; i < MAX_TRIES; ++i) {
			Socket vncSocket = null;
			try {
				vncSocket = new Socket(localIp, vncPort);
				vncPort = ((vncPort + 1) < VNC_PORT_MAX) ? (vncPort + 1) : VNC_PORT_MIN;
			} catch (IOException e) {
				return vncPort;
			} finally {
				try {
					if (vncSocket != null)
						vncSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return -1;
	}

}
