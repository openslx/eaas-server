package de.bwl.bwfla.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import com.opencsv.CSVReader;

public class DiskPartitionDescription {

	private File image;
	private DiskDescription diskDescription;
	private List<DiskPartition> partitions = null;
	
	public DiskPartitionDescription(File image) throws BWFLAException, IOException
	{
		this.image = image;
		read();
	}
	
	public boolean hasPartitionTable()
	{
		if(partitions != null)
			return true;
		
		return false;
	}
	
	public List<DiskPartition> getPartitionTable()
	{
		return partitions;
	}
	
	private void read() throws BWFLAException, IOException {
		DeprecatedProcessRunner process = new DeprecatedProcessRunner("parted");
		process.addArgument("-sm");
		process.addArgument(image.getAbsolutePath());
		process.addArguments("unit", "B", "print");
		process.redirectStdErrToStdOut(false);

		CSVReader reader = null;
		try {
			if (!process.execute(false, false))
				throw new BWFLAException(process.getStdErrString());

			reader = new CSVReader(process.getStdOutReader(), ':');
			String[] header = reader.readNext();
			String[] diskinfo = reader.readNext();

			if (header == null || diskinfo == null) {
				reader.close();
				throw new BWFLAException("header & diskinfo line missing");
			}

			diskDescription = new DiskDescription(header[0], diskinfo);

			String[] nextLine;
			partitions = new ArrayList<DiskPartition>();
			while ((nextLine = reader.readNext()) != null) {
				partitions.add(new DiskPartition(nextLine));
			}
		} finally {
			if (reader != null)
				reader.close();
			if (process != null)
				process.cleanup();
		}
	}
	
	public static class DiskDescription 
	{
		// "path":"size":"transport-type":"logical-sector-size":"physical-sector-size":"partition-table-type":"model-name";
		String path, transport, partitionType, modelName;
		long size, logicalSectorSize, physicalSectorSize;
		
		private enum PartLine{
			PATH,
			SIZE,
			TRANSPORT,
			LSIZE,
			PSIZE,
			TYPE,
			MODEL,
			PARTLINESIZE
		};
		
		public DiskDescription(String type, String[] partedLine) throws BWFLAException
		{
			if(!type.equals("BYT;"))
				throw new BWFLAException("unsupported disk type: " + type);
			
			if(partedLine.length < PartLine.PARTLINESIZE.ordinal())
				throw new BWFLAException("invalid parted line size: " + partedLine.length);
		
			path = partedLine[PartLine.PATH.ordinal()];
			size = Long.parseLong(partedLine[PartLine.SIZE.ordinal()].replace("B", ""));
			transport = partedLine[PartLine.TRANSPORT.ordinal()];
			logicalSectorSize = Long.parseLong(partedLine[PartLine.LSIZE.ordinal()].replace("B", ""));
			physicalSectorSize = Long.parseLong(partedLine[PartLine.PSIZE.ordinal()].replace("B", ""));
			partitionType = partedLine[PartLine.TYPE.ordinal()];
			modelName = partedLine[PartLine.MODEL.ordinal()];
		}

		public String getPath() {
			return path;
		}

		public String getTransport() {
			return transport;
		}

		public String getPartitionType() {
			return partitionType;
		}

		public String getModelName() {
			return modelName;
		}

		public long getSize() {
			return size;
		}

		public long getLogicalSectorSize() {
			return logicalSectorSize;
		}

		public long getPhysicalSectorSize() {
			return physicalSectorSize;
		}
	}
	
	public static class DiskPartition
	{
		private long index, begin, end, size;
		private String fsType, partitionName, flags;
		
		private enum PartLine{
			INDEX,
			BEGIN,
			END,
			SIZE,
			FSTYPE,
			PARTNAME,
			FLAGS,
			PARTLINESIZE
		};
		
		// "number":"begin":"end":"size":"filesystem-type":"partition-name":"flags-set";
		public DiskPartition(String[] partedLine) throws BWFLAException
		{
			if(partedLine.length < PartLine.PARTLINESIZE.ordinal())
				throw new BWFLAException("invalid parted line size: " + partedLine.length);
			
			index = Long.parseLong(partedLine[PartLine.INDEX.ordinal()]);
			begin = Long.parseLong(partedLine[PartLine.BEGIN.ordinal()].replace("B", ""));
			end = Long.parseLong(partedLine[PartLine.END.ordinal()].replace("B", ""));
			size = Long.parseLong(partedLine[PartLine.SIZE.ordinal()].replace("B", ""));
			fsType = partedLine[PartLine.FSTYPE.ordinal()];
			partitionName = partedLine[PartLine.PARTNAME.ordinal()];
			flags = partedLine[PartLine.FLAGS.ordinal()];
		}
		
		public long getIndex() {return index;}

		public long getBegin() {
			return begin;
		}

		public long getEnd() {
			return end;
		}

		public long getSize() {
			return size;
		}

		public String getFsType() {
			return fsType;
		}

		public String getPartitionName() {
			return partitionName;
		}

		public String getFlags() {
			return flags;
		}
	}
}
