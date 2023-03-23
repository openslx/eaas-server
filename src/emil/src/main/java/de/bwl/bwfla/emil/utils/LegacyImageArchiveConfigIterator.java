package de.bwl.bwfla.emil.utils;

import de.bwl.bwfla.common.utils.ConfigHelpers;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import java.util.Iterator;


public class LegacyImageArchiveConfigIterator implements Iterator<Configuration>
{
	private Configuration next;
	private int index;


	public LegacyImageArchiveConfigIterator()
	{
		this.next = LegacyImageArchiveConfigIterator.get(0);
		this.index = 0;
	}

	@Override
	public boolean hasNext()
	{
		return next.get("name") != null;
	}

	@Override
	public Configuration next()
	{
		final var current = next;
		this.next = LegacyImageArchiveConfigIterator.get(++index);
		return current;
	}

	private static Configuration get(int index)
	{
		final var prefix = ConfigHelpers.toListKey("imagearchive.backends", index, ".");
		return ConfigHelpers.filter(ConfigurationProvider.getConfiguration(), prefix);
	}
}
