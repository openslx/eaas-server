package de.bwl.bwfla.common.utils;

import java.io.CharArrayWriter;
import java.io.IOException;

import com.google.gson.stream.JsonWriter;


public class JsonBuilder
{
	/** Default JSON buffer capacity. */
	private static final int DEFAULT_CAPACITY = 128;
	
	/** When true, then JSON will be human-readable, else compact. */
	private static final boolean PRETTY_PRINT_ENABLED = false;
	
	private final CharArrayWriter buffer;
	private final JsonWriter writer;
	
	
	public JsonBuilder()
	{
		this(DEFAULT_CAPACITY, PRETTY_PRINT_ENABLED);
	}
	
	public JsonBuilder(int capacity)
	{
		this(capacity, PRETTY_PRINT_ENABLED);
	}

	public JsonBuilder(boolean prettyPrint)
	{
		this(DEFAULT_CAPACITY, prettyPrint);
	}
	
	public JsonBuilder(int capacity, boolean prettyPrint)
	{
		this.buffer = new CharArrayWriter(capacity);
		this.writer = new JsonWriter(buffer);
		if (prettyPrint)
			writer.setIndent("    ");
	}
	
	public void beginObject() throws IOException
	{
		writer.beginObject();
	}
	
	public void endObject() throws IOException
	{
		writer.endObject();
	}
	
	public void beginArray() throws IOException
	{
		writer.beginArray();
	}
	
	public void endArray() throws IOException
	{
		writer.endArray();
	}

	public JsonBuilder name(String n) throws IOException
	{
		writer.name(n);
		return this;
	}
	
	public JsonBuilder value(String v) throws IOException
	{
		writer.value(v);
		return this;
	}
	
	public void add(String name, boolean value) throws IOException
	{
		writer.name(name);
		writer.value(value);
	}
	
	public void add(String name, String value) throws IOException
	{
		writer.name(name);
		writer.value(value);
	}
	
	public void finish() throws IOException
	{
		writer.close();
	}
	
	public String toString()
	{
		return buffer.toString();
	}
}
