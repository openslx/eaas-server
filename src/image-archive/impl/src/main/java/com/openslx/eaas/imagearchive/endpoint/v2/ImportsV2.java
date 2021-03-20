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

package com.openslx.eaas.imagearchive.endpoint.v2;

import com.openslx.eaas.common.databind.Streamable;
import com.openslx.eaas.imagearchive.ArchiveBackend;
import com.openslx.eaas.imagearchive.BlobKind;
import com.openslx.eaas.imagearchive.api.v2.IImportsV2;
import com.openslx.eaas.imagearchive.api.v2.common.CountOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.FetchOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ListOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.ResolveOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportFailureV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportRequestV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportSourceV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportStateV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportStatusV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportTargetV2;
import com.openslx.eaas.imagearchive.databind.ImportFailure;
import com.openslx.eaas.imagearchive.databind.ImportSource;
import com.openslx.eaas.imagearchive.databind.ImportState;
import com.openslx.eaas.imagearchive.databind.ImportStatus;
import com.openslx.eaas.imagearchive.databind.ImportTarget;
import com.openslx.eaas.imagearchive.databind.ImportTask;
import com.openslx.eaas.imagearchive.service.impl.ImportService;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;


@ApplicationScoped
public class ImportsV2 implements IImportsV2
{
	private ImportService service;


	// ===== IListable API ==============================

	@Override
	public long count(CountOptionsV2 options)
	{
		return service.count();
	}

	@Override
	public void exists(String id) throws BWFLAException
	{
		final var result = service.lookup(ImportsV2.convert(id));
		if (result == null)
			throw new NotFoundException();
	}

	@Override
	public Response list(ListOptionsV2 options) throws BWFLAException
	{
		final var result = service.list(options.offset(), options.limit());
		return Response.ok(Streamable.of(result))
				.build();
	}


	// ===== IManyReadable API ==============================

	@Override
	public String resolve(String id, ResolveOptionsV2 options) throws BWFLAException
	{
		// NOTE: access via URL is not supported!
		throw new BadRequestException();
	}

	@Override
	public ImportStatusV2 fetch(String id) throws BWFLAException
	{
		final var result = service.lookup(ImportsV2.convert(id));
		if (result == null)
			throw new NotFoundException();

		return ImportsV2.convert(result);
	}

	@Override
	public Response fetch(FetchOptionsV2 options) throws BWFLAException
	{
		final var response = service.fetch(options.offset(), options.limit())
				.map(ImportsV2::convert);

		return Response.ok(Streamable.of(response))
				.build();
	}


	// ===== IWritable API ==============================

	@Override
	public String insert(ImportRequestV2 request) throws BWFLAException
	{
		final var task = ImportsV2.convert(request);
		final var taskid = service.submit(task);
		if (taskid < 0)
			throw new InternalServerErrorException();

		return ImportsV2.convert(taskid);
	}

	@Override
	public void delete(String id) throws BWFLAException
	{
		service.abort(ImportsV2.convert(id));
	}

	@Override
	public CompletionStage<ImportStatusV2> watch(String id) throws BWFLAException
	{
		final var result = service.watch(ImportsV2.convert(id));
		if (result == null)
			throw new NotFoundException();

		return result.thenApply(ImportsV2::convert);
	}


	// ===== Internal Helpers ==============================

	@PostConstruct
	private void initialize()
	{
		this.service = ArchiveBackend.instance()
				.services()
				.imports();
	}

	private static String convert(int taskid)
	{
		return Integer.toString(taskid);
	}

	private static int convert(String taskid)
	{
		return Integer.parseUnsignedInt(taskid);
	}

	private static ImportStatusV2 convert(ImportStatus status)
	{
		return new ImportStatusV2()
				.setTaskId(ImportsV2.convert(status.taskid()))
				.setState(ImportsV2.convert(status.state()))
				.setTarget(ImportsV2.convert(status.target()))
				.setFailure(ImportsV2.convert(status.failure()));
	}

	private static ImportStateV2 convert(ImportState state)
	{
		return ImportStateV2.from(state.value());
	}

	private static ImportFailureV2 convert(ImportFailure failure)
	{
		if (failure == null)
			return null;

		return new ImportFailureV2()
				.setReason(failure.reason())
				.setDetail(failure.detail());
	}

	private static ImportTargetV2 convert(ImportTarget target)
	{
		if (target == null)
			return null;

		return new ImportTargetV2()
				.setKind(ImportsV2.convert(target.kind()))
				.setLocation(target.location())
				.setName(target.name());
	}

	private static ImportTargetV2.Kind convert(BlobKind kind)
	{
		return ImportTargetV2.Kind.from(kind.value());
	}

	private static ImportTask convert(ImportRequestV2 request)
	{
		return new ImportTask()
				.setDescription(request.description())
				.setSource(ImportsV2.convert(request.source()))
				.setTarget(ImportsV2.convert(request.target()));
	}

	private static ImportSource convert(ImportSourceV2 source)
	{
		return new ImportSource()
				.setUrl(source.url())
				.setHeaders(source.headers());
	}

	private static ImportTarget convert(ImportTargetV2 target)
	{
		return new ImportTarget()
				.setKind(ImportsV2.convert(target.kind()))
				.setLocation(target.location())
				.setName(target.name());
	}

	private static BlobKind convert(ImportTargetV2.Kind kind)
	{
		return BlobKind.from(kind.value());
	}
}
