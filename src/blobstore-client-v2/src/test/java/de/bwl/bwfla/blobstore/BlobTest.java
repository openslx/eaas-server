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
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;


public class BlobTest extends BaseTest
{
	@Test
	public void testBlobUploadDownload() throws BWFLAException, IOException
	{
		final String content = "hello world!";
		final var blob = bucket.blob("blob");

		Assert.assertFalse("blob's existence", blob.exists());

		blob.uploader()
				.stream(new ByteArrayInputStream(content.getBytes()))
				.contentType("text/plain")
				.upload();

		Assert.assertTrue("blob's existence", blob.exists());

		// full content case
		var bytes = blob.downloader()
				.download()
				.readAllBytes();

		Assert.assertEquals("blob's content", content, new String(bytes));

		final var offset = content.length() / 2;
		final var length = content.length() - offset;
		final var slice = content.substring(offset, offset + length);

		// offset+length case
		bytes = blob.downloader()
				.offset(offset)
				.length(length)
				.download()
				.readAllBytes();

		Assert.assertEquals("blob's range content", slice, new String(bytes));

		// range case
		bytes = blob.downloader()
				.range(offset, length)
				.download()
				.readAllBytes();

		Assert.assertEquals("blob's range content", slice, new String(bytes));
	}

	@Test
	public void testBlobCopy() throws BWFLAException, IOException
	{
		final String srcprefix = "source-";
		final int numsources = 5;
		final var contents = new String[numsources];

		// multi-part copy expects >5MB parts!
		final int partsize = 5*1024*1024;

		for (int i = 0; i < numsources; ++i) {
			contents[i] = String.valueOf(i)
					.repeat(partsize);

			final var blob = bucket.blob(srcprefix + i);
			blob.uploader()
					.stream(new ByteArrayInputStream(contents[i].getBytes()))
					.contentType("text/plain")
					.upload();
		}

		// single-part copy
		{
			final var blob = bucket.blob("target-single");
			blob.copier()
					.source(bucket.name(), srcprefix + 0)
					.copy();

			final var bytes = blob.downloader()
					.download()
					.readAllBytes();

			Assert.assertEquals("blob's content", contents[0], new String(bytes));
		}

		// multi-part copy
		{
			final var blob = bucket.blob("target-multi");
			final var copier = blob.copier();
			for (int i = 0; i < numsources; ++i)
				copier.source(bucket.name(), srcprefix + i);

			copier.copy();

			final var bytes = blob.downloader()
					.download()
					.readAllBytes();

			final StringBuilder content = new StringBuilder();
			for (String part : contents)
				content.append(part);

			Assert.assertEquals("blob's content", content.toString(), new String(bytes));
		}
	}

	@Test
	public void testBlobPresignedUrl() throws Exception
	{
		final String content = "hello world!";
		final var blob = bucket.blob("blob");

		blob.uploader()
				.stream(new ByteArrayInputStream(content.getBytes()))
				.contentType("text/plain")
				.upload();

		final var bytes = new URL(blob.newPreSignedGetUrl())
				.openConnection()
				.getInputStream()
				.readAllBytes();

		Assert.assertEquals("blob's content", content, new String(bytes));
	}

	@Test
	public void testBlobTagging() throws BWFLAException
	{
		final int numtags = 5;
		final var exptags = new HashMap<String, String>();
		for (int i = 1; i <= numtags; ++i)
			exptags.put("tag-name-" + i, "tag-value-" + i);

		final var blob = bucket.blob("blob");
		blob.uploader()
				.stream(new ByteArrayInputStream("hello world!".getBytes()))
				.contentType("text/plain")
				.upload();

		Assert.assertTrue("initial tags must be empty", blob.tags().isEmpty());

		blob.setTags(exptags);
		blob.tags()
				.forEach((name, value) -> Assert.assertEquals(exptags.get(name), value));
	}

	@Test
	public void testBlobMetaData() throws BWFLAException
	{
		final int numentries = 5;
		final var expdata = new HashMap<String, String>();
		for (int i = 1; i <= numentries; ++i)
			expdata.put("name-" + i, "value-" + i);

		final var blob = bucket.blob("blob");
		final var content = "hello world!".getBytes();
		final var contentType = "text/plain";
		final var uploader = blob.uploader()
				.stream(new ByteArrayInputStream(content))
				.contentType(contentType);

		expdata.forEach(uploader::userdata);
		uploader.upload();

		final var desc = blob.stat();
		Assert.assertEquals(blob.bucket(), desc.bucket());
		Assert.assertEquals(blob.name(), desc.name());
		Assert.assertEquals(contentType, desc.contentType());
		Assert.assertEquals(content.length, desc.size());
		Assert.assertEquals(numentries, desc.userdata().size());
		desc.userdata()
				.forEach((name, value) -> Assert.assertEquals(expdata.get(name), value));
	}

	@Test
	public void testPathBuilder()
	{
		final String[] elements = { "root", "dir-a", "dir-b", "dir-c", "blob" };
		final var fullpath = Arrays.stream(elements)
				.skip(1)
				.reduce(elements[0], (lhs,rhs) -> lhs + "/" + rhs);

		// construction
		{
			var abspath = BlobStore.path("/root");
			Assert.assertEquals(BlobStore.path("root"), abspath);
			Assert.assertEquals("root", abspath.toString());

			abspath = BlobStore.path("/", elements);
			Assert.assertEquals(BlobStore.path("", elements), abspath);
			Assert.assertEquals(fullpath, abspath.toString());
		}

		// starts-with
		{
			final var prefix = BlobStore.path(elements[0], elements[1]);
			var path = BlobStore.path(elements[0]);
			Assert.assertFalse(path.startswith(prefix));
			Assert.assertFalse(path.startswith(prefix.toString()));

			path = path.resolve(elements[1]).resolve(elements[2]);
			Assert.assertTrue(path.startswith(prefix));
			Assert.assertTrue(path.startswith(prefix.toString()));
		}

		// ends-with
		{
			final var count = elements.length;
			final var suffix = BlobStore.path(elements[count-2], elements[count-1]);
			var path = BlobStore.path(elements[0], elements[1]);
			Assert.assertFalse(path.endswith(suffix));
			Assert.assertFalse(path.endswith(suffix.toString()));

			path = BlobStore.path(fullpath);
			Assert.assertTrue(path.endswith(suffix));
			Assert.assertTrue(path.endswith(suffix.toString()));
		}

		// resolving
		{
			final var expected = BlobStore.path(elements[0], elements[1], elements[2]);
			var path = BlobStore.path(elements[0])
					.resolve(elements[1])
					.resolve(elements[2]);

			Assert.assertEquals(expected, path);

			path = BlobStore.path(elements[0])
					.resolve(BlobStore.path("/", elements[1]))
					.resolve(BlobStore.path(elements[2]));

			Assert.assertEquals(expected, path);
		}

		// normalizing
		{
			final var expected = BlobStore.path(elements[0], elements[1], elements[2]);
			final var path = BlobStore.path(elements[0])
					.resolve(".")
					.resolve(elements[1])
					.resolve("..")
					.resolve(elements[1])
					.resolve(elements[2])
					.resolve("..")
					.resolve("/xyz")
					.resolve("..")
					.resolve("..")
					.resolve(elements[1])
					.resolve(elements[2])
					.normalize();

			Assert.assertEquals(expected, path);
		}

		// siblings
		{
			final var count = elements.length;
			final var path = BlobStore.path(fullpath);
			Assert.assertEquals(elements[0], path.first().toString());
			Assert.assertEquals(elements[count-1], path.last().toString());
			Assert.assertEquals(path.subpath(0, count-1), path.parent());
			Assert.assertNull(BlobStore.path("blob").parent());
		}

		// element access
		{
			final var count = elements.length;
			final var path = BlobStore.path(fullpath);
			final var iter = path.iterator();
			Assert.assertEquals(count, path.count());
			for (int i = 0; i < count; ++i) {
				Assert.assertTrue(iter.hasNext());
				Assert.assertEquals(elements[i], iter.next().toString());
				Assert.assertEquals(elements[i], path.element(i).toString());
			}
		}

		// subpaths
		{
			final var maxcount = elements.length;
			final var path = BlobStore.path(fullpath);

			// prefixes
			for (int count = 1; count <= maxcount; ++count) {
				final var subpath = path.subpath(0, count);
				Assert.assertEquals(count, subpath.count());
				for (int i = 0; i < count; ++i)
					Assert.assertEquals(elements[i], subpath.element(i).toString());
			}

			// suffixes
			for (int start = 0; start < maxcount; ++start) {
				final var count = maxcount - start;
				final var subpath = path.subpath(start, maxcount);
				Assert.assertEquals(count, subpath.count());
				for (int i = 0; i < count; ++i)
					Assert.assertEquals(elements[start+i], subpath.element(i).toString());
			}
		}
	}
}
