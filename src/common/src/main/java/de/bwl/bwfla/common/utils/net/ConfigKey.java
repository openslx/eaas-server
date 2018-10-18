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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;


/**
 * This is a verbatim copy of tamaya's @Config annotation for the sole purpose
 * of interjecting some @Config injection points without a buttload of code
 * to be written first for a CDI extension that observes ProducerMethod event.  
 *  
 * @author thomas
 */

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface ConfigKey {

    /**
     * Defines the configuration property keys to be used. Hereby the first non null value evaluated is injected as
     * property value.
     *
     * @return the property keys, not null. If empty, the field or property name (of a setter method) being injected
     * is used by default.
     */
    @Nonbinding
    String[] value() default {};

    /**
     * The default value to be injected, if none of the configuration keys could be resolved. If no key has been
     * resolved and no default value is defined, it is, by default, handled as a deployment error. Depending on the
     * extension loaded default values can be fixed Strings or even themselves resolvable. For typed configuration of
     * type T entries that are not Strings the default value must be a valid input to a corresponding
     * {@link org.apache.tamaya.spi.PropertyConverter}.
     * 
     * @return default value used in case resolution fails.
     */
    @Nonbinding
    String defaultValue() default "";

    /**
     * FLag that defines if a configuration property is required. If a required
     * property is missing, a {@link org.apache.tamaya.ConfigException} is raised.
     * Default is {@code true}.
     * @return the flag value.
     */
    @Nonbinding
    boolean required() default true;
}