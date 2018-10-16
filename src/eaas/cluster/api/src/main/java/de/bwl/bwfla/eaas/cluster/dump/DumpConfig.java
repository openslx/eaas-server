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

package de.bwl.bwfla.eaas.cluster.dump;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;


public final class DumpConfig
{
	private static final String FIELD_EXCLUDE_PREFIX = "!";
	private static final String QUERY_PARAM_FIELDS = "fields";
	
	// Member fields
	private final Queue<String> segments;
	private final Set<String> fieldsToInclude;
	private final Set<String> fieldsToExclude;
	private int segmentCounter;

	public DumpConfig(List<PathSegment> segments)
	{
		this(segments, new MultivaluedHashMap<String, String>());
	}
	
	public DumpConfig(List<PathSegment> segments, MultivaluedMap<String, String> query)
	{
		if (segments == null)
			throw new IllegalArgumentException();
		
		if (query == null)
			throw new IllegalArgumentException();
		
		this.segments = new ArrayDeque<String>();
		this.fieldsToInclude = new HashSet<String>();
		this.fieldsToExclude = new HashSet<String>();
		this.segmentCounter = (segments.isEmpty()) ? 1 : segments.size();
		
		for (PathSegment segment : segments)
			this.segments.add(segment.toString());
		
		if (query.containsKey(QUERY_PARAM_FIELDS)) {
			for (String value : query.get(QUERY_PARAM_FIELDS)) {
				for (String field : value.split(",")) {
					if (field.startsWith(FIELD_EXCLUDE_PREFIX))
						fieldsToExclude.add(field.substring(1));
					else fieldsToInclude.add(field);
				}
			}
		}
	}
	
	public void begin()
	{
		--segmentCounter;
	}
	
	public void end()
	{
		++segmentCounter;
	}
	
	public String nextUrlSegment()
	{
		return segments.poll();
	}

	public boolean hasMoreUrlSegments()
	{
		return !segments.isEmpty();
	}
	
	public Set<String> fields()
	{
		return fieldsToInclude;
	}
	
	public boolean included(String field)
	{
		// Check fields only if dumping the goal resource!
		
		if (segmentCounter != 0)
			return true;
		
		if (fieldsToExclude.contains(field))
			return false;
		
		return (fieldsToInclude.isEmpty() || fieldsToInclude.contains(field));
	}
	
	public boolean excluded(String field)
	{
		return !this.included(field);
	}
	
	public boolean hasFieldsToExclude()
	{
		return (!fieldsToInclude.isEmpty() || !fieldsToExclude.isEmpty());
	}
}
