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

import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;


/**
 * Classes, that implement this interface, are allowed to intercept and modify
 * the whole Guacamole's instruction stream between the server and the client.
 */
public interface IGuacInterceptor
{
	/** This callback will be invoked, when the connection begins. */
	public void onBeginConnection() throws Exception;
	
	/** This callback will be ivoked, when the connection ends. */
	public void onEndConnection() throws Exception;
	
	/**
	 * This callback will be invoked, when a message is recieved from the Guacamole client.
	 * 
	 * @param message The buffer containing the message.
	 * @return true when the message should be forwarded, false if dropped
	 */
	public boolean onClientMessage(CharArrayWrapper message) throws Exception;
	
	/**
	 * This callback will be invoked, when a message is recieved from the GUACD deamon.
	 * 
	 * @param message The buffer containing the message.
	 * @return true when the message should be forwarded, false if dropped
	 */
	public boolean onServerMessage(CharArrayWrapper message) throws Exception;
}
