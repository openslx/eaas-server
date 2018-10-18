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

package de.bwl.bwfla.common.services.guacplay.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public final class BusyWorkerRunner
{
	private static final int DEFAULT_NUM_WORKERS = 8;
	
	
	public static void main(String[] args) throws InterruptedException
	{
		final int numWorkers = (args.length == 1) ? Integer.parseInt(args[0]) : DEFAULT_NUM_WORKERS;
		final ExecutorService executor = Executors.newFixedThreadPool(numWorkers);
		
		for (int i = 0; i < numWorkers; ++i)
			executor.execute(new BusyWorker(i + 1));
		
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.MINUTES);
	}
}
