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

import java.util.Arrays;


public class IntegerMultiCounter extends MultiCounter
{
	private final int[] values;

	public IntegerMultiCounter(int length)
	{
		this.values = new int[length];
	}

	@Override
	public void reset()
	{
		Arrays.fill(values, 0);
	}


	// ===== Positional Access ===============

	@Override
	public int get(int i)
	{
		return values[i];
	}

	@Override
	public void set(int i, int value)
	{
		values[i] = value;
	}

	@Override
	public void update(int i, int delta)
	{
		values[i] += delta;
	}

	@Override
	public void increment(int i)
	{
		++values[i];
	}

	@Override
	public void decrement(int i)
	{
		--values[i];
	}
}
