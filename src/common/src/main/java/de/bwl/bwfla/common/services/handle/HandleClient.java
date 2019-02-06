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
import net.handle.hdllib.AbstractRequest;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.AddValueRequest;
import net.handle.hdllib.Common;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.DeleteHandleRequest;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.ModifyValueRequest;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.RemoveValueRequest;
import net.handle.hdllib.ResolutionRequest;
import net.handle.hdllib.ResolutionResponse;
import net.handle.hdllib.Resolver;
import net.handle.hdllib.ScanCallback;
import net.handle.hdllib.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class HandleClient
{
	private final PublicKeyAuthenticationInfo pubKeyAuthInfo;
	private final HandleResolver resolver;
	private final byte[] encodedAdminRecord;
	private final String prefix;

	public static final int INDEX_INVALID = -1;
	public static final int INDEX_FIRST_URL_RECORD = 1;
	public static final int INDEX_ADMIN_RECORD = 100;
	public static final int TTL_ADMIN_RECORD = (int) TimeUnit.SECONDS.convert(24L, TimeUnit.HOURS);
	public static final int TTL_URL_RECORD = (int) TimeUnit.SECONDS.convert(1L, TimeUnit.HOURS);


	public HandleClient() throws BWFLAException
	{
		this(HandleUtils.getHandlePrefix(), HandleUtils.preparePublicKeyAuthentication());
	}

	public HandleClient(PublicKeyAuthenticationInfo pubKeyAuthInfo)
	{
		this(HandleUtils.getHandlePrefix(), pubKeyAuthInfo);
	}

	public HandleClient(String prefix, PublicKeyAuthenticationInfo pubKeyAuthInfo)
	{
		if (prefix == null || prefix.isEmpty())
			throw new IllegalArgumentException("Invalid prefix!");

		this.pubKeyAuthInfo = pubKeyAuthInfo;
		this.resolver = new HandleResolver();
		this.encodedAdminRecord = HandleUtils.newEncodedAdminRecord(pubKeyAuthInfo);
		this.prefix = prefix;
	}

	/** Lists all handles registered under client's prefix. */
	public List<String> list() throws HandleException
	{
		final List<String> handles = new ArrayList<String>();
		try {
			final ScanCallback callback = (byte[] bytes) -> handles.add(Util.decodeString(bytes));
			resolver.listHandlesUnderPrefix(prefix, pubKeyAuthInfo, callback);
		}
		catch (net.handle.hdllib.HandleException error) {
			throw new HandleException("Listing all handles failed!", error);
		}

		return handles;
	}

	/** Creates a new handle and registers specified URLs under it. */
	public void create(String name, String... urls) throws HandleException
	{
		final UrlEntry[] entries = new UrlEntry[urls.length];
		for (int i = 0; i < urls.length; ++i)
			entries[i] = new UrlEntry(INDEX_FIRST_URL_RECORD + i, urls[i]);

		this.create(name, entries);
	}

	/** Creates a new handle and registers specified URL under it. */
	public void create(String name, int index, String url) throws HandleException
	{
		this.create(name, new UrlEntry(index, url));
	}

	/** Creates a new handle and registers specified URLs under it. */
	public void create(String name, UrlEntry... entries) throws HandleException
	{
		final String handle = this.toHandle(name);
		final int timestamp = HandleUtils.timestamp();

		// Prepare new handle's value-records
		final HandleValue[] values = new HandleValue[entries.length + 1];
		for (int i = 0; i < entries.length; ++i) {
			final UrlEntry entry = entries[i];
			values[i] = HandleUtils.newUrlHandleValue(entry.index(), entry.url(), timestamp, TTL_URL_RECORD);
		}

		values[entries.length] = HandleUtils.newAdminHandleValue(encodedAdminRecord, timestamp, TTL_ADMIN_RECORD);

		final CreateHandleRequest request = new CreateHandleRequest(Util.encodeString(handle), values, pubKeyAuthInfo);
		this.process(request, "Creating handle '" + handle + "' failed!");
	}

	/** Resolves specified handle name and returns all corresponding URLs. */
	public List<String> resolve(String name) throws HandleException
	{
		final List<String> urls = new ArrayList<String>();
		final String handle = this.toHandle(name);
		final byte[][] types = { Common.STD_TYPE_URL };
		final ResolutionRequest request = new ResolutionRequest(Util.encodeString(handle), types, null, pubKeyAuthInfo);
		final ResolutionResponse response = (ResolutionResponse) this.process(request, "Resolving URLs for handle '" + handle + "' failed!");
		try {
			for (HandleValue value : response.getHandleValues())
				urls.add(value.getDataAsString());
		}
		catch (net.handle.hdllib.HandleException error) {
			throw new HandleException("Parsing values for handle '" + handle + "' failed!", error);
		}

		return urls;
	}

	/** Resolves specified handle name and returns corresponding URLs at specified indexes. */
	public List<String> resolve(String name, int... indexes) throws HandleException
	{
		final List<String> urls = new ArrayList<String>();
		final String handle = this.toHandle(name);
		final ResolutionRequest request = new ResolutionRequest(Util.encodeString(handle), null, indexes, pubKeyAuthInfo);
		final ResolutionResponse response = (ResolutionResponse) this.process(request, "Resolving URLs for handle '" + handle + "' failed!");
		try {
			for (HandleValue value : response.getHandleValues())
				urls.add(value.getDataAsString());
		}
		catch (net.handle.hdllib.HandleException error) {
			throw new HandleException("Parsing values for handle '" + handle + "' failed!", error);
		}

		return urls;
	}

	/** Adds new URL value to handle, finding next free index. */
	public void add(String name, String url) throws HandleException
	{
		final HandleValue[] values = this.list(name);
		final int index = this.findFreeIndex(values);
		this.add(name, index, url);
	}

	/** Adds new URL value to handle. */
	public void add(String name, int index, String url) throws HandleException
	{
		final String handle = this.toHandle(name);
		final int timestamp = HandleUtils.timestamp();
		final HandleValue value = HandleUtils.newUrlHandleValue(index, url, timestamp, TTL_URL_RECORD);
		final AddValueRequest request = new AddValueRequest(Util.encodeString(handle), value, pubKeyAuthInfo);
		this.process(request, "Adding new entry to handle '" + handle + "' failed!");
	}

	/** Updates handle's value with new URL. */
	public void update(String name, String newurl) throws HandleException
	{
		final String prefix = newurl.substring(newurl.lastIndexOf("/") + 1);
		if (prefix.length() < 4)
			throw new IllegalArgumentException("Invalid URL: " + newurl);

		int index = INDEX_INVALID;

		// Find matching previous URL record...
		final HandleValue[] values = this.list(name, Common.STD_TYPE_URL);
		for (HandleValue value : values) {
			final String cururl = value.getDataAsString();
			if (cururl.startsWith(prefix)) {
				index = value.getIndex();
				break;
			}
		}

		if (index == INDEX_INVALID) {
			net.handle.hdllib.HandleException error = new net.handle.hdllib.HandleException(net.handle.hdllib.HandleException.INVALID_VALUE);
			throw new HandleException("Handle's URL prefix not found!", error);
		}

		this.update(name, index, newurl);
	}

	/** Updates handle's value with new URL. */
	public void update(String name, int index, String url) throws HandleException
	{
		final String handle = this.toHandle(name);
		final int timestamp = HandleUtils.timestamp();
		final HandleValue value = HandleUtils.newUrlHandleValue(index, url, timestamp, TTL_URL_RECORD);
		final ModifyValueRequest request = new ModifyValueRequest(Util.encodeString(handle), value, pubKeyAuthInfo);
		this.process(request, "Updating handle '" + handle + "' failed!");
	}

	/** Removes handle's values at specified indexes. */
	public void remove(String name, int... indexes) throws HandleException
	{
		final String handle = this.toHandle(name);
		final RemoveValueRequest request = new RemoveValueRequest(Util.encodeString(handle), indexes, pubKeyAuthInfo);
		this.process(request, "Removing values from handle '" + handle + "' failed!");
	}

	/** Deletes the handle. */
	public void delete(String name) throws HandleException
	{
		final String handle = this.toHandle(name);
		final DeleteHandleRequest request = new DeleteHandleRequest(Util.encodeString(handle), pubKeyAuthInfo);
		this.process(request, "Deleting handle '" + handle + "' failed!");
	}

	public String toHandle(String name)
	{
		final String prefix = this.prefix + "/";
		if (name.startsWith(prefix))
			return name;

		return prefix + name;
	}

	public static class UrlEntry
	{
		private final int index;
		private final String url;

		public UrlEntry(int index, String url)
		{
			this.index = index;
			this.url = url;
		}

		public int index()
		{
			return index;
		}

		public String url()
		{
			return url;
		}
	}


	/* ==================== Internal Helpers ==================== */

	private AbstractResponse process(AbstractRequest request, String errmsg) throws HandleException
	{
		try {
			final AbstractResponse response = resolver.processRequest(request);
			HandleUtils.checkResponseCode(response, errmsg);
			return response;
		}
		catch (net.handle.hdllib.HandleException error) {
			throw new HandleException(errmsg, error);
		}
	}

	private HandleValue[] list(String name) throws HandleException
	{
		final String handle = this.toHandle(name);
		try {
			return resolver.resolveHandle(handle);
		}
		catch (net.handle.hdllib.HandleException error) {
			throw new HandleException("Resolving values for handle '" + handle + "' failed!", error);
		}
	}

	private HandleValue[] list(String name, byte[]... types) throws HandleException
	{
		final String handle = this.toHandle(name);
		try {
			return resolver.resolveHandle(Util.encodeString(handle), types, null);
		}
		catch (net.handle.hdllib.HandleException error) {
			throw new HandleException("Resolving values for handle '" + handle + "' failed!", error);
		}
	}

	private int findFreeIndex(HandleValue[] values)
	{
		// Prepare all occupied index values
		final int[] indices = new int[values.length];
		for (int i = 0; i < values.length; ++i)
			indices[i] = values[i].getIndex();

		Arrays.sort(indices);

		// Find first free/unused index...
		int freeidx = INDEX_FIRST_URL_RECORD;
		for (int i = 0; i < indices.length; ++i) {
			final int curidx = indices[i];
			if (freeidx < curidx)
				return freeidx;

			freeidx = curidx + 1;
		}

		return freeidx;
	}
}
