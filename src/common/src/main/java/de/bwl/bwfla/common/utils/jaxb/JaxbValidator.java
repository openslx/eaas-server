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

package de.bwl.bwfla.common.utils.jaxb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlElement;

public class JaxbValidator {
    final static Logger log = Logger.getLogger(JaxbValidator.class.getName());

    public static <T> void validate(T target) throws ValidationException {
        validateRequired(target);
        validateNillable(target);
    }

    public static <T> void validateRequired(T target)
            throws ValidationException {
        Class<? extends Object> targetClass = target.getClass();

        StringBuilder errors = new StringBuilder();

        List<Field> fields = getAllFields(targetClass);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                // recurse into fields
                if (field.get(target) != null && JaxbType.class
                        .isAssignableFrom(field.get(target).getClass())) {
                    validateRequired(field.get(target));
                }


                XmlElement annotation = field.getAnnotation(XmlElement.class);
                if (annotation != null && annotation.required()) {
                    // TODO: we cannot detect primitive types like this
                    if (field.get(target) == null) {
                        // if there is a default value, use it instead
                        if (!annotation.defaultValue().equals("\u0000")) {
                            // Strings
                            if (field.getType().equals(String.class)) {
                                field.set(target, annotation.defaultValue());
                            }

                            // boxed types
                            else if (field.getType().equals(Byte.class)) {
                                field.set(target, Byte
                                        .parseByte(annotation.defaultValue()));
                            } else if (field.getType().equals(Short.class)) {
                                field.set(target, Short
                                        .parseShort(annotation.defaultValue()));
                            } else if (field.getType().equals(Integer.class)) {
                                field.set(target, Integer
                                        .valueOf(annotation.defaultValue()));
                            } else if (field.getType().equals(Long.class)) {
                                field.set(target, Long
                                        .parseLong(annotation.defaultValue()));
                            } else if (field.getType().equals(Boolean.class)) {
                                field.set(target, Boolean.parseBoolean(
                                        annotation.defaultValue()));
                            } else if (field.getType().equals(Float.class)) {
                                field.set(target, Float
                                        .parseFloat(annotation.defaultValue()));
                            } else if (field.getType().equals(Double.class)) {
                                field.set(target, Double.parseDouble(
                                        annotation.defaultValue()));
                            } else if (field.getType()
                                    .equals(Character.class)) {
                                field.set(target, Character.valueOf(
                                        annotation.defaultValue().charAt(0)));
                            }
                            continue;
                        }
                        
                        if (errors.length() != 0) {
                            errors.append(" ");
                        }
                        String message = String.format(
                                "%s: required field '%s' is null.",
                                targetClass.getSimpleName(), field.getName());
                        log.log(Level.SEVERE, message);
                        errors.append(message);
                    }
                }
            } catch (IllegalArgumentException e) {
                log.log(Level.SEVERE, field.getName(), e);
            } catch (IllegalAccessException e) {
                log.log(Level.SEVERE, field.getName(), e);
            }
        }
        if (errors.length() != 0) {
            throw new ValidationException(errors.toString());
        }
    }

    public static <T> void validateNillable(T target)
            throws ValidationException {
        Class<? extends Object> targetClass = target.getClass();

        StringBuilder errors = new StringBuilder();
        
        List<Field> fields = getAllFields(targetClass);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                // recurse into fields
                if (field.get(target) != null && JaxbType.class
                        .isAssignableFrom(field.get(target).getClass())) {
                    validateNillable(field.get(target));
                }

                XmlElement annotation = field.getAnnotation(XmlElement.class);
                if (annotation != null && !annotation.nillable()
                        && annotation.required()) {
                    if (field.get(target) == null) {
                        if (errors.length() != 0) {
                            errors.append(" ");
                        }
                        String message = String.format(
                                "%s: nillable = false , field '%s' is null.",
                                targetClass.getSimpleName(), field.getName());
                        log.log(Level.SEVERE, message);
                        errors.append(message);
                    }
                }
            } catch (IllegalArgumentException e) {
                log.log(Level.SEVERE, field.getName(), e);
            } catch (IllegalAccessException e) {
                log.log(Level.SEVERE, field.getName(), e);
            }
        }
        if (errors.length() != 0) {
            throw new ValidationException(errors.toString());
        }
    }
    
    private static List<Field> getAllFields(Class<?> targetClass) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> klass = targetClass;
        while (klass.getSuperclass() != null) {
            fields.addAll(Arrays.asList(klass.getDeclaredFields()));
            klass = klass.getSuperclass();
        }
        return fields;
    }
}