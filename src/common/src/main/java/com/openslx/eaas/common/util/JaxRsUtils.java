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

import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;


public class JaxRsUtils
{
	/** Get element's relative URI template */
	public static String getUriTemplate(AnnotatedElement element)
	{
		return ReflectionUtils.annotation(element, Path.class)
				.value();
	}

	/** Get element's absolute URI template */
	public static <T> String getUriTemplate(String baseurl, AnnotatedElement element)
	{
		final var path = JaxRsUtils.getUriTemplate(element);
		return JaxRsUtils.join(baseurl, path);
	}

	/** Get named method's relative URI template */
	public static <T> String getUriTemplate(Class<T> clazz, String method)
	{
		final var element = ReflectionUtils.method(clazz, method);
		return JaxRsUtils.getUriTemplate(element);
	}

	/** Get named method's absolute URI template */
	public static <T> String getUriTemplate(String baseurl, Class<T> clazz, String method)
	{
		final var path = JaxRsUtils.getUriTemplate(clazz, method);
		return JaxRsUtils.join(baseurl, path);
	}

	/** Join base URL with subpaths into a single URL */
	public static String join(String baseurl, String... subpaths)
	{
		if (subpaths == null || subpaths.length == 0)
			return baseurl;

		final var separator = "/";
		int capacity = baseurl.length();
		for (final var subpath : subpaths)
			capacity += separator.length() + subpath.length();

		final var sb = new StringBuilder(capacity);
		int endpos = baseurl.length();
		if (baseurl.endsWith(separator))
			--endpos;

		sb.append(baseurl, 0, endpos);

		for (final var subpath : subpaths) {
			if (!subpath.startsWith(separator))
				sb.append(separator);

			endpos = subpath.length();
			if (subpath.endsWith(separator))
				--endpos;

			sb.append(subpath, 0, endpos);
		}

		return sb.toString();
	}

	/** Extract and collect all annotated header parameters in given object */
	public static void extractHeaderParams(Object object, Map<String, String> values)
	{
		JaxRsUtils.collect(new HeaderParamExtractor(), object, values);
	}

	/** Extract and collect all annotated query parameters in given object */
	public static void extractQueryParams(Object object, Map<String, String> values)
	{
		JaxRsUtils.collect(new QueryParamExtractor(), object, values);
	}


	// ===== Internal Helpers ====================

	private static abstract class ParamExtractor<A extends Annotation>
	{
		private final Class<A> annclazz;

		public ParamExtractor(Class<A> annclazz)
		{
			this.annclazz = annclazz;
		}

		public Class<A> getAnnotationClass()
		{
			return annclazz;
		}

		public A getAnnotation(AnnotatedElement element)
		{
			return ReflectionUtils.annotation(element, this.getAnnotationClass());
		}

		public abstract String getAnnotatedValue(AnnotatedElement element);

		public Object extract(Field field, Object object) throws IllegalAccessException
		{
			return field.get(object);
		}
	}

	private static class HeaderParamExtractor extends ParamExtractor<HeaderParam>
	{
		public HeaderParamExtractor()
		{
			super(HeaderParam.class);
		}

		public String getAnnotatedValue(AnnotatedElement element)
		{
			return this.getAnnotation(element)
					.value();
		}
	}

	private static class QueryParamExtractor extends ParamExtractor<QueryParam>
	{
		public QueryParamExtractor()
		{
			super(QueryParam.class);
		}

		public String getAnnotatedValue(AnnotatedElement element)
		{
			return this.getAnnotation(element)
					.value();
		}
	}

	private static <A extends Annotation> void collect(ParamExtractor<A> extractor, Object object, Map<String, String> values)
	{
		final Consumer<Class<?>> collector = (clazz) -> {
			try {
				for (final var field : clazz.getDeclaredFields()) {
					if (!field.isAnnotationPresent(extractor.getAnnotationClass()))
						continue;

					// enable access to non-public fields
					field.setAccessible(true);

					final var name = extractor.getAnnotatedValue(field);
					final var value = extractor.extract(field, object);
					if (value != null)
						values.put(name, value.toString());
				}
			}
			catch (Exception error) {
				throw new IllegalArgumentException(error);
			}
		};

		for (var clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass())
			collector.accept(clazz);
	}
}
