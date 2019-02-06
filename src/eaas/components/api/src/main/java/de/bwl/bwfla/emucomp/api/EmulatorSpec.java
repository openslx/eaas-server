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

package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "emulatorSpec", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
    "machine", "architecture",
})
public class EmulatorSpec 
{
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected EmulatorSpec.Machine machine;
	
	// optional for now
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required=false)
    protected EmulatorSpec.Architecture architecture = null;
    
    @XmlAttribute(name = "bean", required = true)
    protected String bean;
    @XmlAttribute(name = "version")
    protected String version;


    @XmlAttribute(name = "containerName", required = false)
    protected String containerName;
    @XmlAttribute(name = "containerVersion", required = false)
    protected String containerVersion;

    public EmulatorSpec.Machine getMachine() {
        return machine;
    }

    public void setMachine(EmulatorSpec.Machine value) {
        this.machine = value;
    }

    public String getBean() {
        return bean;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerVersion() {
        return containerVersion;
    }

    public void setContainerVersion(String containerVersion) {
        this.containerVersion = containerVersion;
    }

    public void setBean(String value) {
        this.bean = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        this.version = value;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"id", "name"})
    public static class Architecture {
    	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    	protected String id;
    	
    	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    	protected String name;
    	
    	public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Machine {
    	
        @XmlValue
        protected String value;
        @XmlAttribute(name = "base")
        protected String base;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getBase() {
            return base;
        }

        public void setBase(String value) {
            this.base = value;
        }

    }

}
