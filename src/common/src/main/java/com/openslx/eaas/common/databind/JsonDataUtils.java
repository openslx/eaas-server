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

package com.openslx.eaas.common.databind;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import java.io.InputStream;


public class JsonDataUtils
{
	public <T> T read(InputStream source, Class<T> clazz) throws Exception
	{
		return reader.forType(clazz)
				.readValue(source);
	}

	public <T> T read(byte[] source, int offset, int length, Class<T> clazz) throws Exception
	{
		return reader.forType(clazz)
				.readValue(source, offset, length);
	}

	public <T> T read(byte[] source, Class<T> clazz) throws Exception
	{
		return reader.forType(clazz)
				.readValue(source);
	}

	public <T> T read(String source, Class<T> clazz) throws Exception
	{
		return reader.forType(clazz)
				.readValue(source);
	}

	public ObjectMapper mapper()
	{
		return mapper;
	}

	public ObjectReader reader()
	{
		return reader;
	}

	public ObjectWriter writer()
	{
		return writer;
	}

	public ObjectWriter writer(boolean pretty)
	{
		return (pretty) ? writer : writer.without(SerializationFeature.INDENT_OUTPUT);
	}


	// ===== Internal Helpers ==============================

	// NOTE: according to docs following objects can and should be cached!
	private final ObjectMapper mapper;
	private final ObjectReader reader;
	private final ObjectWriter writer;

	JsonDataUtils()
	{
		this.mapper = new ObjectMapper()
				.enable(JsonParser.Feature.ALLOW_COMMENTS)
				.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
				.registerModule(new JaxbAnnotationModule());

		this.reader = mapper.reader();

		this.writer = mapper.writer()
				.with(SerializationFeature.INDENT_OUTPUT);
	}
}
