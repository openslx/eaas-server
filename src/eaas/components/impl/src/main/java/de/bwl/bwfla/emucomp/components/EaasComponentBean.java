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

package de.bwl.bwfla.emucomp.components;

import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.utils.TaskStack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;


public abstract class EaasComponentBean extends AbstractEaasComponent 
{
	protected final PrefixLogger LOG;
	protected final TaskStack cleanups;
	private final Path workdir;
	private final Path bindingDir;

	private final static String tmpBindingsDir = "/tmp-storage";

	protected EaasComponentBean()
	{
		LOG = new PrefixLogger(this.getClass().getSimpleName());
		this.cleanups = new TaskStack(LOG);

		// Create component's working directory
		try {
			Set<PosixFilePermission> permissions = new HashSet<PosixFilePermission>();
			permissions.add(PosixFilePermission.OWNER_READ);
			permissions.add(PosixFilePermission.OWNER_WRITE);
			permissions.add(PosixFilePermission.OWNER_EXECUTE);
			permissions.add(PosixFilePermission.GROUP_READ);
			permissions.add(PosixFilePermission.GROUP_WRITE);
			permissions.add(PosixFilePermission.GROUP_EXECUTE);

			Path tmpBindingsPath = Paths.get(tmpBindingsDir);
			this.workdir = Files.createTempDirectory("eaas-", PosixFilePermissions.asFileAttribute(permissions));
			this.bindingDir = Files.createTempDirectory(tmpBindingsPath,
					"bindings-", PosixFilePermissions.asFileAttribute(permissions));
		}
		catch (IOException error) {
			throw new UncheckedIOException("Creating working directory failed!", error);
		}
	}

	@Override
	public void setComponentId(String id)
	{
		super.setComponentId(id);

		LOG.getContext().add("id", id);
	}

	public Path getWorkingDir()
	{
		return workdir;
	}

	public Path getBindingsDir()
	{
		return bindingDir;
	}


	private void deleteTmpDirs(Path tmpDir)
	{
		try (final Stream<Path> stream = Files.walk(tmpDir)) {
			final Consumer<Path> deleter = (path) -> {
				try {
					Files.delete(path);
				}
				catch (Exception error) {
					final String message = "Deleting '" + path.toString() + "' failed! ("
							+ error.getClass().getName() + ": " + error.getMessage() + ")";

					LOG.warning(message);
				}
			};

			stream.sorted(Comparator.reverseOrder())
					.forEach(deleter);

			LOG.info("Working directory removed: " + tmpDir.toString());
		}
		catch (Exception error) {
			String message = "Deleting working directory failed!\n";
			LOG.log(Level.WARNING, message, error);
		}
	}

	@Override
	public void destroy()
	{
		// Run all tasks in reverse order
		LOG.info("Running cleanup tasks...");
		if (!cleanups.execute())
			LOG.warning("Running cleanup tasks failed!");

		// Delete temp dirs
		deleteTmpDirs(bindingDir);
		deleteTmpDirs(workdir);

		// Destroy base class!
		super.destroy();
	}
}
