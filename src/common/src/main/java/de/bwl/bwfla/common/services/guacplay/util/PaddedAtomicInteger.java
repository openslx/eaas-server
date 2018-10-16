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

package de.bwl.bwfla.common.services.guacplay.util;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * An {@link AtomicInteger} with additional padding,
 * to avoid false sharing between multiple threads.
 */
public final class PaddedAtomicInteger extends AtomicInteger
{
	private static final long serialVersionUID = 2612149770864081523L;
	
	
	// The size of a cacheline in modern CPUs is about 64 bytes or less.
	// AtomicInteger has one int field, hence we need to fill 60 bytes.

	// Padding: 1 x 4 bytes
	@SuppressWarnings("unused")
	private int __padding4b;
	
	// Padding: 7 x 8 bytes = 56 bytes
	@SuppressWarnings("unused")
	private long __padding8b_1, __padding8b_2, __padding8b_3, __padding8b_4,
                 __padding8b_5, __padding8b_6, __padding8b_7;
	
	
	/** Constructor */
	public PaddedAtomicInteger(int value)
	{
		super(value);
	}
}
