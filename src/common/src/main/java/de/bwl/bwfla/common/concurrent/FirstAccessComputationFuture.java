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

package de.bwl.bwfla.common.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A Future that will use the first call to {@link #get()}.
 * 
 * This Future does not require an external Thread to compute the result.
 * Instead, the result is computed when the first thread will access the Future.
 * This is especially useful in cases when many threads should access the same
 * result but each of them will wait for it and they are supposed to block until
 * the result is ready.
 *
 * @param <T>
 */
public class FirstAccessComputationFuture<T> extends FutureTask<T> {
    AtomicBoolean computing = new AtomicBoolean(false);

    public FirstAccessComputationFuture(Callable<T> callable) {
        super(callable);
    }
    
    public FirstAccessComputationFuture(Runnable runnable, T value) {
        super(runnable, value);
    }

    public T get() throws InterruptedException, ExecutionException {
        if (computing.compareAndSet(false, true)) {
            super.run();
        }
        return super.get();
    }
}
