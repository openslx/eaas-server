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

package de.bwl.bwfla.eaas.cluster.config.util;

import java.util.concurrent.TimeUnit;

import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;


/** A converter for strings of the form <i>"25 seconds"</i> to durations in milliseconds. */
public class DurationPropertyConverter implements PropertyConverter<Long>
{
	private final DurationParser parser;

	
	public DurationPropertyConverter()
	{
		this.parser = new DurationParser();
	}
	
	@Override
	public Long convert(String value, ConversionContext context)
	{
		return parser.parse(value, TimeUnit.MILLISECONDS);
	}
}
