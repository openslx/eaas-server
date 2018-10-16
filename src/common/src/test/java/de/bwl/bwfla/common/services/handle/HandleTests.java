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

package de.bwl.bwfla.common.services.handle;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


public class HandleTests extends BaseTest
{
	private static final String[] URLS = new String[4];
	static {
		final String id = UUID.randomUUID().toString();
		for (int i = 0; i < URLS.length; ++i)
			URLS[i] = "http://library-" + (i + 1) + ".abc/images/" + id;
	}

	private static final String NAME_PREFIX = "__test_".toUpperCase();
	private static final String[] NAMES = new String[5];
	static {
		for (int i = 0; i < NAMES.length; ++i)
			NAMES[i] = NAME_PREFIX + (i + 1);
	}

	private static final String PREFIX = HandleUtils.getHandlePrefix();
	private static final PublicKeyAuthenticationInfo AUTHINFO;
	static {
		try {
			AUTHINFO = HandleUtils.preparePublicKeyAuthentication();
		}
		catch (Exception error) {
			throw new RuntimeException(error);
		}
	}

	@After
	public void tearDownAfter()
	{
		super.tearDownAfter();

		if (this.isCurTestPassed())
			return;

		try {
			final HandleClient client = new HandleClient(PREFIX, AUTHINFO);
			this.deletingTestHandles(client, Arrays.asList(NAMES));
		}
		catch (Exception error) {
			// Ignore it!
		}
	}
	
	@Test
	public void testCreatingHandle() throws BWFLAException
	{
		log.info("Testing creating handle...");

		// Create new handle
		final String name = NAMES[0];
		final String url = URLS[0];
		final HandleClient client = new HandleClient(PREFIX, AUTHINFO);
		final String handle = client.toHandle(name);
		client.create(name, url);
		try {
			// Ensure, handle was created
			final boolean found = client.list().stream()
					.anyMatch((entry) -> entry.contentEquals(handle));

			Assert.assertTrue("Missing handle name: " + name, found);
		}
		finally {
			client.delete(name);
		}

		this.markAsPassed();
	}

	@Test
	public void testListingHandles() throws BWFLAException
	{
		log.info("Testing listing handles...");

		// Create all handles
		final Set<String> names = new HashSet<String>();
		final HandleClient client = new HandleClient(PREFIX, AUTHINFO);
		try {
			this.creatingAllTestHandles(client, names);

			// Ensure, all handles were created
			final List<String> handles = HandleTests.getAllTestHandles(client);
			Assert.assertEquals(names.size(), handles.size());

			for (String handle : handles) {
				final String name = handle.substring(PREFIX.length() + 1);
				Assert.assertTrue("Missing handle name: " + name, names.contains(name));
			}
		}
		finally {
			this.deletingTestHandles(client, names);
		}

		this.markAsPassed();
	}

	@Test
	public void testResolvingHandleByName() throws BWFLAException
	{
		log.info("Testing resolving handle (by name)...");

		// Create new handle
		final String name = NAMES[0];
		final String url = URLS[0];
		final HandleClient client = new HandleClient(PREFIX, AUTHINFO);
		client.create(name, url);
		try {
			// Check resolved URL
			final List<String> urls = client.resolve(name);
			Assert.assertEquals(1, urls.size());
			Assert.assertEquals(url, urls.get(0));
		}
		finally {
			client.delete(name);
		}

		this.markAsPassed();
	}

	@Test
	public void testResolvingHandleByIndex() throws BWFLAException
	{
		log.info("Testing resolving handle (by index)...");

		// Create new handle
		final String name = NAMES[0];
		final HandleClient client = new HandleClient(PREFIX, AUTHINFO);
		client.create(name, URLS);
		try {
			Assert.assertTrue(URLS.length >= 3);

			final int[] indexes = { 1, URLS.length - 1 };

			// Check resolved URL
			final List<String> urls = client.resolve(name, indexes);
			Assert.assertEquals(indexes.length, urls.size());
			for (int i = 0; i < indexes.length; ++i)
				Assert.assertEquals(URLS[indexes[i] - 1], urls.get(i));
		}
		finally {
			client.delete(name);
		}

		this.markAsPassed();
	}

	@Test
	public void testAddingHandleValues() throws BWFLAException
	{
		log.info("Testing adding values to handle...");

		// Create new handle
		final String name = NAMES[0];
		final HandleClient client = new HandleClient(PREFIX, AUTHINFO);
		client.create(name, URLS[0]);
		try {
			// Add new URLs to created handle
			for (int i = 1; i < URLS.length; ++i) {
				final int index = 1 + i;
				client.add(name, index, URLS[i]);

				// Check resolved URLs
				final List<String> urls = client.resolve(name);
				Assert.assertEquals(index, urls.size());
				for (int j = 0; j < index; ++j)
					Assert.assertEquals(URLS[j], urls.get(j));
			}
		}
		finally {
			client.delete(name);
		}

		this.markAsPassed();
	}

	@Test
	public void testUpdatingHandle() throws BWFLAException
	{
		log.info("Testing updating handle...");

		final String name = NAMES[0];
		final String[] origurls = new String[URLS.length];
		Arrays.fill(origurls, "==placeholder==");

		// Create new handle with multiple URLs
		final HandleClient client = new HandleClient(PREFIX, AUTHINFO);
		client.create(name, origurls);
		try {
			// Update handle's URLs
			for (int i = 0; i < URLS.length; ++i) {
				final int index = 1 + i;
				client.update(name, index, URLS[i]);

				// Check resolved URLs
				final List<String> urls = client.resolve(name);
				for (int j = 0; j < index; ++j)
					Assert.assertEquals(URLS[j], urls.get(j));
			}
		}
		finally {
			client.delete(name);
		}

		this.markAsPassed();
	}

	@Test
	public void testRemovingHandleValues() throws BWFLAException
	{
		log.info("Testing removing handle values...");

		// Create new handle with multiple URLs
		final String name = NAMES[0];
		final HandleClient client = new HandleClient(PREFIX, AUTHINFO);
		client.create(name, URLS);
		try {
			// Update handle's URLs
			for (int index = URLS.length; index > 1; ) {
				client.remove(name, index);
				--index;

				// Check resolved URLs
				final List<String> urls = client.resolve(name);
				Assert.assertEquals(index, urls.size());
				for (int j = 0; j < index; ++j)
					Assert.assertEquals(URLS[j], urls.get(j));
			}
		}
		finally {
			client.delete(name);
		}

		this.markAsPassed();
	}

	@Test
	public void testDeletingHandles() throws BWFLAException
	{
		log.info("Testing deleting handles...");

		// Create all handles
		final Set<String> names = new HashSet<String>();
		final HandleClient client = new HandleClient(PREFIX, AUTHINFO);
		try {
			this.creatingAllTestHandles(client, names);

			for (Iterator<String> iter = names.iterator(); iter.hasNext(); ) {
				client.delete(iter.next());
				iter.remove();

				// Check, that correct handle was deleted
				final List<String> handles = HandleTests.getAllTestHandles(client);
				Assert.assertEquals(names.size(), handles.size());
				for (String handle : handles) {
					final String name = handle.substring(PREFIX.length() + 1);
					Assert.assertTrue("Missing handle name: " + name, names.contains(name));
				}
			}
		}
		finally {
			this.deletingTestHandles(client, names);
		}

		this.markAsPassed();
	}

	
	/* ========================= INTERNAL STUFF ========================= */

	private void creatingAllTestHandles(HandleClient client, Collection<String> names) throws BWFLAException
	{
		for (String name : NAMES) {
			client.create(name, URLS[0]);
			names.add(name);
		}
	}

	private void deletingTestHandles(HandleClient client, Collection<String> names) throws BWFLAException
	{
		for (String name : names)
			client.delete(name);
	}

	private static List<String> getAllTestHandles(HandleClient client) throws BWFLAException
	{
		final List<String> handles = client.list().stream()
				.filter((handle) -> handle.contains(NAME_PREFIX))
				.collect(Collectors.toList());

		return handles;
	}

	private static List<String> toHandleNames(List<String> handles) throws BWFLAException
	{
		final List<String> names = handles.stream()
				.map((handle) -> handle.substring(PREFIX.length() + 1))
				.collect(Collectors.toList());

		return names;
	}
}
