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
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class BucketTest extends BaseTest
{
	@Test
	public void testBucketExistence() throws BWFLAException
	{
		Assert.assertTrue("bucket created", bucket.exists());

		final var blobstore = bucket.storage();
		var count = blobstore.list()
				.filter((name) -> name.equals(bucket.name()))
				.count();

		Assert.assertEquals("bucket name listable", 1L, count);

		count = blobstore.buckets()
				.filter((entry) -> entry.name().equals(bucket.name()))
				.count();

		Assert.assertEquals("bucket listable", 1L, count);

		bucket.remove();
		Assert.assertFalse("bucket removed", bucket.exists());
	}

	@Test
	public void testBucketRemoval() throws BWFLAException
	{
		final String edir = "even";
		final String odir = "odd";
		final int numblobs = 2 * 3;

		final var oddnames = new ArrayList<String>();

		// upload even/odd objects to two different directories
		for (int i = 1; i <= numblobs; ++i) {
			final var content = new ByteArrayInputStream(("content-" + i).getBytes());
			final var prefix = (i % 2 == 0) ? edir : odir;
			final var name = prefix + "/blob-" + i;
			bucket.blob(name)
					.uploader()
					.stream(content)
					.contentType("text/plain")
					.upload();

			if (name.startsWith(odir))
				oddnames.add(name);
		}

		Assert.assertEquals("blobs uploaded", numblobs/2, bucket.list(edir).count());
		Assert.assertEquals("blobs uploaded", numblobs/2, bucket.list(odir).count());

		bucket.remove(oddnames);
		Assert.assertEquals("subdir removed", 0, bucket.list(odir).count());
		Assert.assertEquals("subdir exists", numblobs / 2, bucket.list(edir).count());

		bucket.remove(true);
		Assert.assertFalse("bucket removed", bucket.exists());
	}

	@Test
	public void testBucketTagging() throws BWFLAException
	{
		final int numtags = 5;
		final var exptags = new HashMap<String, String>();
		for (int i = 1; i <= numtags; ++i)
			exptags.put("tag-name-" + i, "tag-value-" + i);

		Assert.assertTrue("initial tags must be empty", bucket.tags().isEmpty());

		bucket.setTags(exptags);

		final var tags = bucket.tags();
		Assert.assertEquals(numtags, exptags.size());
		tags.forEach((name, value) -> Assert.assertEquals(exptags.get(name), value));
	}
}
