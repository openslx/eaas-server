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


public class MultiCounter
{
	private final int[] values;

	public MultiCounter(int length)
	{
		this.values = new int[length];
	}

	public void reset()
	{
		Arrays.fill(values, 0);
	}


	// ===== Positional Access ===============

	public int get(int i)
	{
		return values[i];
	}

	public void set(int i, int value)
	{
		values[i] = value;
	}

	public void update(int i, int delta)
	{
		values[i] += delta;
	}

	public void increment(int i)
	{
		++values[i];
	}

	public void decrement(int i)
	{
		--values[i];
	}


	// ===== Named Access ===============

	public int get(Enum<?> name)
	{
		return this.get(name.ordinal());
	}

	public void set(Enum<?> name, int value)
	{
		this.set(name.ordinal(), value);
	}

	public void update(Enum<?> name, int delta)
	{
		this.update(name.ordinal(), delta);
	}

	public void increment(Enum<?> name)
	{
		this.increment(name.ordinal());
	}

	public void decrement(Enum<?> name)
	{
		this.decrement(name.ordinal());
	}
}
