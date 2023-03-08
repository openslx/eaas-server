/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.common.util;

import com.openslx.eaas.common.databind.DataUtils;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;


public class CurlTool implements AutoCloseable
{
	private final DeprecatedProcessRunner curl;
	private final StringBuilder query;
	private String url;

	public CurlTool()
	{
		this(Logger.getLogger("CURL"));
	}

	public CurlTool(Logger log)
	{
		this.query = new StringBuilder(128);
		this.curl = new DeprecatedProcessRunner()
				.setLogger(log);

		this.reset();
	}

	public CurlTool logger(Logger log)
	{
		curl.setLogger(log);
		return this;
	}

	public CurlTool url(String url)
	{
		this.url = url;
		return this;
	}

	public CurlTool authorization(String value)
	{
		return this.header("authorization", value);
	}

	public CurlTool accept(String mediatype)
	{
		return this.header("accept", mediatype);
	}

	public CurlTool header(String name, String value)
	{
		if (value == null)
			value = "";

		curl.addArgument("-H")
				.addArgument(name, ": ", value);

		return this;
	}

	public CurlTool headers(Map<String, String> headers)
	{
		headers.forEach(this::header);
		return this;
	}

	public CurlTool query(String name, String value)
	{
		if (value == null)
			throw new IllegalArgumentException();

		if (query.length() > 0)
			query.append('&');

		if (name != null) {
			query.append(name);
			query.append('=');
		}

		final var encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
		query.append(encoded);
		return this;
	}

	public CurlTool query(Map<String, String> params)
	{
		params.forEach(this::query);
		return this;
	}

	public void get(Path output) throws BWFLAException
	{
		if (output == null)
			throw new IllegalArgumentException();

		this.download(output.toString(), null);
	}

	public <R> R get(ResponseBodyHandler<R> handler) throws BWFLAException
	{
		return this.download("-", handler);
	}

	public <T> void put(T value) throws BWFLAException
	{
		this.put(Payload.wrap(value));
	}

	public <T,R> R put(T value, ResponseBodyHandler<R> handler) throws BWFLAException
	{
		return this.put(Payload.wrap(value), handler);
	}

	public <T> void put(Payload<T> payload) throws BWFLAException
	{
		this.upload(UploadMethod.PUT, payload);
	}

	public <T,R> R put(Payload<T> payload, ResponseBodyHandler<R> handler) throws BWFLAException
	{
		return this.upload(UploadMethod.PUT, payload, handler);
	}

	public <T> void post(T value) throws BWFLAException
	{
		this.post(Payload.wrap(value));
	}

	public <T,R> R post(T value, ResponseBodyHandler<R> handler) throws BWFLAException
	{
		return this.post(Payload.wrap(value), handler);
	}

	public <T> void post(Payload<T> payload) throws BWFLAException
	{
		this.upload(UploadMethod.POST, payload);
	}

	public <T,R> R post(Payload<T> payload, ResponseBodyHandler<R> handler) throws BWFLAException
	{
		return this.upload(UploadMethod.POST, payload, handler);
	}

	public CurlTool reset()
	{
		curl.setCommand("curl")
				.addArgument("--fail")
				.addArgument("--globoff")
				.addArgument("--silent")
				.addArgument("--show-error")
				.addArgument("--tcp-fastopen")
				.addArgument("--location");

		query.setLength(0);
		url = null;
		return this;
	}

	@Override
	public void close()
	{
		curl.cleanup();
	}

	@FunctionalInterface
	public interface ResponseBodyHandler<R>
	{
		R accept(InputStream body) throws Exception;
	}

	public static <R> ResponseBodyHandler<R> newDiscardingBodyHandler()
	{
		return (body) -> null;
	}

	public static class Payload<T>
	{
		private final T value;
		private final String mediatype;

		private Payload(T value, String mediatype)
		{
			this.value = value;
			this.mediatype = mediatype;
		}

		public T value()
		{
			return value;
		}

		public String mediatype()
		{
			return mediatype;
		}

		public static <T> Payload<T> wrap(T value)
		{
			final var mediatype = (value instanceof InputStream) ?
					MediaType.APPLICATION_OCTET_STREAM : MediaType.APPLICATION_JSON;

			return Payload.wrap(value, mediatype);
		}

		public static <T> Payload<T> wrap(T value, String mediatype)
		{
			return new Payload<>(value, mediatype);
		}
	}


	// ===== Internal Helpers ====================

	private void append(String method, String url, String query)
	{
		if (query != null && !query.isEmpty())
			url += "?" + query;

		curl.addArguments("-X", method,  "--url", url);
	}

	private <R> R download(String output, ResponseBodyHandler<R> handler) throws BWFLAException
	{
		this.append("GET", url, query.toString());
		curl.addArguments("--output", output);

		if (!curl.start(false))
			throw new BWFLAException("Starting curl failed!");

		int rc = 0;
		R result = null;
		try {
			if (handler != null) {
				// read response body...
				try (final var response = curl.getStdOutStream()) {
					result = handler.accept(response);
				}
				catch (Exception error) {
					throw new BWFLAException("Reading response body failed!", error);
				}
			}
		}
		finally {
			rc = curl.waitUntilFinished();
			curl.printStdErr();
			curl.cleanup();
		}

		if (rc != 0)
			throw new BWFLAException("Running curl failed!");

		return result;
	}

	private enum UploadMethod
	{
		POST("--data-binary"),
		PUT("--upload-file");

		private final String argname;

		UploadMethod(String argname)
		{
			this.argname = argname;
		}

		public String argname()
		{
			return argname;
		}
	}

	private <T> void upload(UploadMethod method, Payload<T> payload) throws BWFLAException
	{
		this.upload(method, payload, CurlTool.newDiscardingBodyHandler());
	}

	private <T,R> R upload(UploadMethod method, Payload<T> payload, ResponseBodyHandler<R> handler)
			throws BWFLAException
	{
		this.header("content-type", payload.mediatype());
		this.append(method.name(), url, query.toString());
		curl.addArguments(method.argname(), "-");

		if (!curl.start(false))
			throw new BWFLAException("Starting curl failed!");

		int rc = 0;
		R result = null;
		try {
			// upload payload to destination...
			try (final var output = curl.getStdInStream()) {
				CurlTool.write(output, payload);
			}
			catch (Exception error) {
				throw new BWFLAException("Writing request payload failed!", error);
			}

			// read response body...
			try (final var response = curl.getStdOutStream()) {
				result = handler.accept(response);
			}
			catch (Exception error) {
				throw new BWFLAException("Reading response body failed!", error);
			}
		}
		finally {
			rc = curl.waitUntilFinished();
			curl.printStdErr();
			curl.cleanup();
		}

		if (rc != 0)
			throw new BWFLAException("Running curl failed!");

		return result;
	}

	private static <T> void write(OutputStream output, Payload<T> payload) throws Exception
	{
		final var value = payload.value();
		if (value instanceof InputStream) {
			final var data = (InputStream) value;
			data.transferTo(output);
		}
		else {
			final var mediatype = payload.mediatype();
			switch (mediatype) {
				case MediaType.APPLICATION_JSON:
					DataUtils.json()
							.writer()
							.writeValue(output, value);

					break;

				case MediaType.APPLICATION_XML:
					DataUtils.xml()
							.write(output, value);

					break;

				default:
					throw new IllegalArgumentException("Not supported mediatype: " + mediatype);
			}
		}
	}
}
