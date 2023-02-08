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

package com.openslx.eaas.common.concurrent;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class ParallelProcessors
{
	public static <T> ParallelConsumer<T> consumer(Consumer<T> consumer)
	{
		return ParallelProcessors.consumer((v) -> true, consumer);
	}

	public static <T> ParallelConsumer<T> consumer(Predicate<T> filter, Consumer<T> consumer)
	{
		return new ParallelConsumer<>(filter, consumer);
	}

	public static <T,R> ParallelReducer<T,R> reducer(Function<T,R> mapper, BinaryOperator<R> reducer)
	{
		return new ParallelReducer<>((v) -> true, mapper, reducer);
	}

	public static <T,R> ParallelReducer<T,R> reducer(Predicate<T> filter, Function<T,R> mapper, BinaryOperator<R> reducer)
	{
		return new ParallelReducer<>(filter, mapper, reducer);
	}
}
