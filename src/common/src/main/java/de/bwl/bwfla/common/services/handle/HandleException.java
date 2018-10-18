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

package de.bwl.bwfla.common.services.handle;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import net.handle.hdllib.AbstractResponse;


public class HandleException extends BWFLAException
{
	public static final class ErrorCode
	{
		public static final int HANDLE_ALREADY_EXISTS = net.handle.hdllib.HandleException.HANDLE_ALREADY_EXISTS;
		public static final int INVALID_VALUE = net.handle.hdllib.HandleException.INVALID_VALUE;
	}

	public HandleException(String message, AbstractResponse response)
	{
		super(message, net.handle.hdllib.HandleException.ofResponse(response));
	}

	public HandleException(String message, net.handle.hdllib.HandleException cause)
	{
		super(message, cause);
	}

	public int getErrorCode()
	{
		final net.handle.hdllib.HandleException cause = (net.handle.hdllib.HandleException) this.getCause();
		return cause.getCode();
	}

	public String getResponseCodeMessage()
	{
		return net.handle.hdllib.HandleException.getCodeStr(this.getErrorCode());
	}
}
