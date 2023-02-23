/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.common.concurrent;

import java.util.logging.Logger;


public abstract class ParallelProcessor<D extends ParallelProcessor<D>>
{
	protected Logger log = Logger.getLogger(this.getClass().getSimpleName());

	private int numTasks;

	protected ParallelProcessor()
	{
		this.useFullConcurrency();
	}

	public D setLogger(Logger log)
	{
		this.log = log;
		return (D) this;
	}

	public int getNumTasks()
	{
		return numTasks;
	}

	public D setNumTasks(int num)
	{
		if (num < 1)
			throw new IllegalArgumentException();

		this.numTasks = num;
		return (D) this;
	}

	public D useFullConcurrency()
	{
		final var numcpus = Runtime.getRuntime()
				.availableProcessors();

		return this.setNumTasks(numcpus);
	}

	public D useHalfConcurrency()
	{
		final var numcpus = Runtime.getRuntime()
				.availableProcessors();

		return this.setNumTasks(Integer.max(1, numcpus / 2));
	}
}
