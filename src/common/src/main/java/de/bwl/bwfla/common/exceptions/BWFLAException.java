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

package de.bwl.bwfla.common.exceptions;

import de.bwl.bwfla.common.utils.EaasBuildInfo;

import javax.ejb.ApplicationException;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.TreeMap;


@XmlRootElement
@ApplicationException
public class BWFLAException extends Exception
{
	private static final long	serialVersionUID	= 1988575155200056202L;

	private final TreeMap<String, String> properties;
	private String msgsuffix;


	public BWFLAException() {
		this(null, null);
	}

	public BWFLAException(String message) {
		this(message, null);
	}

	public BWFLAException(Throwable cause) {
		this(null, cause);
	}

	public BWFLAException(String message, Throwable cause) {
		super(message, cause);

		this.msgsuffix = null;
		this.properties = new TreeMap<>();

		if (message != null && !message.contains("BUILD"))
			this.setMetaData("BUILD", EaasBuildInfo.getVersion());
	}

	public BWFLAException setId(String id)
	{
		return this.setMetaData("ID", id.toUpperCase());
	}

	public BWFLAException setMetaData(String key, String value)
	{
		properties.put(key, value);
		msgsuffix = null;
		return this;
	}

	@Override
	public String getMessage()
	{
		final String message = super.getMessage();
		final String suffix = this.getMessageSuffix();
		final StringBuilder sb = new StringBuilder(512);
		if (message != null)
			sb.append(message);

		if (suffix != null) {
			if (message != null)
				sb.append(" ");

			sb.append(suffix);
		}

		return (sb.length() > 0) ? sb.toString() : null;
	}

	private String getMessageSuffix()
	{
		if (msgsuffix == null && !properties.isEmpty()) {
			final StringBuilder sb = new StringBuilder(512);
			sb.append("(");
			properties.forEach((key, value) -> {
				sb.append(key);
				sb.append(": ");
				sb.append(value);
				sb.append(", ");
			});

			sb.setLength(sb.length() - 2);
			sb.append(")");

			// cache the suffix
			msgsuffix = sb.toString();
		}

		return msgsuffix;
	}
}