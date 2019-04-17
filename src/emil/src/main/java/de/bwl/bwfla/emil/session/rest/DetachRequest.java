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

package de.bwl.bwfla.emil.session.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.TimeUnit;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DetachRequest extends JaxbType
{
	@XmlElement(required = true)
	private long lifetime;

	private TimeUnit unit;

	@XmlElement(required = false)
	private ComponentTitleCreator componentTitle;

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class ComponentTitleCreator extends JaxbType
	{
		@XmlElement(required = true, name = "componentName")
		private String componentName;
		@XmlElement(required = true, name = "componentId")
		private String componentId;

		public String getComponentName() {
			return componentName;
		}

		public String getComponentId() {
			return componentId;
		}
	}


	@XmlElement
	private String sessionName;

	public long getLifetime()
	{
		return lifetime;
	}

	public void setLifetime(long lifetime)
	{
		this.lifetime = lifetime;
	}

	public TimeUnit getLifetimeUnit()
	{
		return unit;
	}

	@XmlElement(name = "lifetime_unit",required = true)
	public void setLifetimeUnit(String unitstr)
	{
		switch (unitstr) {
			case "milliseconds":
				this.unit = TimeUnit.MILLISECONDS;
				break;
			case "seconds":
				this.unit = TimeUnit.SECONDS;
				break;
			case "minutes":
				this.unit = TimeUnit.MINUTES;
				break;
			case "hours":
				this.unit = TimeUnit.HOURS;
				break;
			case "days":
				this.unit = TimeUnit.DAYS;
				break;
			default:
				throw new IllegalArgumentException("Unsupported time unit: " + unitstr);
		}
	}




	public String getSessionName() {
		return sessionName;
	}

	public ComponentTitleCreator getComponentTitle() {
		return componentTitle;
	}

	public void setComponentTitle(ComponentTitleCreator componentTitle) {
		this.componentTitle = componentTitle;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}
}
