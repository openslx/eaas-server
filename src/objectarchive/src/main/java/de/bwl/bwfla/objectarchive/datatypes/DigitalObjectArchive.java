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

package de.bwl.bwfla.objectarchive.datatypes;

import java.nio.file.Path;
import java.util.List;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.objectarchive.datatypes.ObjectFileCollection;

/**
 * 
 * @author klaus
 * This interface is the internal view on object archive impl. Currently it 
 * is in sync with ObjectArchiveFacadeWSRemote, however, the implementation 
 * is not bound to the WS interface. The WS facade is in charge of translating 
 * between DigitalObjectArchive and the Facade. 
 */
public interface DigitalObjectArchive
{
	public List<String> getObjectList(); 
	public FileCollection getObjectReference(String objectId);
	public ObjectFileCollection getObjectHandle(String objectId);
	public void importObject(ObjectFileCollection fc) throws BWFLAException;
	String getName();
	Path getLocalPath();
	DigitalObjectMetadata getMetadata(String objectId);

	boolean isDefaultArchive();

    int getNumObjectSeats(String id);

	void sync();

    TaskState sync(List<String> objectId);

    void delete(String id) throws BWFLAException;
}
