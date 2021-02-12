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

package com.openslx.eaas.imagearchive;

import com.openslx.eaas.imagearchive.config.ImageArchiveConfig;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.logging.PrefixLoggerContext;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.spi.CDI;
import java.util.logging.Logger;


@Startup
@Singleton
public class ArchiveBackend
{
	private static final Logger LOG = Logger.getLogger("IMAGE-ARCHIVE");

	private ImageArchiveConfig config;


	public ImageArchiveConfig config()
	{
		return config;
	}

	/** Return global backend-instance */
	public static ArchiveBackend instance()
	{
		return CDI.current()
				.select(ArchiveBackend.class)
				.get();
	}

	/** Return global logger */
	public static Logger logger()
	{
		return LOG;
	}

	/** Create a new prefix-logger */
	public static Logger logger(String key, String value)
	{
		final var context = new PrefixLoggerContext()
				.add(key, value);

		return new PrefixLogger(LOG.getName(), context);
	}


	// ===== Internal Helpers ==============================

	protected ArchiveBackend()
	{
		// Empty!
	}

	@PostConstruct
	private void initialize()
	{
		try {
			this.config = ImageArchiveConfig.create(LOG);
		}
		catch (Exception error) {
			throw new RuntimeException(error);
		}
	}
}
