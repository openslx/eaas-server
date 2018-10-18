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

package de.bwl.bwfla.eaas.cluster.dump;


public class DumpFlags
{
	public static final int INLINED        = 1 << 0;
	public static final int TIMESTAMP      = 1 << 1;
	public static final int RESOURCE_TYPE  = 1 << 2;


	/* ========== Public Helpers ========== */

	public static int set(int flags, int flag)
	{
		return (flags | flag);
	}

	public static int reset(int flags, int flag)
	{
		return (flags & ~flag);
	}

	public static boolean test(int flags, int flag)
	{
		return ((flags & flag) == flag);
	}

	private DumpFlags()
	{
		// Empty!
	}
}