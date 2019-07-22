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

package de.bwl.bwfla.emil.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EventObserver
{
	private final Logger log;
	private final String url;
	private final Client client;
	private final SseEventSource source;

	public EventObserver(String url, Logger log)
	{
		this.log = log;
		this.url = url;
		this.client = ClientBuilder.newClient();
		this.source = SseEventSource.target(client.target(url))
				.build();
	}

	public void start()
	{
		log.info("Start observing server-sent-events from " + url);
		source.open();
	}

	public void stop()
	{
		log.info("Stop observing server-sent-events from " + url);
		source.close();
		client.close();
	}

	public EventObserver register(Consumer<InboundSseEvent> onevent)
	{
		final Consumer<Throwable> onerror = (error) -> {
			log.log(Level.WARNING, "EventSource failed for: " + url, error);
		};

	    source.register(onevent, onerror);
	    return this;
	}
}
