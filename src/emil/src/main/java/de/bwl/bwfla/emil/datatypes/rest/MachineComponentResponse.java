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

package de.bwl.bwfla.emil.datatypes.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class MachineComponentResponse extends ComponentResponse {
    @XmlElement(required = false)
    private Integer driveId;

    @XmlElement
    private List<RemovableMedia> removableMediaList;

    @Deprecated
    public MachineComponentResponse(String id, Integer driveId) {
        super(id);
        this.driveId = driveId;
    }

    public MachineComponentResponse(String id, List<RemovableMedia> removableMediaList) {
        super(id);
        this.removableMediaList = removableMediaList;
    }

    public Integer getDriveId() {
        return driveId;
    }

    public void setDriveId(Integer driveId) {
        this.driveId = driveId;
    }

    public List<RemovableMedia> getRemovbleMediaList() {
        return removableMediaList;
    }

    public void setRemovableMediaList(List<RemovableMedia> removbleMediaList) {
        this.removableMediaList = removbleMediaList;
    }

    @XmlRootElement
    public static class RemovableMedia {
        @XmlElement
        private String id;
        @XmlElement
        private String archive;
        @XmlElement
        private String driveIndex;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getArchive() {
            return archive;
        }

        public void setArchive(String archive) {
            this.archive = archive;
        }

        public String getDriveIndex() {
            return driveIndex;
        }

        public void setDriveIndex(String driveIndex) {
            this.driveIndex = driveIndex;
        }
    }
}
