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

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DiskDescriptionTest
{
	private static final Logger LOG = Logger.getLogger(DiskDescriptionTest.class.getName());

	// Input generated with 'parted -sm /dev/sda unit B print'.


	@Test
	public void testParseUnpartitionedDisk()
	{
		try {
			final String input = "BYT;\n"
					+ "/dev/sda:256060514304B:scsi:512:512:gpt:disk 123:;";

			final DiskDescription disk = DiskDescription.parse(input, "B", LOG);
			final List<DiskDescription.Partition> parts = disk.getPartitions();

			DiskDescriptionTest.check(disk, "/dev/sda", 256060514304L, "scsi", 512, 512, "gpt", "disk 123");
			Assert.assertEquals(0, parts.size());
		}
		catch (Exception error) {
			final String message = "Parsing valid disk-description failed!";
			LOG.log(Level.WARNING, message, error);
			Assert.fail(message);
		}
	}

	@Test
	public void testParseDiskWithFormattedPartitionsOnly()
	{
		final String input = "BYT;\n"
				+ "/dev/sda:256060514304B:scsi:512:512:gpt:disk 123:;\n"
				+ "1:1048576B:537919487B:536870912B:fat32:esp:boot, esp;\n"
				+ "2:537919488B:215285235711B:214747316224B:xfs:root:;";

		try {
			final DiskDescription disk = DiskDescription.parse(input, "B", LOG);
			final List<DiskDescription.Partition> parts = disk.getPartitions();

			DiskDescriptionTest.check(disk, "/dev/sda", 256060514304L, "scsi", 512, 512, "gpt", "disk 123");
			Assert.assertEquals(2, parts.size());

			DiskDescriptionTest.check(parts, 1, 1048576L, 537919487L, 536870912L, "fat32", "esp", "boot, esp");
			DiskDescriptionTest.check(parts, 2, 537919488L, 215285235711L, 214747316224L, "xfs", "root", null);
		}
		catch (Exception error) {
			final String message = "Parsing valid disk-description failed!";
			LOG.log(Level.WARNING, message, error);
			Assert.fail(message);
		}
	}

	@Test
	public void testParseDiskWithUnformattedPartitionsOnly()
	{
		final String input = "BYT;\n"
				+ "/dev/sda:256060514304B:ide:4096:512:msdos:disk 123:;\n"
				+ "1:1048576B:537919487B:536870912B:free;\n"
				+ "2:537919488B:215285235711B:214747316224B:free;\n"
				+ "3:215285235712B:215285235722B:10B:free;";

		try {
			final DiskDescription disk = DiskDescription.parse(input, "B", LOG);
			final List<DiskDescription.Partition> parts = disk.getPartitions();

			DiskDescriptionTest.check(disk, "/dev/sda", 256060514304L, "ide", 4096, 512, "msdos", "disk 123");
			Assert.assertEquals(3, parts.size());

			DiskDescriptionTest.check(parts, 1, 1048576L, 537919487L, 536870912L, null, null, null);
			DiskDescriptionTest.check(parts, 2, 537919488L, 215285235711L, 214747316224L, null, null, null);
			DiskDescriptionTest.check(parts, 3, 215285235712L, 215285235722L, 10L, null, null, null);
		}
		catch (Exception error) {
			final String message = "Parsing valid disk-description failed!";
			LOG.log(Level.WARNING, message, error);
			Assert.fail(message);
		}
	}

	@Test
	public void testParseDiskWithMixedPartitions()
	{
		final String input = "BYT;\n"
				+ "/dev/sda:256060514304B:file:512:512:gpt:Example Disk:;\n"
				+ "1:1048576B:537919487B:536870912B:fat32:esp:boot, esp;\n"
				+ "2:537919488B:215285235711B:214747316224B:xfs:root:;\n"
				+ "3:215285235712B:215285235722B:10B:free;";

		try {
			final DiskDescription disk = DiskDescription.parse(input, "B", LOG);
			final List<DiskDescription.Partition> parts = disk.getPartitions();

			DiskDescriptionTest.check(disk, "/dev/sda", 256060514304L, "file", 512, 512, "gpt", "Example Disk");
			Assert.assertEquals(3, parts.size());

			DiskDescriptionTest.check(parts, 1, 1048576L, 537919487L, 536870912L, "fat32", "esp", "boot, esp");
			DiskDescriptionTest.check(parts, 2, 537919488L, 215285235711L, 214747316224L, "xfs", "root", null);
			DiskDescriptionTest.check(parts, 3, 215285235712L, 215285235722L, 10L, null, null, null);
		}
		catch (Exception error) {
			final String message = "Parsing valid disk-description failed!";
			LOG.log(Level.WARNING, message, error);
			Assert.fail(message);
		}
	}

	@Test
	public void testParseWrongHeader()
	{
		final String input = "CYL;\n";

		try {
			final DiskDescription result = DiskDescription.parse(input, "B", LOG);
			Assert.fail("Invalid header has been parsed without errors!");
		}
		catch (Exception error) {
			// Expected outcome!
		}
	}

	@Test
	public void testParseWrongSizes()
	{
		final String input = "BYT;\n"
				+ "/dev/sda:XYZ:scsi:XYZ:XYZ:gpt:Example Disk:;";

		try {
			final DiskDescription result = DiskDescription.parse(input, "B", LOG);
			Assert.fail("Invalid sizes have been parsed without errors!");
		}
		catch (Exception error) {
			// Expected outcome!
		}
	}

	@Test
	public void testParseWrongNumOfFields()
	{
		final String input = "BYT;\n"
				+ "/dev/sda:XYZ;";

		try {
			final DiskDescription result = DiskDescription.parse(input, "B", LOG);
			Assert.fail("Invalid input has been parsed without errors!");
		}
		catch (Exception error) {
			// Expected outcome!
		}
	}


	private static void check(DiskDescription disk, String path, long size, String transport,
							  int lbsize, int pbsize, String type, String model)
	{
		Assert.assertEquals(path, disk.getDevicePath());
		Assert.assertEquals(size, disk.getSize());
		Assert.assertEquals(transport, disk.getTransport().value());
		Assert.assertEquals(lbsize, disk.getLogicalSectorSize());
		Assert.assertEquals(pbsize, disk.getPhysicalSectorSize());
		Assert.assertEquals(type, disk.getPartitionTableType().value());
		Assert.assertEquals(model, disk.getModelName());
	}

	private static void check(List<DiskDescription.Partition> partitions, int index,
							  long start, long end, long size, String fstype, String name, String flags)
	{
		final DiskDescription.Partition part = partitions.get(index - 1);
		Assert.assertEquals(index, part.getIndex());
		Assert.assertEquals(start, part.getStartOffset());
		Assert.assertEquals(end, part.getEndOffset());
		Assert.assertEquals(size, part.getSize());
		Assert.assertEquals(fstype, part.getFileSystemType());
		Assert.assertEquals(name, part.getPartitionName());
		Assert.assertEquals(flags, part.getFlags());
	}
}
