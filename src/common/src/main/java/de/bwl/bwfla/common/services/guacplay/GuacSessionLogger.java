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

package de.bwl.bwfla.common.services.guacplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.bwl.bwfla.common.services.guacplay.net.IGuacInterceptor;
import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;


public class GuacSessionLogger implements IGuacInterceptor
{
	private static final int MAX_CHARS_TO_PRINT = 80;

	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(GuacSessionLogger.class);

	@Override
	public synchronized void onBeginConnection() throws Exception
	{
		log.info("========== GUACAMOLE SESSION START ==========");
	}

	@Override
	public synchronized void onEndConnection() throws Exception
	{
		log.info("========== GUACAMOLE SESSION END ===========");
	}

	@Override
	public synchronized boolean onClientMessage(CharArrayWrapper message) throws Exception
	{
		log.info("C2S: {}", GuacSessionLogger.getInstrHead(message));
		return true;
	}

	@Override
	public synchronized boolean onServerMessage(CharArrayWrapper message) throws Exception
	{
		log.info("S2C: {}", GuacSessionLogger.getInstrHead(message));
		return true;
	}
	
	private static String getInstrHead(CharArrayWrapper message)
	{
		final char[] data = message.array();
		final int offset = message.offset();
		final int length = message.length();
		
		final int count = Math.min(length, MAX_CHARS_TO_PRINT);
		String instr = new String(data, offset, count);
		if (length > MAX_CHARS_TO_PRINT)
			instr += "...";
		
		return instr;
	}
}
