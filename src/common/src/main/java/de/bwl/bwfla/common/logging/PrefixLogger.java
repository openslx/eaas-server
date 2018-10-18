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

package de.bwl.bwfla.common.logging;

import java.util.logging.LogRecord;
import java.util.logging.Logger;


/** Simple logger, that prepends a predefined prefix to every message. */
public class PrefixLogger extends Logger
{
	private final PrefixLoggerContext context;
	
	public PrefixLogger(String name)
	{
		this(name, new PrefixLoggerContext());
	}
	
	public PrefixLogger(String name, PrefixLoggerContext context)
	{
		super(name, null);
		super.setParent(Logger.getLogger(name));
		this.context = context;
	}
	
	public PrefixLogger(PrefixLogger other)
	{
		this(other.getName(), new PrefixLoggerContext(other.getContext()));
	}
	
	public PrefixLoggerContext getContext()
	{
		return context;
	}
	
	@Override
	public void log(LogRecord record)
	{
		// Prepend the prefix to message
		String message = record.getMessage();
		record.setMessage(context.prefix() + message);
		super.log(record);
	}
}
