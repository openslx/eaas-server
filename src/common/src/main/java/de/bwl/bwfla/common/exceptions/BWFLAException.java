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

package de.bwl.bwfla.common.exceptions;

import de.bwl.bwfla.common.utils.EaasBuildInfo;

import javax.ejb.ApplicationException;



@ApplicationException
public class BWFLAException extends Exception
{
	private static final long	serialVersionUID	= 1988575155200056202L;

	private static String getBuildHeader()
	{
		return " (BUILD: " + EaasBuildInfo.getVersion() + ")";
	}

	public BWFLAException() {
		super(getBuildHeader());
	}

	public BWFLAException(String message) {
		super(message + getBuildHeader());
	}
	
	public BWFLAException(String message, Throwable cause) {
		super(message + getBuildHeader(), cause);
	}
	
	public BWFLAException(Throwable cause) {
		super(getBuildHeader(), cause);
	}
}