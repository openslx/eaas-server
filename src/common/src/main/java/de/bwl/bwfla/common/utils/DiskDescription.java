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

package de.bwl.bwfla.common.utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.bwl.bwfla.common.exceptions.BWFLAException;


/**
 * This class represents a disk's description,
 * generated using parted command-line utility.
 *
 * @see #read(Path, Logger)
 */
public class DiskDescription
{
	private String path;
	private Transport transport;
	private PartitionTableType partitionTableType;
	private String modelName;
	private int logicalSectorSize;
	private int physicalSectorSize;
	private long size;

	private final List<Partition> partitions;

	/** Default unit for numeric values */
	private static final String DEFAULT_UNIT = "B";


	public DiskDescription()
	{
		this.partitions = new ArrayList<>();
	}

	public String getDevicePath()
	{
		return path;
	}

	public Transport getTransport()
	{
		return transport;
	}

	public PartitionTableType getPartitionTableType()
	{
		return partitionTableType;
	}

	public String getModelName()
	{
		return modelName;
	}

	public int getLogicalSectorSize()
	{
		return logicalSectorSize;
	}

	public int getPhysicalSectorSize()
	{
		return physicalSectorSize;
	}

	public long getSize()
	{
		return size;
	}

	public boolean hasPartitions()
	{
		return !partitions.isEmpty();
	}

	public List<Partition> getPartitions()
	{
		return partitions;
	}

	public DiskDescription setDevicePath(String path)
	{
		this.path = path;
		return this;
	}

	public DiskDescription setTransport(Transport transport)
	{
		this.transport = transport;
		return this;
	}

	public DiskDescription setTransport(String transport)
	{
		return this.setTransport(Transport.from(transport));
	}

	public DiskDescription setPartitionTableType(PartitionTableType type)
	{
		this.partitionTableType = type;
		return this;
	}

	public DiskDescription setPartitionTableType(String type)
	{
		return this.setPartitionTableType(PartitionTableType.from(type));
	}

	public DiskDescription setModelName(String name)
	{
		this.modelName = name;
		return this;
	}

	public DiskDescription setLogicalSectorSize(int size)
	{
		this.logicalSectorSize = size;
		return this;
	}

	public DiskDescription setPhysicalSectorSize(int size)
	{
		this.physicalSectorSize = size;
		return this;
	}

	public DiskDescription setDiskSize(long size)
	{
		this.size = size;
		return this;
	}

	public static DiskDescription read(Path image, Logger log) throws BWFLAException, IOException
	{
		final DeprecatedProcessRunner process = new DeprecatedProcessRunner("parted")
				.addArgument("--script")
				.addArgument("--machine")
				.addArgument(image.toString())
				.addArguments("unit", DEFAULT_UNIT, "print")
				.redirectStdErrToStdOut(false)
				.setLogger(log);

		final DeprecatedProcessRunner.Result result = process.executeWithResult()
				.orElseThrow(() -> new BWFLAException("Running parted failed!"));

		if (!result.successful())
			throw new BWFLAException("Reading disk-description failed!");

		// Format of parted's machine-readable output for the upper commands contains lines with ':'-separated fields.
		// It starts with a unit-header line, followed by a disk-description line, followed by partition-description lines.
		//
		// For example, the output on a 256GB disk with two partitions looks like this:
		//
		//    BYT;
		//    /dev/sda:256060514304B:scsi:512:512:gpt::;
		//    1:1048576B:537919487B:536870912B:fat32:esp:boot, esp;
		//    2:537919488B:215285235711B:214747316224B:xfs:root:;
		//
		// More specifically, a disk-description line contains the following pieces of information:
		//    ...
		//    <path>:<size>:<transport-type>:<logical-sector-size>:<physical-sector-size>:<partition-table-name>:<model-name>:<disk-flags>;
		//    ...
		//
		// Each line per formatted partition (with filesystem) contains the following:
		//    ....
		//    <partition-number>:<start-offset>:<end-offset>:<size>:<filesystem-type>:<partition-name>:<partition-flags>;
		//    ...
		//
		// Each line per unformatted partition (without filesystem) contains the following:
		//    ....
		//    <partition-number>:<start-offset>:<end-offset>:<size>:free;
		//    ...
		//
		// For more detailed information, see also parted's sources at:
		// https://git.savannah.gnu.org/gitweb/?p=parted.git;a=blob;f=parted/parted.c;hb=d6594b56a8d8e74185e11608f1b61471af7cc114#l1039


		return DiskDescription.parse(result.stdout(), DEFAULT_UNIT, log);
	}

	public static DiskDescription parse(String input, String unit, Logger log) throws BWFLAException
	{
		return Parser.parse(input, unit, log);
	}


	public enum Transport
	{
		SD_MMC("sd/mmc"),
		IDE("ide"),
		SCSI("scsi"),
		NVME("nvme"),
		VIRTBLK("virtblk"),
		LOOPBACK("loopback"),
		FILE("file"),
		UNKNOWN("unknown");

		private final String value;

		Transport(String value)
		{
			this.value = value;
		}

		public String value()
		{
			return value;
		}

		public static Transport from(String value) throws IllegalArgumentException
		{
			for (Transport transport : Transport.values()) {
				if (value.equalsIgnoreCase(transport.value()))
					return transport;
			}

			return UNKNOWN;
		}
	}

	public enum PartitionTableType
	{
		BSD("bsd"),
		LOOP("loop"),
		GPT("gpt"),
		MAC("mac"),
		MSDOS("msdos"),
		PC98("pc98"),
		SUN("sun"),
		UNKNOWN("unknown");

		private final String value;

		PartitionTableType(String value)
		{
			this.value = value;
		}

		public String value()
		{
			return value;
		}

		public static PartitionTableType from(String value) throws IllegalArgumentException
		{
			for (PartitionTableType type : PartitionTableType.values()) {
				if (value.equalsIgnoreCase(type.value()))
					return type;
			}

			return UNKNOWN;
		}
	}

	public static class Partition
	{
		private int index;
		private long start;
		private long end;
		private long size;
		private String name;
		private String flags;
		private String fstype;

		public Partition()
		{
			// Empty!
		}

		public int getIndex()
		{
			return index;
		}

		public long getStartOffset()
		{
			return start;
		}

		public long getEndOffset()
		{
			return end;
		}

		public long getSize()
		{
			return size;
		}

		public boolean hasFileSystemType()
		{
			return fstype != null;
		}

		public String getFileSystemType()
		{
			return fstype;
		}

		public String getPartitionName()
		{
			return name;
		}

		public String getFlags()
		{
			return flags;
		}

		public Partition setIndex(int index)
		{
			this.index = index;
			return this;
		}

		public Partition setStartOffset(long start)
		{
			this.start = start;
			return this;
		}

		public Partition setEndOffset(long end)
		{
			this.end = end;
			return this;
		}

		public Partition setSize(long size)
		{
			this.size = size;
			return this;
		}

		public Partition setName(String name)
		{
			this.name = name;
			return this;
		}

		public Partition setFlags(String flags)
		{
			this.flags = flags;
			return this;
		}

		public Partition setFileSystemType(String fstype)
		{
			this.fstype = fstype;
			return this;
		}
	}


	// ========== Internal Helpers ====================

	/** Parser for parted's output. */
	private static class Parser implements Consumer<String>, Closeable
	{
		private enum State
		{
			EXPECTING_HEADER,
			EXPECTING_DISK_DESCRIPTION,
			EXPECTING_PART_DESCRIPTION,
		}

		private enum DiskFields
		{
			PATH,
			SIZE,
			TRANSPORT,
			LOGICAL_SECTOR_SIZE,
			PHYSICAL_SECTOR_SIZE,
			PARTITION_TABLE_TYPE,
			MODEL,
			FLAGS,
			__LAST_FIELD__;

			public static int count()
			{
				return __LAST_FIELD__.ordinal();
			}
		}

		private enum PartitionFields
		{
			INDEX,
			START,
			END,
			SIZE,
			FILESYSTEM_TYPE,
			NAME,
			FLAGS,
			__LAST_FIELD__;

			public static int count()
			{
				return __LAST_FIELD__.ordinal();
			}
		}

		private final Logger log;
		private final String unit;
		private State state;
		private DiskDescription disk;

		public static final String HEADER = "BYT;";

		private Parser(String unit, Logger log)
		{
			this.state = State.EXPECTING_HEADER;
			this.log = log;
			this.unit = unit;
			this.disk = null;
		}

		public DiskDescription result()
		{
			if (disk == null)
				throw new IllegalStateException("Disk is not initialized properly!");

			return disk;
		}

		@Override
		public void accept(String line)
		{
			if (line.isEmpty())
				return;

			switch (state) {
				case EXPECTING_HEADER:
					if (!line.equals(HEADER))
						throw new IllegalStateException("Expected a header, but found: " + line);

					state = State.EXPECTING_DISK_DESCRIPTION;
					break;

				case EXPECTING_DISK_DESCRIPTION:
					disk = this.parseDiskDescription(line, unit);
					state = State.EXPECTING_PART_DESCRIPTION;
					break;

				case EXPECTING_PART_DESCRIPTION:
					disk.getPartitions()
							.add(this.parsePartitionDescription(line, unit));

					break;
			}
		}

		@Override
		public void close()
		{
			// Nothing to do!
		}

		public static DiskDescription parse(String input, String unit, Logger log) throws BWFLAException
		{
			try (Parser parser = new Parser(unit, log); Stream<String> lines = input.lines()) {
				lines.forEach(parser);
				return parser.result();
			}
			catch (Exception error) {
				throw new BWFLAException("Parsing disk-description failed!", error);
			}
		}

		private DiskDescription parseDiskDescription(String line, String unit) throws IllegalStateException
		{
			log.info("Parsing disk-description line...");
			final FieldReader<DiskFields> reader = new FieldReader<>(line, unit, DiskFields.count());
			return new DiskDescription()
					.setDevicePath(reader.string(DiskFields.PATH))
					.setDiskSize(reader.int64(DiskFields.SIZE))
					.setTransport(reader.string(DiskFields.TRANSPORT))
					.setLogicalSectorSize(reader.int32(DiskFields.LOGICAL_SECTOR_SIZE))
					.setPhysicalSectorSize(reader.int32(DiskFields.PHYSICAL_SECTOR_SIZE))
					.setPartitionTableType(reader.string(DiskFields.PARTITION_TABLE_TYPE))
					.setModelName(reader.string(DiskFields.MODEL));
		}

		private Partition parsePartitionDescription(String line, String unit) throws IllegalStateException
		{
			final int expNumFieldsUnformatted = 5;
			final int expNumFieldsFormatted = PartitionFields.count();
			final int partnum = 1 + disk.getPartitions().size();

			log.info("Parsing " + partnum + ". partition-description line...");
			final FieldReader<PartitionFields> reader = new FieldReader<>(line, unit);
			final int numfields = reader.fields().length;
			if (numfields != expNumFieldsFormatted && numfields != expNumFieldsUnformatted) {
				final String message = "Expected " + expNumFieldsUnformatted + " or " + expNumFieldsFormatted
						+ " field(s), but found " + numfields + "!";

				throw new IllegalStateException(message);
			}

			final Partition description = new Partition()
					.setIndex(reader.int32(PartitionFields.INDEX))
					.setStartOffset(reader.int64(PartitionFields.START))
					.setEndOffset(reader.int64(PartitionFields.END))
					.setSize(reader.int64(PartitionFields.SIZE));

			if (numfields == expNumFieldsFormatted) {
				description.setFileSystemType(reader.string(PartitionFields.FILESYSTEM_TYPE))
						.setName(reader.string(PartitionFields.NAME))
						.setFlags(reader.string(PartitionFields.FLAGS));
			}

			return description;
		}
	}

	private static class FieldReader<T extends Enum<T>>
	{
		private final String[] fields;
		private final String unit;

		private static final String DELIMITER = ":";
		private static final String TERMINATOR = ";";


		public FieldReader(String line, int expNumFields) throws IllegalStateException
		{
			this(line, null, expNumFields);
		}

		public FieldReader(String line, String unit, int expNumFields) throws IllegalStateException
		{
			this(line, unit);
			if (fields.length != expNumFields)
				throw new IllegalStateException("Expected " + expNumFields + " field(s), but found " + fields.length + "!");
		}

		public FieldReader(String line, String unit)
		{
			this.unit = unit;
			this.fields = FieldReader.split(line);
		}

		public String[] fields()
		{
			return fields;
		}

		public String string(Enum<T> id)
		{
			return this.get(id);
		}

		public int int32(Enum<T> id)
		{
			return Integer.parseInt(this.get(id, unit));
		}

		public long int64(Enum<T> id)
		{
			return Long.parseLong(this.get(id, unit));
		}

		private String get(Enum<T> id)
		{
			return fields[id.ordinal()];
		}

		/** Get field with suffix stripped */
		private String get(Enum<T> id, String suffix)
		{
			String field = this.get(id);
			if (suffix != null && field.endsWith(suffix))
				field = field.substring(0, field.length() - suffix.length());

			return field;
		}

		private static String[] split(String line)
		{
			final String[] fields = line.split(DELIMITER);
			if (line.endsWith(TERMINATOR)) {
				final int pos = fields.length - 1;
				final String field = fields[pos];
				fields[pos] = field.substring(0, field.length() - TERMINATOR.length());
			}

			// String.split() can produce empty strings,
			// replace them with nulls here...
			for (int i = 0; i < fields.length; ++i) {
				if (fields[i].isEmpty())
					fields[i] = null;
			}

			return fields;
		}
	}
}
