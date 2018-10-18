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


@XmlType(namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlEnum
public enum EmuCompState 
{
	@XmlEnumValue("undefined")
	EMULATOR_UNDEFINED("undefined"),
	
	@XmlEnumValue("busy")
	EMULATOR_BUSY("busy"), // FIXME: remove this state after adding thread-safety
	
	@XmlEnumValue("ready")
	EMULATOR_READY("ready"),
	
	@XmlEnumValue("running")
	EMULATOR_RUNNING("running"),
	
	@XmlEnumValue("inactive")
	EMULATOR_INACTIVE("inactive"),
	
	@XmlEnumValue("stopped")
	EMULATOR_STOPPED("stopped"),
	
	@XmlEnumValue("failed")
	EMULATOR_FAILED("failed"),
	
	@XmlEnumValue("client_fault")
	EMULATOR_CLIENT_FAULT("client_fault");
	
    private final String value;

    EmuCompState(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmuCompState fromValue(String value)
    {
    	EmuCompState state = VALUES.get(value);
        if (state == null)
        	throw new IllegalArgumentException(value);
        
        return state;
    }
    
    /** Local copy of the possible values */
    private static final Map<String, EmuCompState> VALUES;
    static {
    	VALUES = new TreeMap<String, EmuCompState>(String.CASE_INSENSITIVE_ORDER);
    	for (EmuCompState state: EmuCompState.values())
    		VALUES.put(state.value(), state);
    }
}
