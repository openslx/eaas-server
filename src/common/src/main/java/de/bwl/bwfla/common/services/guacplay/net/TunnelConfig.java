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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;


/** This class describes the parameters for the construction of {@link GuacTunnel}s. */
@XmlType(name="tunnelConfig", namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlRootElement(name="tunnelConfig")
public final class TunnelConfig
{
	// Member fields
	
	@XmlElement(name="guacamoleConfiguration")
	private final GuacConfigurationWrapper config;
	
	@XmlElement(name="guacamoleClientInformation")
	private final GuacClientInformationWrapper info;

	private IGuacInterceptor interceptor;
	private String guachost;
	private int guacport;
	
	/** Port, used by the GUACD deamon. */
	public static final int GUACD_PORT	= 4822;
	
	
	/* Constructor */
	public TunnelConfig()
	{
		this("localhost", GUACD_PORT);
	}
	
	/* Constructor */
	public TunnelConfig(String guachost, int guacport)
	{
		this.config = new GuacConfigurationWrapper();
		this.info = new GuacClientInformationWrapper();
		this.interceptor = null;
		this.guachost = guachost;
		this.guacport = guacport;
	}

	/** Returns the Gucamole's configuration. */
	public GuacamoleConfiguration getGuacamoleConfiguration()
	{
		return config;
	}
	
	/** Returns the information about Gucamole's client. */
	public GuacamoleClientInformation getGuacamoleClientInformation()
	{
		return info;
	}
	
	/** Registers the {@link IGuacInterceptor}. */
	public void setInterceptor(IGuacInterceptor interceptor)
	{
		this.interceptor = interceptor;
	}
	
	/** Returns the registered {@link IGuacInterceptor}. */
	@XmlTransient
	public IGuacInterceptor getInterceptor()
	{
		return interceptor;
	}
	
	/** Returns the hostname of guacd. */
	@XmlElement(name="guachost")
	public String getGuacdHostname()
	{
		return guachost;
	}

	/** Sets the hostname of guacd. */
	public void setGuacdHostname(String host)
	{
		this.guachost = host;
	}
	
	/** Returns the port of guacd. */
	@XmlElement(name="guacport")
	public int getGuacdPort()
	{
		return guacport;
	}
	
	/** Sets the port of guacd. */
	public void setGuacdPort(int port)
	{
		this.guacport = port;
	}
}
