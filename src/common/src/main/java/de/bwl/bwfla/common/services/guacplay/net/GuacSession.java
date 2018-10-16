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

package de.bwl.bwfla.common.services.guacplay.net;

import javax.servlet.http.HttpSession;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.servlet.GuacamoleSession;


public class GuacSession extends GuacamoleSession
{
	private static final String ATTRNAME = "GUAC_SESSION";


	/**
	 * Creates a new {@link GuacSession} instance and registers
	 * it in {@link HttpSession}, if one doesn't already exist.
	 */
	public static void register(HttpSession session) throws GuacamoleException
	{
		if (session == null)
			throw new GuacamoleServerException("User's HttpSession is invalid!");

		synchronized (session) {
			if (session.getAttribute(ATTRNAME) == null) {
				// Create a new session object and register it once
				session.setAttribute(ATTRNAME, new GuacSession(session));
			}
		}
	}
	
	/** Looks up and returns the registered {@link GuacSession} instance. */
	public static GuacSession get(HttpSession session) throws GuacamoleException
	{
		if (session == null)
			throw new GuacamoleServerException("User's HttpSession is invalid!");

		Object object = session.getAttribute(ATTRNAME);
		if (object == null)
			throw new GuacamoleServerException("User's GuacSession is invalid!");

		return (GuacSession) object;
	}

	/** Internal Constructor */
	private GuacSession(HttpSession session) throws GuacamoleException
	{
		super(session);
	}
}
