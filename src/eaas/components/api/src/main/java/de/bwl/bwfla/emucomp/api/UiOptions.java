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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uiOptions", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
    "html5",
    "input",
    "time",
	"forwarding_system"
})
public class UiOptions {

    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected Html5Options html5;
    
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected InputOptions input;

    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected TimeOptions time;

	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
	private String forwarding_system;

	public String getForwarding_system() {
		return forwarding_system;
	}

	public void setForwarding_system(String forwarding_system) {
		this.forwarding_system = forwarding_system;
	}

	public InputOptions getInput() {
		return input;
	}

	public void setInput(InputOptions input) {
		this.input = input;
	}

	public Html5Options getHtml5() {
        return html5;
    }

    public void setHtml5(Html5Options value) {
        this.html5 = value;
    }

	public TimeOptions getTime() {
		return time;
	}

	public void setTime(TimeOptions time) {
		this.time = time;
	}
}
