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

import java.util.Comparator;

import de.bwl.bwfla.eaas.cluster.metadata.Labels;


public final class ResourceProviderComparators
{
	public static final Comparator<IResourceProvider> NAME_COMPARATOR = (p1, p2) -> {
		final String n1 = p1.getName();
		final String n2 = p2.getName();
		return n1.compareTo(n2);
	};

	public static final Comparator<IResourceProvider> RANK_COMPARATOR = (p1, p2) -> {
		final String key = Labels.RANK;
		final String r1 = p1.getLabelIndex().get(key);
		final String r2 = p2.getLabelIndex().get(key);

		// Rank values are expected to be integers.
		// To avoid parsing the integers every time,
		// compare them directly as strings...

		if (r1.length() == r2.length())
			return r1.compareTo(r2);

		// ... shorter integer strings have lower rank
		return Integer.compare(r1.length(), r2.length());
	};
}
