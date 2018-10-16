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

package de.bwl.bwfla.common.utils;

import java.io.Serializable;



public class Pair<A, B> implements Serializable
{
	private static final long	serialVersionUID	= -6745192997731214482L;
	private A					a;
	private B					b;

	public Pair(A a, B b)
	{
		this.a = a;
		this.b = b;
	}

	public A getA()
	{
		return a;
	}

	public void setA(A a)
	{
		this.a = a;
	}

	public B getB()
	{
		return b;
	}

	public void setB(B b)
	{
		this.b = b;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Pair))
			return false;

		Pair<?, ?> other = (Pair<?, ?>) obj;
		return this.a.equals(other.a) && this.b.equals(other.b);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		return result;
	}
	
	public int getHashCode()
	{
		return this.hashCode();
	}
}