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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlValue;

import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;


public class GuacConfigurationWrapper extends GuacamoleConfiguration
{
	private static final long serialVersionUID = -6391907013494373279L;

	
	@XmlElementWrapper(name="parameters")
	@XmlElement(name="parameter")
	public List<ParamEntry> getParameterList()
	{
		final Set<String> names = super.getParameterNames();
		final List<ParamEntry> entries = new ArrayList<ParamEntry>(names.size());
		
		for (String name : names) {
			ParamEntry entry = new ParamEntry();
			entry.name = name;
			entry.value = super.getParameter(name);
			
			entries.add(entry);
		}
		
		return entries;
	}
	
	public void setParameterList(List<ParamEntry> entries)
	{
		for (ParamEntry entry : entries)
			super.setParameter(entry.name, entry.value);
	}
	
	
	public static class ParamEntry
	{
		@XmlAttribute
		public String name;
		
		@XmlValue
		public String value;
	}
}
