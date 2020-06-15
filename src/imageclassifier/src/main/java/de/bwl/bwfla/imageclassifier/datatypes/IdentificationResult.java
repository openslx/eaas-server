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

package de.bwl.bwfla.imageclassifier.datatypes;

import java.util.HashMap;
import java.util.Map;

import de.bwl.bwfla.emucomp.api.FileCollection;

public class IdentificationResult<T>
{
	private final FileCollection fileCollection;

	private final HashMap<String, IdentificationData<?>> identificationData;

	private final Map<String, String> policy;


	public IdentificationResult(FileCollection fc, Map<String, String> policy)
	{
		this.fileCollection = fc;
		this.policy = policy;
		this.identificationData = new HashMap<String, IdentificationData<?>>();
	}

	public void addResult(String id, IdentificationData<?> data)
	{
		identificationData.put(id, data);
	}

	public HashMap<String, IdentificationData<?>> getIdentificationData() {
		return identificationData;
	}

	public Map<String, String> getPolicy()
	{
		return policy;
	}

	public FileCollection getFileCollection() {
		return fileCollection;
	}
}
