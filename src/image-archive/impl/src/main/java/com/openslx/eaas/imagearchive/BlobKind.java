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

package com.openslx.eaas.imagearchive;


public enum BlobKind
{
	ALIASING,
	ENVIRONMENT,
	SESSION,
	NETWORK,
	MACHINE,
	CONTAINER,
	EMULATOR_METADATA,
	EMULATOR,
	TEMPLATE,
	CHECKPOINT,
	IMAGE_METADATA,
	IMAGE,
	ROM;

	public String value()
	{
		return this.name()
				.replace("_", "-")
				.toLowerCase();
	}

	public static BlobKind from(String kind)
	{
		switch (kind) {
			case "aliasing":
				return BlobKind.ALIASING;
			case "environment":
				return BlobKind.ENVIRONMENT;
			case "session":
				return BlobKind.SESSION;
			case "network":
				return BlobKind.NETWORK;
			case "machine":
				return BlobKind.MACHINE;
			case "container":
				return BlobKind.CONTAINER;
			case "emulator-metadata":
				return BlobKind.EMULATOR_METADATA;
			case "emulator":
				return BlobKind.EMULATOR;
			case "template":
				return BlobKind.TEMPLATE;
			case "checkpoint":
				return BlobKind.CHECKPOINT;
			case "image-metadata":
				return BlobKind.IMAGE_METADATA;
			case "image":
				return BlobKind.IMAGE;
			case "rom":
				return BlobKind.ROM;
			default:
				throw new IllegalArgumentException("Unknown kind: " + kind);
		}
	}

	/** Return number of blob-kinds */
	public static int count()
	{
		return COUNT;
	}

	private static final int COUNT = BlobKind.values().length;
}
