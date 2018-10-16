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

package de.bwl.bwfla.common.utils.net;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.ConfigurationProvider;

@ApplicationScoped
public class PortRangeProvider {
    public static class Port {
        final private PortRange range;
        private int port = -1;
        
        Port(PortRange range) {
            this.range = range;
        }

        public int get() throws IOException {
            if (this.port < 0) {
                port = this.range.getNextAvailablePort();
            }
            return this.port;
        }
        
        public void release() {
            this.range.releasePort(this.port);
        }
    }
    
    private static class PortRange {
        final private int start;
        final private int end;
        private Set<Integer> used = new HashSet<Integer>();

        public PortRange(int start, int end) {
            super();
            this.start = start;
            this.end = end;
        }

        synchronized public int getNextAvailablePort() throws IOException {
            int port = this.start;
            for (; port <= this.end; ++port) {
                // check whether the port is available
                if (used.contains(port)) {
                    continue;
                }
                used.add(port);
                return port;
            }
            throw new IOException("No more ports available in the range [" + this.start + ", " + this.end + "]");
        }
        
        synchronized public void releasePort(int port) {
            this.used.remove(port);
        }
    }

    // for every configuration key there is a range instance
    protected Map<String, PortRange> ranges = new HashMap<String, PortRange>();    
    
    @Produces
    @Dependent
    @ConfigKey
    public Port getPort(InjectionPoint ip) {
        ConfigKey config = ip.getAnnotated().getAnnotation(ConfigKey.class);
        // try to find port range from config
        String value = null;
        String identifier = null;
        for (String s : config.value()) {
            value = ConfigurationProvider.getConfiguration().get(s);
            if (value != null) {
                identifier = s;
                break;
            }
        }
        
        // if either is null, the key was not configured
        if (value == null || identifier == null) {
            throw new ConfigException(String.format(
                    "Cannot resolve any of the possible configuration keys: %s. Please provide one of the given keys " +
                            "with a value in your configuration sources.",
                    Arrays.toString(config.value())));
        }
        
        // make value final to use it in the lambda
        final String value2 = value;
        PortRange range = this.ranges.computeIfAbsent(identifier, (id) -> {
            final Matcher m = Pattern.compile("^(\\d+)\\s*-\\s*(\\d+)$").matcher(value2);
            if (m.matches()) {
                int start = Integer.parseInt(m.group(1));
                int end = Integer.parseInt(m.group(2));
                return new PortRange(start, end);
            } else {
                return null;
            }
        });
        
        return new Port(range);
    }
}
