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

package com.openslx.eaas.imagearchive.databind;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;


public class EmulatorMetaData extends AbstractMetaData
{
	private String id;
	private String name;
	private String version;
	private String digest;
	private Set<String> tags;
	private Provenance provenance;
	private ImageMetaData image;

	/** Version tag to be used by default */
	public static final String DEFAULT_VERSION = "default";


	public EmulatorMetaData()
	{
		super(Kinds.V1);
	}

	@JsonSetter(Fields.ID)
	public EmulatorMetaData setId(String id)
	{
		this.id = id;
		return this;
	}

	@JsonGetter(Fields.ID)
	public String id()
	{
		if (id == null) {
			if (version == null || version.isEmpty())
				throw new IllegalStateException("Invalid emulator version!");

			id = EmulatorMetaData.identifier(name, version);
		}

		return id;
	}

	@JsonSetter(Fields.NAME)
	public EmulatorMetaData setName(String name)
	{
		this.name = name;
		this.id = null;
		return this;
	}

	@JsonGetter(Fields.NAME)
	public String name()
	{
		return name;
	}

	@JsonSetter(Fields.VERSION)
	public EmulatorMetaData setVersion(String version)
	{
		this.version = version;
		this.id = null;
		return this;
	}

	@JsonGetter(Fields.VERSION)
	public String version()
	{
		return version;
	}

	@JsonSetter(Fields.DIGEST)
	public EmulatorMetaData setDigest(String digest)
	{
		this.digest = digest;
		return this;
	}

	@JsonGetter(Fields.DIGEST)
	public String digest()
	{
		return digest;
	}

	@JsonSetter(Fields.TAGS)
	public EmulatorMetaData setTags(Set<String> tags)
	{
		this.tags = tags;
		return this;
	}

	@JsonGetter(Fields.TAGS)
	public Set<String> tags()
	{
		if (tags == null)
			tags = new HashSet<>();

		return tags;
	}

	@JsonSetter(Fields.PROVENANCE)
	public EmulatorMetaData setProvenance(Provenance provenance)
	{
		this.provenance = provenance;
		return this;
	}

	@JsonGetter(Fields.PROVENANCE)
	public Provenance provenance()
	{
		if (provenance == null)
			provenance = new Provenance();

		return provenance;
	}

	@JsonSetter(Fields.IMAGE)
	public EmulatorMetaData setImage(ImageMetaData image)
	{
		this.image = image;
		return this;
	}

	@JsonGetter(Fields.IMAGE)
	public ImageMetaData image()
	{
		if (image == null)
			image = new ImageMetaData();

		return image;
	}

	@JsonIgnore
	public Stream<String> aliases()
	{
		return this.tags()
				.stream()
				.map((tag) -> EmulatorMetaData.identifier(name, tag));
	}


	/** Compute emulator ID from it's name and version */
	public static String identifier(String name, String version)
	{
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("Invalid emulator name!");

		if (version == null || version.isEmpty())
			version = DEFAULT_VERSION;

		try {
			final var key = (name + "/" + version).getBytes(StandardCharsets.UTF_8);
			final var buffer = new byte[EMULATOR_ID_NAMESPACE.length + key.length];
			System.arraycopy(EMULATOR_ID_NAMESPACE, 0, buffer, 0, EMULATOR_ID_NAMESPACE.length);
			System.arraycopy(key, 0, buffer, EMULATOR_ID_NAMESPACE.length, key.length);
			return UUID.nameUUIDFromBytes(buffer)
					.toString();
		}
		catch (Exception error) {
			throw new IllegalStateException("Computing emulator ID failed!", error);
		}
	}


	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class Provenance
	{
		private String url;
		private String tag;
		private List<String> layers;

		@JsonSetter(Fields.URL)
		public Provenance setUrl(String url)
		{
			this.url = url;
			return this;
		}

		@JsonGetter(Fields.URL)
		public String url()
		{
			return url;
		}

		@JsonSetter(Fields.TAG)
		public Provenance setTag(String tag)
		{
			this.tag = tag;
			return this;
		}

		@JsonGetter(Fields.TAG)
		public String tag()
		{
			return tag;
		}

		@JsonSetter(Fields.LAYERS)
		public Provenance setLayers(List<String> layers)
		{
			this.layers = layers;
			return this;
		}

		@JsonGetter(Fields.LAYERS)
		public List<String> layers()
		{
			if (layers == null)
				layers = new ArrayList<>();

			return layers;
		}
	}


	public static final class Kinds
	{
		public static final String TYPE = "emulator";
		public static final String V1   = AbstractMetaData.kind(TYPE, "v1");
	}


	// ===== Internal Helpers ====================

	/** UUID-v3 namespace for emulator IDs */
	private static final byte[] EMULATOR_ID_NAMESPACE = new byte[16];
	static {
		final var uuid = UUID.fromString("1ba9cbc8-59fb-46e1-a319-76db7fad5931");
		ByteBuffer.wrap(EMULATOR_ID_NAMESPACE)
				.putLong(uuid.getMostSignificantBits())
				.putLong(uuid.getLeastSignificantBits());
	}

	private static final class Fields
	{
		public static final String ID          = "id";
		public static final String NAME        = "name";
		public static final String VERSION     = "version";
		public static final String DIGEST      = "digest";
		public static final String TAGS        = "tags";
		public static final String PROVENANCE  = "provenance";
		public static final String IMAGE       = "image";

		// Provenance fields
		public static final String URL         = "url";
		public static final String TAG         = "tag";
		public static final String LAYERS      = "layers";
	}
}
