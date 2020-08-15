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

package de.bwl.bwfla.emucomp.api;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DiskDescription;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ImageGeneralizationUtils {
    public enum FS_TYPE {
        ntfs, vfat, hfs
    }

    public static boolean checkPartition(DiskDescription.Partition p, String fsType) {
        return p.getIndex() == 1 && p.getFileSystemType().equals(fsType);
    }

    public static boolean checkPartition(DiskDescription.Partition p, String label, String fsType) {
        return p.getPartitionName().equals(label) && p.getFileSystemType().equals(fsType);
    }

    public static void cleanUpFuse(File tempDDdir, File tempMountDir, String loopDev, Logger log) throws BWFLAException, IOException {
        if (tempDDdir != null) {
            log.info("unmounting and deleting DDdir");
            if (tempDDdir.exists())
                EmulatorUtils.unmountFuse(tempDDdir.toPath());
        }
        if (tempMountDir != null) {
            log.info("unmounting and deleting MountDir");
            if (tempMountDir.exists())
                EmulatorUtils.unmountFuse(tempMountDir.toPath());
        }
        if (loopDev != null)
            detachLoop(loopDev);
    }

    public static void cleanUpMount(File tempMountDir, String loopDev, Logger log) throws BWFLAException, IOException {
        if (tempMountDir != null) {
            log.info("unmounting and deleting MountDir");
            if (tempMountDir.exists())
                EmulatorUtils.unmountFuse(tempMountDir.toPath());
        }
        if(loopDev != null)
        detachLoop(loopDev);
    }

    static void detachLoop(String loopDev) throws BWFLAException, IOException {
        if (loopDev != null)
            EmulatorUtils.detachLoop(loopDev);
    }

}
