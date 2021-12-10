/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.migration;

import com.openslx.eaas.migration.config.MigrationConfig;


public class MigrationUtils
{
	public static final float DEFAULT_FAILURE_RATE = 0.25F;

	/** Look up migration's acceptable failure-rate */
	public static float getFailureRate(MigrationConfig config)
	{
		return MigrationUtils.getFailureRate(config, DEFAULT_FAILURE_RATE);
	}

	/** Look up migration's acceptable failure-rate */
	public static float getFailureRate(MigrationConfig config, float defvalue)
	{
		final var value = config.getArguments()
				.get("failure_rate");

		return (value != null) ? Float.parseFloat(value) : defvalue;
	}

	/** Check if current failure-rate is acceptable */
	public static boolean acceptable(int numTotal, int numFailures, float maxFailureRate)
	{
		final float threshold = maxFailureRate * ((float) numTotal);
		return (float) numFailures <= threshold;
	}
}
