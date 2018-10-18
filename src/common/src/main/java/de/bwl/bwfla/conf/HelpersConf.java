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

package de.bwl.bwfla.conf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.tamaya.inject.api.Config;


@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HelpersConf
{	
	@Config("helpers.hddfat16create")
	public String hddFat16Create;
	@Config("helpers.hddfat16io")
	public String hddFat16Io;
	
	@Config("helpers.hddhfscreate")
	public String hddHfsCreate;
	@Config("helpers.hddhfsio")
	public String hddHfsIo;
	
	@Config("helpers.floppyfat12create")
	public String floppyFat12Create;
	@Config("helpers.floppyfat12io")
	public String floppyFat12Io;
}
