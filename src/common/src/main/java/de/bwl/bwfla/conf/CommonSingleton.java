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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import de.bwl.bwfla.configuration.UserConfigurationPropertySourceProvider;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.ConfigurationInjector;


public class CommonSingleton
{
	protected static final Logger		LOG	= Logger.getLogger(CommonSingleton.class.getName());
	public static volatile boolean 		confValid = false;
	public static volatile CommonConf	CONF = new CommonConf();
	public static HelpersConf helpersConf = new HelpersConf();
	public static RunnerConf runnerConf = new RunnerConf();

	public static Path configPath = UserConfigurationPropertySourceProvider.getConfigPath();

	static
	{
		File tempBaseDir = new File(System.getProperty("java.io.tmpdir"));
		if(!tempBaseDir.exists())
		{
			if(!tempBaseDir.mkdirs())
				System.setProperty("java.io.tmpdir", "/tmp");
		}
		else if(tempBaseDir.canWrite())
		{
			System.setProperty("java.io.tmpdir", "/tmp");
		}

		loadConf();
	}

	public static boolean validate(CommonConf conf)
	{

		Path serverDir = Paths.get(conf.serverdatadir);
		if(!Files.exists(serverDir))
		{
			try {
				Files.createDirectories(serverDir);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		return true;
	}

	synchronized public static void loadConf()
	{
		ConfigurationInjector inj = ConfigurationInjection.getConfigurationInjector();
		inj.configure(CONF);
		inj.configure(helpersConf);
		inj.configure(runnerConf);
		confValid = validate(CONF); 

	}
}