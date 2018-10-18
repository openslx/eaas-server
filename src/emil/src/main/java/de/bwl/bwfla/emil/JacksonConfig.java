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

package de.bwl.bwfla.emil;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonConfig extends ResteasyJackson2Provider {
    public void writeTo(Object value, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType json,
            MultivaluedMap<String, Object> headers, OutputStream body)
            throws IOException {
        // Disable serialization of null or absent fields in JSON
        ObjectMapper mapper = locateMapper(type, json);
        mapper.setSerializationInclusion(Include.NON_NULL);
        
        super.writeTo(value, type, genericType, annotations, json, headers, body);
    }
}
