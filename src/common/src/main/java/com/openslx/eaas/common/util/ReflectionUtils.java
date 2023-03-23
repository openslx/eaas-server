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

package com.openslx.eaas.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class ReflectionUtils
{
	/** Look up field by name */
	public static <T> Field field(Class<T> clazz, String name)
	{
		try {
			return clazz.getDeclaredField(name);
		}
		catch (Exception error) {
			throw new IllegalArgumentException(error);
		}
	}

	/** Look up method by name */
	public static <T> Method method(Class<T> clazz, String name)
	{
		try {
			return clazz.getMethod(name);
		}
		catch (Exception error) {
			throw new IllegalArgumentException(error);
		}
	}

	/** Look up element's annotation by class */
	public static <T extends Annotation> T annotation(AnnotatedElement element, Class<T> annclazz)
	{
		final var annotation = element.getAnnotation(annclazz);
		if (annotation == null)
			throw new IllegalArgumentException();

		return annotation;
	}
}
