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
import com.openslx.eaas.imagearchive.service.impl.ImageService;
import com.openslx.eaas.imagearchive.service.impl.MachineService;
import com.openslx.eaas.imagearchive.service.impl.TemplateService;


public class ServiceRegistry extends AbstractRegistry<AbstractService<?>>
{
	public MachineService machines()
	{
		return this.lookup(BlobKind.MACHINE, MachineService.class);
	}

	public TemplateService templates()
	{
		return this.lookup(BlobKind.TEMPLATE, TemplateService.class);
	}

	public ImageService images()
	{
		return this.lookup(BlobKind.IMAGE, ImageService.class);
	}

	public static ServiceRegistry create(ArchiveBackend backend)
	{
		final var registry = new ServiceRegistry();
		registry.insert(MachineService.create(backend));
		registry.insert(TemplateService.create(backend));
		registry.insert(ImageService.create(backend));
		return registry;
	}


	// ===== Internal Helpers ==============================

	private ServiceRegistry()
	{
		super();
	}

	private void insert(AbstractService<?> service)
	{
		super.insert(service.kind(), service);
	}
}
