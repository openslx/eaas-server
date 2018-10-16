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

package de.bwl.bwfla.emucomp.components.emulators;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glyptodon.guacamole.GuacamoleException;

import de.bwl.bwfla.common.services.guacplay.net.GuacTunnel;
import de.bwl.bwfla.common.services.guacplay.net.PlayerTunnel;
import de.bwl.bwfla.common.services.guacplay.replay.SessionPlayer;
import de.bwl.bwfla.common.utils.ProcessMonitor;


public class SessionPlayerWrapper
{
	/** Logger instance. */
	private final Logger log = Logger.getLogger("SessionPlayerWrapper");
	
	private final boolean headless;
	private final Path trace;
	private SessionPlayer player;
	private boolean started;
	
	
	public SessionPlayerWrapper(Path trace, boolean headless)
	{
		this.headless = headless;
		this.trace = trace;
		this.player = null;
		this.started = false;
	}

	public boolean start(GuacTunnel emutunnel, String id, ProcessMonitor monitor)
	{
		try {
			player = new SessionPlayer(id, emutunnel, monitor, headless);
			player.prepare(trace);
			started = true;
		}
		catch (IOException exception) {
			log.info("An error occured while starting the player!");
			log.log(Level.WARNING, exception.getMessage(), exception);
			started = false;
		}
		
		return started;
	}
	
	public void stop()
	{
		if (!started)
			return;

		started = false;
		
		try {
			if (!player.isFinished())
				player.finish();
		}
		catch (IOException | GuacamoleException e) {
			log.info("An error occured while stopping the player!");
			log.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	public PlayerTunnel getPlayerTunnel()
	{
		return player.getPlayerTunnel();
	}
	
	public int getProgress()
	{
		if (!started)
			return 0;
		
		try {
			return player.getProgress();
		}
		catch (IOException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			return 0;
		}
	}
	
	public boolean isPlaying()
	{
		return ((player != null) && player.isPlaying());
	}
}
