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

package de.bwl.bwfla.imageproposer.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ConfigHelpers;
import de.bwl.bwfla.configuration.BaseConfigurationPropertySourceProvider;
import de.bwl.bwfla.emucomp.api.Environment;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.spi.ConfigurationContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
public class ImageSorter
{
	private final Logger log = Logger.getLogger(this.getClass().getName());

	private EnvironmentsAdapter imageArchive;

	private final List<RankComparator> comparators = new ArrayList<RankComparator>();
	private final Map<FieldName, FieldRankIndex> ranks = new LinkedHashMap<FieldName, FieldRankIndex>();


	/** Sorts the specified images according to the configuration */
	public Collection<String> sort(Collection<String> imageIds) throws BWFLAException
	{
		// Fast path: don't sort!
		if (comparators.isEmpty())
			return imageIds;

		// Fast path: sort by ID!
		if ((comparators.size() == 1) && (comparators.get(0).getFieldName() == FieldName.ID)) {
			final RankComparator comparator = comparators.get(0);
			return imageIds.stream()
					.sorted((id1, id2) -> comparator.compare(ranks, id1, id2))
					.collect(Collectors.toList());
		}

		// Slow path: download all environment descriptions and sort!

		final ArrayList<Environment> environments = new ArrayList<Environment>(imageIds.size());
		for (String id : imageIds)
			environments.add(imageArchive.getEnvironmentById(id));

		environments.sort((e1, e2) -> this.compare(e1, e2));
		return environments.stream()
				.map((environment) -> environment.getId())
				.collect(Collectors.toList());
	}


	/* =============== Internal Helpers =============== */

	@PostConstruct
	protected void initialize()
	{
		final Configuration config = ConfigurationProvider.getConfiguration();

		this.imageArchive = new EnvironmentsAdapter(config.get("ws.imagearchive"));

		// Load configuration for image-sorting, if possible...
		final String sortingConfigFile = config.get("imageproposer.sorting_config_file");
		if (sortingConfigFile != null) {
			try {
				this.load(sortingConfigFile);
			}
			catch (Exception exception) {
				log.log(Level.SEVERE,"Loading image-sorting config failed! Cause", exception);
				throw new RuntimeException(exception);
			}
		}
		else {
			log.info("Disabling image-sorting! No configuration found.");
		}
	}

	private void load(String sortingConfigFile) throws MalformedURLException
	{
		log.info("Loading image-sorting config from '" + sortingConfigFile + "'...");

		final ConfigurationContext context = ConfigurationProvider.getConfigurationContextBuilder()
				.addPropertySources(new SorterConfigPropertySourceProvider(sortingConfigFile).getPropertySources())
				.addDefaultPropertyConverters()
				.build();

		final Configuration config = ConfigurationProvider.createConfiguration(context);

		// Parse comparators
		{
			comparators.clear();

			while (true) {
				// Parse next entry...
				final String prefix = ConfigHelpers.toListKey("sort_by", comparators.size(), ".");
				final Configuration subconfig = ConfigHelpers.filter(config, prefix);
				if (ConfigHelpers.isEmpty(subconfig))
					break;  // No more entries found!

				final FieldName field = FieldName.fromString(subconfig.get("field"));
				final SortingOrder order = SortingOrder.valueOf(subconfig.get("order").toUpperCase());
				comparators.add(new RankComparator(field, order));
			}
		}

		// Parse rank mappings
		{
			ranks.clear();

			while (true) {
				final String key = ConfigHelpers.toListKey("ranks", ranks.size(), ".");
				final Configuration subconfig = ConfigHelpers.filter(config, key);
				if (ConfigHelpers.isEmpty(subconfig))
					break;

				FieldRankIndex entry = FieldRankIndex.create(subconfig);
				ranks.put(entry.getFieldName(), entry);
			}
		}

		if (comparators.size() != ranks.size())
			throw new ConfigException("Invalid configuration! The numbers of fields and rank mappings differ.");

		log.info("Proposed images will be sorted by " + comparators.size() + " field(s)");
	}

	private int compare(RankComparator comparator, Environment e1, Environment e2)
	{
		final FieldName field = comparator.getFieldName();
		int ret;
		switch (field) {
			case ID:
				return comparator.compare(ranks, e1.getId(), e2.getId());

			case OS:
				if(!(e1 instanceof MachineConfiguration) || !(e2 instanceof MachineConfiguration))
					return 0;

				ret = comparator.compare(ranks,
						((MachineConfiguration)e1).getOperatingSystemId(),
						((MachineConfiguration)e2).getOperatingSystemId());

				return ret;
			case TAG:
				ret = comparator.compare(ranks, e1.getUserTag(), e2.getUserTag());
				return ret;
			default:
				throw new IllegalArgumentException("Unsupported field: " + field.name());
		}
	}

	private int compare(Environment e1, Environment e2)
	{
		for (RankComparator comparator : comparators) {
			final int result = this.compare(comparator, e1, e2);
			if (result != 0)
				return result;  // Ranks differ!

			// else continue with next field
		}

		return 0;
	}

	private enum SortingOrder
	{
		ASCENDING,
		DESCENDING
	}

	private enum FieldName
	{
		ID("id"),
		OS("os"),
		TAG("tag");

		private final String value;

		FieldName(String name)
		{
			this.value = name;
		}

		public static FieldName fromString(String value)
		{
			for (FieldName field : FieldName.values()) {
				if (value.contentEquals(field.value))
					return field;
			}

			throw new IllegalArgumentException("Invald FieldName constant: " + value);
		}
	}

	private static class RankComparator implements Comparator<Integer>
	{
		private final SortingOrder order;
		private final FieldName field;

		public RankComparator(FieldName field, SortingOrder order)
		{
			this.order = order;
			this.field = field;
		}

		public FieldName getFieldName()
		{
			return field;
		}

		public SortingOrder getSortingOrder()
		{
			return order;
		}

		public int compare(Integer r1, Integer r2)
		{
			return (this.getSortingOrder() == SortingOrder.ASCENDING) ?
					Integer.compare(r1, r2) : Integer.compare(r2, r1);
		}

		public int compare(Map<FieldName, FieldRankIndex> ranks, String v1, String v2)
		{
			final FieldRankIndex index = ranks.get(field);
			final int r1 = index.lookup(v1);
			final int r2 = index.lookup(v2);
			return this.compare(r1, r2);
		}
	}

	private static class FieldRankIndex
	{
		private final FieldName field;
		private final int defaultRank;
		private final Map<String, Integer> entries;

		public FieldRankIndex(FieldName field, int defaultRank)
		{
			this.field = field;
			this.defaultRank = defaultRank;
			this.entries = new HashMap<String, Integer>();
		}

		public FieldName getFieldName()
		{
			return field;
		}

		public int getDefaultRank()
		{
			return defaultRank;
		}

		/** Adds a mapping from field's value to a rank */
		public void put(String value, int rank)
		{
			entries.put(value, rank);
		}

		/** Looks up a rank for the field's value */
		public int lookup(String value)
		{
			return entries.getOrDefault(value, this.getDefaultRank());
		}

		public static FieldRankIndex create(Configuration config)
		{
			final FieldName field = FieldName.fromString(config.get("field"));
			final int defrank = Integer.parseInt(config.get("default"));
			final FieldRankIndex index = new FieldRankIndex(field, defrank);
			for (int count = 0; ; ++count) {
				final String key = ConfigHelpers.toListKey("mapping", count, ".");
				final Configuration subconfig = ConfigHelpers.filter(config, key);
				if (ConfigHelpers.isEmpty(subconfig))
					break;

				final String rank = subconfig.get("rank");
				index.put(subconfig.get("value"), Integer.parseInt(rank));
			}

			return index;
		}
	}

	private static class SorterConfigPropertySourceProvider extends BaseConfigurationPropertySourceProvider
	{
		public SorterConfigPropertySourceProvider(String path) throws MalformedURLException
		{
			this(Paths.get(path));
		}

		public SorterConfigPropertySourceProvider(Path path) throws MalformedURLException
		{
			super(path.toUri().toURL());
		}

		@Override
		public int getDefaultOrdinal()
		{
			return 500;
		}
	}
}
