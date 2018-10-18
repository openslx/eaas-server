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

import java.util.Map;
import java.util.TreeMap;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;



@XmlEnum
@XmlType(namespace = "http://bwfla.bwl.de/common/datatypes")
public enum EaasState 
{
	@XmlEnumValue("undefined")
	SESSION_UNDEFINED("undefined"),
	
	@XmlEnumValue("allocating")
	SESSION_ALLOCATING("allocating"),
	
	@XmlEnumValue("ready")
	SESSION_READY("ready"),
	
	@XmlEnumValue("busy")
	SESSION_BUSY("busy"),
	
	@XmlEnumValue("running")
	SESSION_RUNNING("running"),
	
	@XmlEnumValue("inactive")
	SESSION_INACTIVE("inactive"),
	
	@XmlEnumValue("stopped")
	SESSION_STOPPED("stopped"),
	
	@XmlEnumValue("out_of_resources")
	SESSION_OUT_OF_RESOURCES("out_of_resources"),
	
	@XmlEnumValue("failed")
	SESSION_FAILED("failed"),

	@XmlEnumValue("client_fault")
	SESSION_CLIENT_FAULT("client_fault");
	
	private final String value;
	
    private EaasState(String v) 
    {
        value = v;
    }

    public String value() 
    {
        return value;
    }

    public static EaasState fromValue(String value) 
    {
    	EaasState state = VALUES.get(value);
        if (state == null)
        	throw new IllegalArgumentException(value);
        
        return state;
    }
    
    /** Local copy of the possible values */
    private static final Map<String, EaasState> VALUES;
    static {
    	VALUES = new TreeMap<String, EaasState>(String.CASE_INSENSITIVE_ORDER);
    	for (EaasState state: EaasState.values())
    		VALUES.put(state.value(), state);
    }
}