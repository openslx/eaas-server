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

package com.openslx.eaas.imagearchive.client.endpoint.v2;

import com.openslx.eaas.common.databind.Streamable;
import com.openslx.eaas.imagearchive.api.v2.common.CountOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.FetchOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.InsertOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ListOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.RangeOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ReplaceOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportRequestV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportStateV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportTargetV2;
import com.openslx.eaas.imagearchive.client.endpoint.v2.common.AbstractResourceRO;
import com.openslx.eaas.imagearchive.client.endpoint.v2.common.AbstractResourceRWM;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.TaskStack;
import de.bwl.bwfla.emucomp.api.AbstractDataResource;
import de.bwl.bwfla.emucomp.api.BindingDataHandler;
import de.bwl.bwfla.emucomp.api.ContainerConfiguration;
import de.bwl.bwfla.emucomp.api.EmulationEnvironmentHelper;
import de.bwl.bwfla.emucomp.api.Environment;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.OciContainerConfiguration;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class EnvironmentsV2
{
	private final List<AbstractResourceRWM<? extends Environment>> resources;
	private final ArchiveV2 archive;
	private final Logger logger;

	public EnvironmentsV2(ArchiveV2 archive, Logger logger)
	{
		this.resources = new ArrayList<>();
		this.archive = archive;
		this.logger = logger;

		// initialize compatible resources
		resources.add(archive.machines());
		resources.add(archive.containers());
	}


	// ===== IListable API ==============================

	public long count()
	{
		return this.count(null);
	}

	public long count(CountOptionsV2 options)
	{
		final Function<AbstractResourceRO<?>, Long> adapter = (resource) -> {
			try {
				return resource.count(options);
			}
			catch (Exception error) {
				throw new RuntimeException(error);
			}
		};

		return resources.stream()
				.map(adapter)
				.reduce(0L, Long::sum);
	}

	public boolean exists(String id)
	{
		final Predicate<AbstractResourceRO<?>> adapter = (resource) -> {
			try {
				return resource.exists(id);
			}
			catch (Exception error) {
				return false;
			}
		};

		return resources.stream()
				.anyMatch(adapter);
	}

	public Streamable<String> list()
	{
		return this.list(new ListOptionsV2());
	}

	public Streamable<String> list(ListOptionsV2 options)
	{
		final Fetcher<String, ListOptionsV2> fetcher = AbstractResourceRO::list;
		final var adapter = new RangeAdapter<>(fetcher, options, logger);
		final var stream = resources.stream()
				.flatMap(adapter);

		return Streamable.of(stream, adapter.cleanups());
	}


	// ===== IReadable API ==============================

	public Environment fetch(String id) throws NotFoundException
	{
		for (var resource : resources) {
			try {
				final var env = resource.fetch(id);
				if (env != null)
					return env;
			}
			catch (Exception error) {
				// Ignore it!
			}
		}

		throw new NotFoundException("Environment '" + id + "' is missing!");
	}


	// ===== IManyReadable API ==============================

	public Streamable<Environment> fetch()
	{
		return this.fetch(new FetchOptionsV2());
	}

	public Streamable<Environment> fetch(FetchOptionsV2 options)
	{
		final Fetcher<Environment, FetchOptionsV2> fetcher = AbstractResourceRWM::fetch;
		final var adapter = new RangeAdapter<>(fetcher, options, logger);
		final var stream = resources.stream()
				.flatMap(adapter);

		return Streamable.of(stream, adapter.cleanups());
	}


	// ===== IWritable API ==============================

	public String insert(Environment environment) throws BWFLAException
	{
		return this.insert(environment, null);
	}

	public String insert(Environment environment, InsertOptionsV2 options) throws BWFLAException
	{
		if (environment instanceof MachineConfiguration) {
			return archive.machines()
					.insert((MachineConfiguration) environment, options);
		}
		else if (environment instanceof ContainerConfiguration) {
			return archive.containers()
					.insert((ContainerConfiguration) environment, options);
		}

		throw EnvironmentsV2.unsupported(environment);
	}

	public void replace(String id, Environment environment) throws BWFLAException
	{
		this.replace(id, environment, null);
	}

	public void replace(String id, Environment environment, ReplaceOptionsV2 options) throws BWFLAException
	{
		if (environment instanceof MachineConfiguration) {
			archive.machines()
					.replace(id, (MachineConfiguration) environment, options);
		}
		else if (environment instanceof ContainerConfiguration) {
			archive.containers()
					.replace(id, (ContainerConfiguration) environment);
		}
		else throw EnvironmentsV2.unsupported(environment);
	}


	// ===== IDeletable API ==============================

	public void delete(String id) throws BWFLAException
	{
		this.delete(id, true, true);
	}

	public void delete(String id, boolean metadata, boolean images) throws BWFLAException
	{
		logger.info("Deleting environment '" + id + "'...");

		// NOTE: if we delete metadata, we have to delete images too!
		if (metadata || images) {
			final var environment = this.fetch(id);
			List<AbstractDataResource> data = Collections.emptyList();
			if (environment instanceof MachineConfiguration)
				data = ((MachineConfiguration) environment).getAbstractDataResource();
			else if (environment instanceof OciContainerConfiguration)
				data = ((OciContainerConfiguration) environment).getDataResources();

			for (AbstractDataResource adr : data) {
				if (!(adr instanceof ImageArchiveBinding))
					continue;

				final var binding = (ImageArchiveBinding) adr;
				final var imageid = binding.getImageId();
				if (imageid != null && !imageid.isEmpty()) {
					archive.images()
							.delete(imageid);

					logger.info("Deleted image '" + imageid + "'");
				}
			}
		}

		if (metadata) {
			// finally, delete metadata...
			for (var resource : resources) {
				if (resource.exists(id)) {
					resource.delete(id);
					break;
				}
			}

			logger.info("Deleted environment '" + id + "'");
		}
	}


	// ===== Import API ==============================

	public String insert(MachineConfiguration machine, Collection<BindingDataHandler> data) throws BWFLAException
	{
		return this.insert(machine, data, false);
	}

	public String insert(MachineConfiguration machine, Collection<BindingDataHandler> data, InsertOptionsV2 options)
			throws BWFLAException
	{
		return this.insert(machine, data, false, options);
	}

	public String insert(MachineConfiguration machine, Collection<BindingDataHandler> data, boolean checkpoint)
			throws BWFLAException
	{
		return this.insert(machine, data, checkpoint, null);
	}

	public String insert(MachineConfiguration machine, Collection<BindingDataHandler> data, boolean checkpoint, InsertOptionsV2 options)
			throws BWFLAException
	{
		logger.info("Importing new machine-environment...");

		final var imageids = new ArrayList<String>();
		try {
			// import images first
			if (data != null) {
				for (BindingDataHandler handler : data) {
					final var imageid = this.insert(handler, options);
					imageids.add(imageid);

					final var binding = new ImageArchiveBinding();
					binding.setId(handler.getId());
					binding.setImageId(imageid);

					EmulationEnvironmentHelper.replace(machine, binding, checkpoint);
					logger.info("Imported image '" + handler.getId() + "' as '" + imageid + "'");
				}
			}

			// import metadata
			final var id = archive.machines()
					.insert(machine, options);

			logger.info("Imported machine-environment '" + id + "'");

			machine.setId(id);
			return id;
		}
		catch (Exception error) {
			// remove all imported images!
			imageids.forEach((imageid) -> {
				try {
					archive.images()
							.delete(imageid);
				}
				catch (Exception exception) {
					logger.log(Level.WARNING, "Cleaning up partial import failed!", exception);
				}
			});

			if (error instanceof BWFLAException)
				throw (BWFLAException) error;
			else throw new BWFLAException(error);
		}
	}

	public void replicate(Environment environment, Collection<AbstractDataResource> data, ReplaceOptionsV2 options)
			throws BWFLAException
	{
		logger.info("Replicating environment '" + environment.getId() + "'...");

		class ImportTask
		{
			public String taskid;
			public ImageArchiveBinding binding;
		}

		final var imageids = new ArrayList<String>();
		final var tasks = new ArrayList<ImportTask>();
		try {
			if (data == null)
				data = Collections.emptyList();

			// submit all image import-requests at once...
			for (AbstractDataResource adr : data) {
				if (adr instanceof ImageArchiveBinding) {
					final var binding = (ImageArchiveBinding) adr;
					final var request = new ImportRequestV2();
					request.source()
							.setUrl(binding.getUrl());

					request.target()
							.setKind(ImportTargetV2.Kind.IMAGE)
							.setName(binding.getImageId());

					final var task = new ImportTask();
					task.binding = binding;
					task.taskid = archive.imports()
							.insert(request);

					tasks.add(task);
				}
			}

			// collect all import results...
			for (var task : tasks) {
				final var result = archive.imports()
						.watch(task.taskid)
						.get(1L, TimeUnit.HOURS);

				if (result.state() != ImportStateV2.FINISHED)
					throw new BWFLAException("Importing image failed!");

				final var imageid = result.target()
						.name();

				final var binding = task.binding;
				binding.setImageId(imageid);
				binding.setBackendName(null);
				binding.setType(null);
				binding.setUrl(null);

				imageids.add(imageid);
			}

			// import metadata
			this.replace(environment.getId(), environment, options);
			logger.info("Replicated environment '" + environment.getId() + "'");
		}
		catch (Exception error) {
			// abort pending import tasks!
			tasks.forEach((task) -> {
				try {
					archive.imports()
							.delete(task.taskid);
				}
				catch (Exception exception) {
					logger.log(Level.WARNING, "Aborting pending import failed!", exception);
				}
			});

			// remove all imported images!
			imageids.forEach((imageid) -> {
				try {
					archive.images()
							.delete(imageid);
				}
				catch (Exception exception) {
					logger.log(Level.WARNING, "Cleaning up partial import failed!", exception);
				}
			});

			if (error instanceof BWFLAException)
				throw (BWFLAException) error;
			else throw new BWFLAException(error);
		}
	}


	// ===== Internal Helpers ==============================

	private interface Fetcher<T, O extends RangeOptionsV2<?>>
	{
		Streamable<? extends T> fetch(AbstractResourceRWM<? extends Environment> resource, O options)
				throws Exception;
	}

	private static class RangeAdapter<T, O extends RangeOptionsV2<?>>
			implements Function<AbstractResourceRWM<? extends Environment>, Stream<T>>
	{
		private final Fetcher<? extends T, O> fetcher;
		private final TaskStack cleanups;
		private final O options;

		public RangeAdapter(Fetcher<? extends T, O> fetcher, O options, Logger logger)
		{
			this.cleanups = new TaskStack(logger);
			this.fetcher = fetcher;
			this.options = options;
		}

		@Override
		public Stream<T> apply(AbstractResourceRWM<? extends Environment> resource)
		{
			try {
				// all records returned?
				if (options.limit() <= 0)
					return null;

				final var copts = new CountOptionsV2()
						.setLocation(options.location());

				final var count = (int) resource.count(copts);
				if (count <= options.offset()) {
					options.setOffset(options.offset() - count);
					return null;  // skip all records!
				}

				// fetch next batch of records
				final var streamable = fetcher.fetch(resource, options);
				cleanups.push("close-range-batch", streamable::close);

				// update range on access
				options.setOffset(0);
				return streamable.stream()
						.peek(this::update)
						.map((record) -> (T) record);
			}
			catch (Exception error) {
				throw new RuntimeException(error);
			}
		}

		public TaskStack cleanups()
		{
			return cleanups;
		}

		private void update(T record)
		{
			var limit = options.limit() - 1;
			if (limit == 0)
				limit = -1;  // 0 is a special default!

			options.setLimit(limit);
		}
	}

	private static IllegalArgumentException unsupported(Environment environment)
	{
		final var type = environment.getClass()
				.getSimpleName();

		return new IllegalArgumentException("Environments of type '" + type + "' are not yet supported!");
	}

	private String insert(BindingDataHandler handler, InsertOptionsV2 options) throws Exception
	{
		if (handler.getData() != null) {
			// import image from some stream
			final var data = handler.getData();
			try (final var input = data.getInputStream()) {
				return archive.images()
						.insert(input, options);
			}
		}

		// import image from some URL...
		final var srcurl = handler.getUrl();
		if (srcurl == null || srcurl.isEmpty())
			throw new IllegalArgumentException("Invalid data source URL!");

		final var request = new ImportRequestV2();
		request.source()
				.setUrl(handler.getUrl());

		final var target = request.target()
				.setKind(ImportTargetV2.Kind.IMAGE);

		if (options != null)
			target.setLocation(options.location());

		return archive.imports()
				.await(request, 1, TimeUnit.HOURS);
	}
}
