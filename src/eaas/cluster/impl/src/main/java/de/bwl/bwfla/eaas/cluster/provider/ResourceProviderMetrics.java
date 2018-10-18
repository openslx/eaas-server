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

package de.bwl.bwfla.eaas.cluster.provider;

import javax.json.stream.JsonGenerator;

import de.bwl.bwfla.eaas.cluster.dump.DumpConfig;
import de.bwl.bwfla.eaas.cluster.dump.DumpTrigger;
import de.bwl.bwfla.eaas.cluster.dump.IDumpable;
import de.bwl.bwfla.eaas.cluster.dump.ObjectDumper;

// package-private


class ResourceProviderMetrics implements ResourceProvider.Metrics, IDumpable
{
	private int numRequestsTotal;
	private int numRequestsDeferred;
	private int numRequestsFailed;
	private int numRequestsExpired;

	public ResourceProviderMetrics()
	{
		this.numRequestsTotal = 0;
		this.numRequestsDeferred = 0;
		this.numRequestsFailed = 0;
		this.numRequestsExpired = 0;
	}

	public void reset()
	{
		numRequestsTotal = 0;
		numRequestsDeferred = 0;
		numRequestsFailed = 0;
		numRequestsExpired = 0;
	}

	public void requested()
	{
		++numRequestsTotal;
	}
	
	public void deferred()
	{
		++numRequestsDeferred;
	}

	public void failed()
	{
		++numRequestsFailed;
	}

	public void expired()
	{
		++numRequestsExpired;
	}

	@Override
	public void dump(JsonGenerator json, DumpConfig dconf, int flags)
	{
		final DumpTrigger trigger = new DumpTrigger(dconf);
		trigger.setResourceDumpHandler(() -> {
			final ObjectDumper dumper = new ObjectDumper(json, dconf, flags, this.getClass());
			dumper.add(DumpFields.NUM_REQUESTS, () -> {
				json.write(DumpFields.NUM_REQUESTS, this.getNumRequests());
			});

			dumper.add(DumpFields.NUM_REQUESTS_DEFERRED, () -> {
				json.write(DumpFields.NUM_REQUESTS_DEFERRED, this.getNumRequestsDeferred());
			});

			dumper.add(DumpFields.NUM_REQUESTS_EXPIRED, () -> {
				json.write(DumpFields.NUM_REQUESTS_EXPIRED, this.getNumRequestsExpired());
			});

			dumper.add(DumpFields.NUM_REQUESTS_FAILED, () -> {
				json.write(DumpFields.NUM_REQUESTS_FAILED, this.getNumRequestsFailed());
			});

			dumper.run();
		});
		
		trigger.run();
	}
	
	private static class DumpFields
	{
		private static final String NUM_REQUESTS           = "num_requests";
		private static final String NUM_REQUESTS_DEFERRED  = "num_requests_deferred";
		private static final String NUM_REQUESTS_EXPIRED   = "num_requests_expired";
		private static final String NUM_REQUESTS_FAILED    = "num_requests_failed";
	}


	/* ===== Implementation of ResourceProvider.Metrics ===== */

	@Override
	public int getNumRequests()
	{
		return numRequestsTotal;
	}
	
	@Override
	public int getNumRequestsDeferred()
	{
		return numRequestsDeferred;
	}

	@Override
	public int getNumRequestsFailed()
	{
		return numRequestsFailed;
	}

	@Override
	public int getNumRequestsExpired()
	{
		return numRequestsExpired;
	}
}
