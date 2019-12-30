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

package de.bwl.bwfla.eaas.cluster.provider.iaas;

import java.security.SecureRandom;


/** A simple class for the node's name generation */
public class NodeNameGenerator
{
	// Name-Alphabet, skipping easy to confuse chars: i, l, o
	private static final String ALPHABET = "0123456789abcdefghjkmnpqrstuvwxyz";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final String prefix;
	private final int randlen;

	public NodeNameGenerator()
	{
		this("");
	}

	public NodeNameGenerator(String prefix)
	{
		this(prefix, 5);
	}

	public NodeNameGenerator(String prefix, int randlen)
	{
		this.prefix = prefix;
		this.randlen = randlen;
	}

	public String next()
	{
		final StringBuilder name = new StringBuilder(prefix.length() + randlen)
				.append(prefix);

		final int jmax = ALPHABET.length();

		// Generate random suffix...
		for (int i = 0; i < randlen; ++i) {
			final int j = RANDOM.nextInt(jmax);
			name.append(ALPHABET.charAt(j));
		}

		return name.toString();
	}
}
