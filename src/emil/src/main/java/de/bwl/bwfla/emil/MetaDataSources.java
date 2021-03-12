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

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.common.CountOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.FetchOptionsV2;
import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.emil.datatypes.EaasiSoftwareObject;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emucomp.api.Environment;
import de.bwl.bwfla.metadata.repository.api.ItemDescription;
import de.bwl.bwfla.metadata.repository.api.ItemIdentifierDescription;
import de.bwl.bwfla.metadata.repository.source.ItemIdentifierSource;
import de.bwl.bwfla.metadata.repository.source.ItemSource;
import de.bwl.bwfla.metadata.repository.source.MetaDataSource;
import de.bwl.bwfla.metadata.repository.source.QueryOptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class MetaDataSources
{
	public static MetaDataSource images(String archive, ImageArchiveClient imagearchive, Executor executor)
	{
		return new MetaDataSource()
				.set(new EnvironmentIdentifierSource(archive, imagearchive, executor))
				.set(new EnvironmentSource(archive, imagearchive, executor));
	}

    public static MetaDataSource environments(EmilEnvironmentRepository environmentRepository, Executor executor) {
		return new MetaDataSource()
				.set(new EmilEnvironmentIdentifierSource(environmentRepository, executor))
				.set(new EmilEnvironmentSource(environmentRepository, executor));
    }

	public static MetaDataSource software(EmilSoftwareData softwareData, Executor executor)
	{
		return new MetaDataSource()
				.set(new SoftwareIdentifierSource(softwareData, executor))
				.set(new SoftwareSource(softwareData, executor));
	}


    // ========== MetaDataSource Implementations =========================

	private static class AbstractEnvironmentSource
	{
		private final String archive;
		private final Executor executor;
		private final ImageArchiveClient imagearchive;

		protected AbstractEnvironmentSource(String archive, ImageArchiveClient imagearchive, Executor executor)
		{
			this.archive = archive;
			this.executor = executor;
			this.imagearchive = imagearchive;
		}

		protected CompletableFuture<Environment> findEnvironment(String id)
		{
			final Supplier<Environment> supplier = () -> {
				try {
					return imagearchive.api()
							.v2()
							.environments()
							.fetch(id);
				}
				catch (Exception error) {
					throw new CompletionException("Finding environment failed!", error);
				}
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}

		protected CompletableFuture<Stream<Environment>> listEnvironments(QueryOptions options)
		{
			final Supplier<Stream<Environment>> supplier = () -> {
				final var fopts = new FetchOptionsV2()
						.setLocation(archive)
						.setOffset(options.offset())
						.setLimit(options.count())
						.setFromTime(options.from())
						.setUntilTime(options.until());

				final var result = imagearchive.api()
						.v2()
						.environments()
						.fetch(fopts);

				return result.stream()
						.onClose(result::close);
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}

		public CompletableFuture<Integer> count(QueryOptions options)
		{
			final Supplier<Integer> supplier = () -> {
				final var copts = new CountOptionsV2()
						.setLocation(archive)
						.setFromTime(options.from())
						.setUntilTime(options.until());

				return (int) imagearchive.api()
					.v2()
					.environments()
					.count(copts);
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}
	}

	private static class AbstractEmilEnvironmentSource
	{
		private final Executor executor;
		private final EmilEnvironmentRepository environmentRepository;

		protected AbstractEmilEnvironmentSource(EmilEnvironmentRepository environmentRepository, Executor executor)
		{
			this.executor = executor;
			this.environmentRepository = environmentRepository;
		}

		protected CompletableFuture<EmilEnvironment> findEnvironment(String id)
		{
			final Supplier<EmilEnvironment> supplier = () -> {
				try {
					return environmentRepository.getEmilEnvironmentById(id);
				}
				catch (Exception error) {
					throw new CompletionException("Finding environment failed!", error);
				}
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}

		protected CompletableFuture<Stream<EmilEnvironment>> listEnvironments(QueryOptions options)
		{
			final Supplier<Stream<EmilEnvironment>> supplier = () -> {
				final MongodbEaasConnector.FilterBuilder filter = new MongodbEaasConnector.FilterBuilder();
				if (options.hasFrom())
					filter.withFromTime(Environment.Fields.TIMESTAMP, options.from());

				if (options.hasUntil())
					filter.withUntilTime(Environment.Fields.TIMESTAMP, options.until(), true);

				filter.eq("archive", "public");

				return environmentRepository.listPublicEnvironments(options.offset(), options.count(), filter);
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}

		public CompletableFuture<Integer> count(QueryOptions options)
		{
			final MongodbEaasConnector.FilterBuilder filter = new MongodbEaasConnector.FilterBuilder();
			if (options.hasFrom())
				filter.withFromTime(Environment.Fields.TIMESTAMP, options.from());

			if (options.hasUntil())
				filter.withUntilTime(Environment.Fields.TIMESTAMP, options.until(), true);

			filter.eq("archive", "public");
			return CompletableFuture.supplyAsync(() -> (int) environmentRepository.countPublicEnvironments(filter), executor);
		}
	}

	private static class EmilEnvironmentIdentifierSource extends AbstractEmilEnvironmentSource implements ItemIdentifierSource
	{
		private static final Function<EmilEnvironment, ItemIdentifierDescription> MAPPER = (environment) -> {
			return new ItemIdentifierDescription(environment.getEnvId())
					.setTimestamp(environment.getTimestamp());
		};

		public EmilEnvironmentIdentifierSource(EmilEnvironmentRepository environmentRepository, Executor executor)
		{
			super(environmentRepository, executor);
		}

		@Override
		public CompletableFuture<ItemIdentifierDescription> get(String id) {
			return super.findEnvironment(id)
					.thenApply(MAPPER);
		}

		@Override
		public CompletableFuture<Stream<ItemIdentifierDescription>> list(QueryOptions options) {
			return super.listEnvironments(options)
					.thenApply((environments) -> environments.map(MAPPER));
		}
	}

	private static class EnvironmentIdentifierSource extends AbstractEnvironmentSource implements ItemIdentifierSource
	{
		private static final Function<Environment, ItemIdentifierDescription> MAPPER = (environment) -> {
			return new ItemIdentifierDescription(environment.getId())
					.setTimestamp(environment.getTimestamp());
		};

		public EnvironmentIdentifierSource(String archive, ImageArchiveClient imagearchive, Executor executor)
		{
			super(archive, imagearchive, executor);
		}

		@Override
		public CompletableFuture<ItemIdentifierDescription> get(String id)
		{
			return super.findEnvironment(id)
					.thenApply(MAPPER);
		}

		@Override
		public CompletableFuture<Stream<ItemIdentifierDescription>> list(QueryOptions options)
		{
			return super.listEnvironments(options)
					.thenApply((environments) -> environments.map(MAPPER));
		}
	}

	private static class EnvironmentSource extends AbstractEnvironmentSource implements ItemSource
	{
		private static final Function<Environment, ItemDescription> MAPPER = (environment) -> {
			try {
				return new ItemDescription(EnvironmentIdentifierSource.MAPPER.apply(environment))
						.setMetaData(environment.value());
			}
			catch (Exception error) {
				throw new CompletionException("Constructing ItemDescription failed!", error);
			}
		};

		public EnvironmentSource(String archive, ImageArchiveClient imagearchive, Executor executor)
		{
			super(archive, imagearchive, executor);
		}

		@Override
		public CompletableFuture<ItemDescription> get(String id)
		{
			return super.findEnvironment(id)
					.thenApply(MAPPER);
		}

		@Override
		public CompletableFuture<Stream<ItemDescription>> list(QueryOptions options)
		{
			return super.listEnvironments(options)
					.thenApply((environments) -> environments.map(MAPPER));
		}
	}

	private static class EmilEnvironmentSource extends AbstractEmilEnvironmentSource implements ItemSource
	{
		private static final Function<EmilEnvironment, ItemDescription> MAPPER = (environment) -> {
			try {
				return new ItemDescription(EmilEnvironmentIdentifierSource.MAPPER.apply(environment))
						.setMetaData(environment.value());
			}
			catch (Exception error) {
				throw new CompletionException("Constructing ItemDescription failed!", error);
			}
		};

		public EmilEnvironmentSource(EmilEnvironmentRepository environmentRepository, Executor executor)
		{
			super(environmentRepository, executor);
		}

		@Override
		public CompletableFuture<ItemDescription> get(String id)
		{
			return super.findEnvironment(id)
					.thenApply(MAPPER);
		}

		@Override
		public CompletableFuture<Stream<ItemDescription>> list(QueryOptions options)
		{
			return super.listEnvironments(options)
					.thenApply((environments) -> environments.map(MAPPER));
		}
	}

	private static class AbstractSoftwareSource
	{
		private final EmilSoftwareData softwareData;
		private final Executor executor;


		protected AbstractSoftwareSource(EmilSoftwareData softwareData, Executor executor)
		{
			this.softwareData = softwareData;
			this.executor = executor;
		}

		protected CompletableFuture<EaasiSoftwareObject> findSoftware(String id)
		{
			final Supplier<EaasiSoftwareObject> supplier = () -> {
				try {
					EaasiSoftwareObject object =  softwareData.getSoftwareCollection().getId(id);
					return object;
				}
				catch (Exception error) {
					throw new CompletionException("Finding software failed!", error);
				}
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}

		protected CompletableFuture<Stream<EaasiSoftwareObject>> listSoftware(QueryOptions options)
		{
			final Supplier<Stream<EaasiSoftwareObject>> supplier = () -> {
				/* FIXME: we are currently returning always the full stream */
				return softwareData.getSoftwareCollection().toStream();
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}

		public CompletableFuture<Integer> count(QueryOptions options)
		{
			// FIXME: we are currently returning always the full stream
			return CompletableFuture.supplyAsync(() -> softwareData.getSoftwareCollection().size());
		}
	}

	private static class SoftwareIdentifierSource extends AbstractSoftwareSource implements ItemIdentifierSource
	{
		private static final Function<EaasiSoftwareObject, ItemIdentifierDescription> MAPPER = (eaasiSoftwareObject) -> {
			return new ItemIdentifierDescription(eaasiSoftwareObject.getSoftwarePackage().getId())
					.setTimestamp(eaasiSoftwareObject.getSoftwarePackage().getTimestamp());
		};

		public SoftwareIdentifierSource(EmilSoftwareData emilSoftwareData, Executor executor)
		{
			super(emilSoftwareData, executor);
		}

		@Override
		public CompletableFuture<ItemIdentifierDescription> get(String id)
		{
			return super.findSoftware(id)
					.thenApply(MAPPER);
		}

		@Override
		public CompletableFuture<Stream<ItemIdentifierDescription>> list(QueryOptions options)
		{
			return super.listSoftware(options)
					.thenApply((software) -> software.map(MAPPER));
		}
	}

	private static class SoftwareSource extends AbstractSoftwareSource implements ItemSource
	{
		private static final Function<EaasiSoftwareObject, ItemDescription> MAPPER = (software) -> {
			try {
				return new ItemDescription(SoftwareIdentifierSource.MAPPER.apply(software))
						.setMetaData(software.value());
			}
			catch (Exception error) {
				throw new CompletionException("Constructing ItemDescription failed!", error);
			}
		};

		public SoftwareSource(EmilSoftwareData softwareData, Executor executor)
		{
			super(softwareData, executor);
		}

		@Override
		public CompletableFuture<ItemDescription> get(String id)
		{
			return super.findSoftware(id)
					.thenApply(MAPPER);
		}

		@Override
		public CompletableFuture<Stream<ItemDescription>> list(QueryOptions options)
		{
			return super.listSoftware(options)
					.thenApply((software) -> software.map(MAPPER));
		}
	}


	private MetaDataSources()
	{
		// Empty!
	}
}
