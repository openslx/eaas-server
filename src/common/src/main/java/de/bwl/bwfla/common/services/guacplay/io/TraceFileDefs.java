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

package de.bwl.bwfla.common.services.guacplay.io;


/** Constants, for internal usage (package-private). */
class TraceFileDefs
{
	/* Current Version */
	public static final int VERSION_MAJOR  = 1;
	public static final int VERSION_MINOR  = 1;
	
	/* Prefixes */
	public static final char PREFIX_COMMENT        = '#';
	public static final char PREFIX_COMMAND        = '@';
	public static final char PREFIX_SPECIAL_BLOCK  = '!';
	
	/* Special Symbols */
	public static final char SYMBOL_SPACE      = ' ';
	public static final char SYMBOL_TAB        = '\t';
	public static final char SYMBOL_NEWLINE    = '\n';
	public static final char SYMBOL_STRING     = '"';
	
	/** String, used for indentation. */
	public static final String INDENTATION     = "\t";
	
	/* Delimiters */
	public static final char DELIMITER_VALUES  = '|';
	
	/* Commands */
	public static final String COMMAND_SIGNATURE    = PREFIX_COMMAND + "GUACPLAY";
	public static final String COMMAND_BLOCK_BEGIN  = PREFIX_COMMAND + "block";
	public static final String COMMAND_BLOCK_END    = PREFIX_COMMAND + "endblock";
	
	/* Block Names */
	public static final String BLOCKNAME_METADATA   = PREFIX_SPECIAL_BLOCK + "metadata";
	public static final String BLOCKNAME_INDEX      = PREFIX_SPECIAL_BLOCK + "blkindex";
	
	/* Metadata-Block Constants (using YAML-Format) */
	public static final String YAML_BEGIN_PART         = "---";
	public static final char YAML_PRESERVE_NEWLINES    = '|';
	public static final char YAML_TAG_PREFIX           = '!';
	public static final char YAML_KV_DELIMITER         = ':';
}
