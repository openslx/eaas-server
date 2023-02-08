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

package com.openslx.eaas.common.util;

import java.util.concurrent.atomic.AtomicInteger;


public class AtomicMultiCounter extends MultiCounter
{
	private final AtomicInteger[] values;

	public AtomicMultiCounter(int length)
	{
		this.values = new AtomicInteger[length];
		for (int i = 0; i < length; ++i)
			this.values[i] = new AtomicInteger(0);
	}

	@Override
	public void reset()
	{
		for (var value : values)
			value.set(0);
	}


	// ===== Positional Access ===============

	@Override
	public int get(int i)
	{
		return values[i].get();
	}

	@Override
	public void set(int i, int value)
	{
		values[i].set(value);
	}

	@Override
	public void update(int i, int delta)
	{
		values[i].addAndGet(delta);
	}

	@Override
	public void increment(int i)
	{
		values[i].incrementAndGet();
	}

	@Override
	public void decrement(int i)
	{
		values[i].decrementAndGet();
	}
}
