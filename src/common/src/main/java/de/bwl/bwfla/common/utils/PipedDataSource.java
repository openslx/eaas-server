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

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


public class PipedDataSource implements DataSource
{
	private final String name;
	private final String mimetype;
	private final PipedInputStream istream;
	private final PipedOutputStream ostream;


	public PipedDataSource(String name, String mimetype) throws IOException
	{
		this.name = name;
		this.mimetype = mimetype;
		this.istream = new PipedInputStream();
		this.ostream = new PipedOutputStream(istream);
	}

	@Override
	public InputStream getInputStream()
	{
		return istream;
	}

	@Override
	public OutputStream getOutputStream()
	{
		return ostream;
	}

	@Override
	public String getContentType()
	{
		return mimetype;
	}

	@Override
	public String getName()
	{
		return name;
	}
}
