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

package de.bwl.bwfla.eaas.cluster.dump;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.stream.JsonGenerator;


public class ObjectDumper
{
	private final JsonGenerator json;
	private final DumpConfig dconf;
	private final int flags;
	private final Class<?> restype;
	private final Map<String, ObjectDumper.Handler> handlers;
	
	public ObjectDumper(JsonGenerator json, DumpConfig dconf, int flags, Class<?> restype)
	{
		this.json = json;
		this.dconf = dconf;
		this.flags = flags;
		this.restype = restype;
		this.handlers = new LinkedHashMap<String, ObjectDumper.Handler>();
	}
	
	public void add(String field, ObjectDumper.Handler handler)
	{
		handlers.put(field, handler);
	}
	
	public void run()
	{
		final boolean inlined = DumpFlags.test(flags, DumpFlags.INLINED);
		if (!inlined)
			json.writeStartObject();

		if (DumpFlags.test(flags, DumpFlags.TIMESTAMP))
			DumpHelpers.writeResourceTimestamp(json);
		
		if (DumpFlags.test(flags, DumpFlags.RESOURCE_TYPE))
			DumpHelpers.writeResourceType(json, restype);
		
		handlers.forEach((field, handler) -> {
			if (dconf.included(field))
				handler.run();
		});

		if (!inlined)
			json.writeEnd();
	}
	
	@FunctionalInterface
	public interface Handler
	{
		public void run();
	}
}
