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

package de.bwl.bwfla.blobstore;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Random;


public class BaseTest
{
	protected static BlobStore BLOBSTORE;
	protected Bucket bucket;


	@BeforeClass
	public static void initialize() throws BWFLAException
	{
		final String endpoint = System.getProperty("blobstore-endpoint");
		final String accesskey = System.getProperty("blobstore-access-key");
		final String secretkey = System.getProperty("blobstore-secret-key");

		BLOBSTORE = BlobStore.builder()
				.endpoint(endpoint)
				.credentials(accesskey, secretkey)
				.build();

	}

	@Before
	public void setup() throws BWFLAException
	{
		// NOTE: since tests can run in parallel, create a random bucket each time!
		final String name = System.getProperty("blobstore-bucket", "eaas-test")
				+ "-" + BaseTest.newRandomString(8);

		bucket = BLOBSTORE.bucket(name);
		if (bucket.exists())
			bucket.remove(true);

		bucket.create();
	}

	@After
	public void terdown() throws BWFLAException
	{
		if (bucket.exists())
			bucket.remove(true);
	}

	protected static String newRandomString(int length)
	{
		final var builder = new StringBuilder(length);
		final var alphabet = "0123456789abcdefghkmnopqrstuvwxyz";
		new Random()
				.ints(length, 0, alphabet.length())
				.forEach((i) -> builder.append(alphabet.charAt(i)));

		return builder.toString();
	}
}
