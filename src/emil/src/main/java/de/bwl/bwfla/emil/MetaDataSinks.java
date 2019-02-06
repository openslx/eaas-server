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

package de.bwl.bwfla.emil;

import de.bwl.bwfla.api.imagearchive.ImageArchiveMetadata;
import de.bwl.bwfla.api.imagearchive.ImageType;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emucomp.api.Environment;
import de.bwl.bwfla.metadata.repository.api.ItemDescription;
import de.bwl.bwfla.metadata.repository.sink.ItemSink;
import de.bwl.bwfla.metadata.repository.sink.MetaDataSink;

import javax.xml.bind.JAXBException;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class MetaDataSinks
{
	public static MetaDataSink images(String archive, DatabaseEnvironmentsAdapter db)
	{
		return new MetaDataSink()
				.set(new EnvironmentSink(archive, db));
	}

	public static MetaDataSink environments(EmilEnvironmentRepository environmentRepository)
	{
		return new MetaDataSink()
				.set(new EmilEnvironmentSink(environmentRepository));
	}


	// ========== MetaDataSink Implementations =========================

	private static class EnvironmentSink implements ItemSink
	{
		private final Logger log = Logger.getLogger(this.getClass().getName());
		private final DatabaseEnvironmentsAdapter db;
		private final String archive;

		public EnvironmentSink(String archive, DatabaseEnvironmentsAdapter db)
		{
			this.archive = archive;
			this.db = db;
		}

		@Override
		public void insert(ItemDescription item) throws BWFLAException
		{
			try {
				final Environment environment = Environment.fromValue(item.getMetaData());
				final ImageArchiveMetadata iamd = new ImageArchiveMetadata();
				iamd.setType(ImageType.BASE);  // TODO: how should this type be supplied?
				db.importMetadata(archive, environment, iamd, true);
			}
			catch (JAXBException error) {
				throw new BWFLAException(error);
			}
		}

		@Override
		public void insert(Stream<ItemDescription> items) throws BWFLAException
		{
			final Function<ItemDescription, Integer> inserter = (item) -> {
				try {
					this.insert(item);
				}
				catch (Exception error) {
					log.log(Level.WARNING, "Inserting item '" + item.getIdentifier().getId() + "' failed!", error);
					return 1;
				}

				return 0;
			};

			final Optional<Integer> numfailed = items.map(inserter)
					.reduce((i1, i2) -> i1 + i2);

			if (!numfailed.isPresent())
				return;   // Stream was empty, no items inserted!

			if (numfailed.get() > 0)
				throw new BWFLAException("Inserting " + numfailed.get() + " item(s) failed!");
		}
	}

	private static class EmilEnvironmentSink implements ItemSink
	{
		private final Logger log = Logger.getLogger(this.getClass().getName());
		private final EmilEnvironmentRepository environmentRepository;


		public EmilEnvironmentSink(EmilEnvironmentRepository environmentRepository)
		{
			this.environmentRepository = environmentRepository;
		}

		@Override
		public void insert(ItemDescription item) throws BWFLAException
		{
			try {
				log.severe(item.getMetaData());
				final EmilEnvironment environment = EmilEnvironment.fromValue(item.getMetaData(), EmilEnvironment.class);
				environment.setArchive("remote");
				environmentRepository.save(environment, false);
			}
			catch (JAXBException error) {
				throw new BWFLAException(error);
			}
		}

		@Override
		public void insert(Stream<ItemDescription> items) throws BWFLAException
		{
			final Function<ItemDescription, Integer> inserter = (item) -> {
				try {
					this.insert(item);
				}
				catch (Exception error) {
					log.log(Level.WARNING, "Inserting item '" + item.getIdentifier().getId() + "' failed!", error);
					return 1;
				}

				return 0;
			};

			final Optional<Integer> numfailed = items.map(inserter)
					.reduce((i1, i2) -> i1 + i2);

			if (!numfailed.isPresent())
				return;

			if (numfailed.get() > 0)
				throw new BWFLAException("Inserting " + numfailed.get() + " item(s) failed!");
		}
	}


	private MetaDataSinks()
	{
		// Empty!
	}
}
