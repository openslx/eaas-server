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

import java.util.ArrayList;

import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;


/**
 * This class represents a chain of {@link IGuacInterceptor}s.
 * The interceptors are visited in their insertion-order.
 */
public class GuacInterceptorChain implements IGuacInterceptor
{
	private final ArrayList<IGuacInterceptor> interceptors;
	
	/** Constructor */
	public GuacInterceptorChain(int capacity)
	{
		this.interceptors = new ArrayList<IGuacInterceptor>(capacity);
	}
	
	/** Add a new {@link IGuacInterceptor} to this chain. */
	public void addInterceptor(IGuacInterceptor interceptor)
	{
		interceptors.add(interceptor);
	}
	
	/**
	 * Remove the specified {@link IGuacInterceptor} from this chain.
	 * @param interceptor The interceptor to remove.
	 * @return true if the interceptor was found, else false.
	 */
	public boolean removeInterceptor(IGuacInterceptor interceptor)
	{
		return interceptors.remove(interceptor);
	}
	
	
	/* ==================== IGuacInterceptor Implementation ==================== */

	@Override
	public void onBeginConnection() throws Exception
	{
		for (IGuacInterceptor interceptor : interceptors)
			interceptor.onBeginConnection();
	}

	@Override
	public void onEndConnection() throws Exception
	{
		for (IGuacInterceptor interceptor : interceptors)
			interceptor.onEndConnection();
	}

	@Override
	public boolean onClientMessage(CharArrayWrapper message) throws Exception
	{
		for (IGuacInterceptor interceptor : interceptors) {
			if (!interceptor.onClientMessage(message))
				return false;  // Message was dropped/filtered!
		}
		
		return true;
	}

	@Override
	public boolean onServerMessage(CharArrayWrapper message) throws Exception
	{
		for (IGuacInterceptor interceptor : interceptors) {
			if (!interceptor.onServerMessage(message))
				return false;  // Message was dropped/filtered!
		}
		
		return true;
	}
}
