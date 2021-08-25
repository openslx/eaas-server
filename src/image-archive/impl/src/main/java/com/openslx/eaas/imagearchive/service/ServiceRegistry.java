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

package com.openslx.eaas.imagearchive.service;

import com.openslx.eaas.imagearchive.AbstractRegistry;
import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.BlobKind;
import com.openslx.eaas.imagearchive.service.impl.AliasingService;
import com.openslx.eaas.imagearchive.service.impl.CheckpointService;
import com.openslx.eaas.imagearchive.service.impl.ContainerService;
import com.openslx.eaas.imagearchive.service.impl.ImageService;
import com.openslx.eaas.imagearchive.service.impl.ImportService;
import com.openslx.eaas.imagearchive.service.impl.MachineService;
import com.openslx.eaas.imagearchive.service.impl.MetaDataService;
import com.openslx.eaas.imagearchive.service.impl.RomService;
import com.openslx.eaas.imagearchive.service.impl.TemplateService;
import de.bwl.bwfla.common.exceptions.BWFLAException;


public class ServiceRegistry extends AbstractRegistry<AbstractService<?>>
{
	public AliasingService aliases()
	{
		return this.lookup(BlobKind.ALIASING, AliasingService.class);
	}

	public MetaDataService environments()
	{
		return this.lookup(BlobKind.ENVIRONMENT, MetaDataService.class);
	}

	public MetaDataService sessions()
	{
		return this.lookup(BlobKind.SESSION, MetaDataService.class);
	}

	public MetaDataService networks()
	{
		return this.lookup(BlobKind.NETWORK, MetaDataService.class);
	}

	public ContainerService containers()
	{
		return this.lookup(BlobKind.CONTAINER, ContainerService.class);
	}

	public MachineService machines()
	{
		return this.lookup(BlobKind.MACHINE, MachineService.class);
	}

	public TemplateService templates()
	{
		return this.lookup(BlobKind.TEMPLATE, TemplateService.class);
	}

	public CheckpointService checkpoints()
	{
		return this.lookup(BlobKind.CHECKPOINT, CheckpointService.class);
	}

	public ImageService images()
	{
		return this.lookup(BlobKind.IMAGE, ImageService.class);
	}

	public RomService roms()
	{
		return this.lookup(BlobKind.ROM, RomService.class);
	}

	public ImportService imports()
	{
		return imports;
	}

	@Override
	public void close() throws Exception
	{
		super.close();
		imports.close();
	}

	public static ServiceRegistry create(ArchiveBackend backend) throws BWFLAException
	{
		final var registry = new ServiceRegistry();
		registry.insert(AliasingService.create(backend, registry));
		registry.insert(MetaDataService.create(BlobKind.ENVIRONMENT, backend));
		registry.insert(MetaDataService.create(BlobKind.SESSION, backend));
		registry.insert(MetaDataService.create(BlobKind.NETWORK, backend));
		registry.insert(ContainerService.create(backend));
		registry.insert(MachineService.create(backend));
		registry.insert(TemplateService.create(backend));
		registry.insert(CheckpointService.create(backend));
		registry.insert(ImageService.create(backend));
		registry.insert(RomService.create(backend));
		registry.insert(ImportService.create(backend));
		return registry;
	}


	// ===== Internal Helpers ==============================

	private ImportService imports;

	private ServiceRegistry()
	{
		super();
	}

	private void insert(AbstractService<?> service)
	{
		super.insert(service.kind(), service);
	}

	private void insert(ImportService service)
	{
		this.imports = service;
	}
}
