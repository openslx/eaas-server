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

package de.bwl.bwfla.emucomp.control.connectors;


import de.bwl.bwfla.emucomp.xpra.IAudioStreamer;

import java.net.URI;


public class AudioConnector implements IConnector
{
	public final static String PROTOCOL = "audio";

	private final IThrowingSupplier<IAudioStreamer> constructor;
	private IAudioStreamer streamer;

	public AudioConnector(IThrowingSupplier<IAudioStreamer> constructor)
	{
		this.constructor = constructor;
		this.streamer = null;
	}

	@Override
	public URI getControlPath(final URI componentResource)
	{
		return componentResource.resolve(AudioConnector.PROTOCOL);
	}

	@Override
	public String getProtocol()
	{
		return AudioConnector.PROTOCOL;
	}

	public IAudioStreamer newAudioStreamer() throws Exception
	{
		if (streamer != null) {
			streamer.stop();
			streamer.close();
		}

		streamer = constructor.get();
		return streamer;
	}

	public IAudioStreamer getAudioStreamer()
	{
		return streamer;
	}
}
