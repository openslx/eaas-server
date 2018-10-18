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

package de.bwl.bwfla.imagearchive;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;

public class DataUtil {

	static Logger log = Logger.getLogger("DataUtil");

	static protected void writeData(InputStream inputStream, File destImgFile) throws BWFLAException {


		try {
			FileUtils.copyInputStreamToFile(inputStream, destImgFile);
		}
		catch(IOException e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
			throw new BWFLAException("FileUtils: " + e.getClass());
		}
		finally
		{
			if(inputStream != null) try {
				inputStream.close();
			} catch (IOException e) {
				throw new BWFLAException(e);
			}
		}
	}
	
	protected static boolean writeString(String conf, File destConfFile)
	{
		BufferedWriter confOut = null;
		try
		{
			confOut = new BufferedWriter(new FileWriter(destConfFile));
			confOut.write(conf);
			confOut.flush();
			return true;
		}
		catch(Throwable e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		finally
		{
			try
			{
				if(confOut != null)
					confOut.close();
			}
			catch(Throwable e)
			{
				log.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return false;
	}
	
}
