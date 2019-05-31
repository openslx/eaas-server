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

import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
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
	public static MetaDataSource images(String archive, DatabaseEnvironmentsAdapter db, Executor executor)
	{
		return new MetaDataSource()
				.set(new EnvironmentIdentifierSource(archive, db, executor))
				.set(new EnvironmentSource(archive, db, executor));
	}

    public static MetaDataSource environments(EmilEnvironmentRepository environmentRepository, Executor executor) {
		return new MetaDataSource()
				.set(new EmilEnvironmentIdentifierSource(environmentRepository, executor))
				.set(new EmilEnvironmentSource(environmentRepository, executor));
    }

	public static MetaDataSource software(String archive, /*FIXME*/ Object db, Executor executor)
	{
		return new MetaDataSource()
				.set(new SoftwareIdentifierSource(archive, db, executor))
				.set(new SoftwareSource(archive, db, executor));
	}


    // ========== MetaDataSource Implementations =========================

	private static class AbstractEnvironmentSource
	{
		private final String archive;
		private final Executor executor;
		private final DatabaseEnvironmentsAdapter db;

		protected AbstractEnvironmentSource(String archive, DatabaseEnvironmentsAdapter db, Executor executor)
		{
			this.archive = archive;
			this.executor = executor;
			this.db = db;
		}

		protected CompletableFuture<Environment> findEnvironment(String id)
		{
			final Supplier<Environment> supplier = () -> {
				try {
					return db.getEnvironmentById(archive, id);
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
				final MongodbEaasConnector.FilterBuilder filter = new MongodbEaasConnector.FilterBuilder();
				if (options.hasFrom())
					filter.withFromTime(Environment.Fields.TIMESTAMP, options.from());

				if (options.hasUntil())
					filter.withUntilTime(Environment.Fields.TIMESTAMP, options.until(), true);

				return db.listEnvironments(archive, options.offset(), options.count(), filter);
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}

		public CompletableFuture<Integer> count()
		{
			return CompletableFuture.supplyAsync(() -> (int) db.countEnvironments(archive), executor);
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

		public CompletableFuture<Integer> count()
		{
			final MongodbEaasConnector.FilterBuilder filter = new MongodbEaasConnector.FilterBuilder();
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

		public EnvironmentIdentifierSource(String archive, DatabaseEnvironmentsAdapter db, Executor executor)
		{
			super(archive, db, executor);
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

		public EnvironmentSource(String archive, DatabaseEnvironmentsAdapter db, Executor executor)
		{
			super(archive, db, executor);
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
		private final String archive;
		private final Executor executor;
		private final Object db; /*FIXME*/

		protected AbstractSoftwareSource(String archive, /*FIXME*/ Object db, Executor executor)
		{
			this.archive = archive;
			this.executor = executor;
			this.db = db;
		}

		protected CompletableFuture<SoftwarePackage> findSoftware(String id)
		{
			final Supplier<SoftwarePackage> supplier = () -> {
				try {
					/* FIXME: return db.getSofwarePackage(archive, id); */
					return null;
				}
				catch (Exception error) {
					throw new CompletionException("Finding software failed!", error);
				}
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}

		protected CompletableFuture<Stream<SoftwarePackage>> listSoftware(QueryOptions options)
		{
			final Supplier<Stream<SoftwarePackage>> supplier = () -> {
				final MongodbEaasConnector.FilterBuilder filter = new MongodbEaasConnector.FilterBuilder();
				if (options.hasFrom())
					filter.withFromTime(Environment.Fields.TIMESTAMP, options.from());

				if (options.hasUntil())
					filter.withUntilTime(Environment.Fields.TIMESTAMP, options.until(), true);

				/* FIXME: return db.listSoftwarePackages(archive, options.offset(), options.count(), filter); */
				return null;
			};

			return CompletableFuture.supplyAsync(supplier, executor);
		}

		public CompletableFuture<Integer> count()
		{
			/* FIXME: return CompletableFuture.supplyAsync(() -> (int) db.countSoftwarePackages(archive), executor); */
			return null;
		}
	}

	private static class SoftwareIdentifierSource extends AbstractSoftwareSource implements ItemIdentifierSource
	{
		private static final Function<SoftwarePackage, ItemIdentifierDescription> MAPPER = (software) -> {
			return new ItemIdentifierDescription(software.getId())
					.setTimestamp(software.getTimestamp());
		};

		public SoftwareIdentifierSource(String archive, /*FIXME*/ Object db, Executor executor)
		{
			super(archive, db, executor);
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
		private static final Function<SoftwarePackage, ItemDescription> MAPPER = (software) -> {
			try {
				return new ItemDescription(SoftwareIdentifierSource.MAPPER.apply(software))
						.setMetaData(software.value());
			}
			catch (Exception error) {
				throw new CompletionException("Constructing ItemDescription failed!", error);
			}
		};

		public SoftwareSource(String archive, /*FIXME*/ Object db, Executor executor)
		{
			super(archive, db, executor);
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
