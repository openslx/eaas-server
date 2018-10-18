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

package de.bwl.bwfla.common.datatypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


@XmlType(namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlEnum
public enum ResourceState 
{ 
	@XmlEnumValue("undefined")
	RESOURCE_ALLOCATING("undefined"),
	
	@XmlEnumValue("ready")
	RESOURCE_READY("ready"),
	
	@XmlEnumValue("released")
	RESOURCE_RELEASED("released"),
	
	@XmlEnumValue("out_of_resources")
	OUT_OF_RESOURCES("out_of_resources"),
	
	@XmlEnumValue("client_fault")
	CLIENT_FAULT("client_fault");
	
	private final String value;

	ResourceState(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResourceState fromValue(String v) {
        for (ResourceState c: ResourceState.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
};