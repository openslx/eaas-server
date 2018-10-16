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

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A timer that schedules a one-time execution of a given runnable after
 * a specific timeout. As long as the timeout has not expired, this timer
 * can be reset, effectively starting the timeout again.
 * 
 * Note: This implementation creates a lot of new ScheduledFuture instances.
 * Benchmarks (both, on stackoverflow.com and my own) have shown that,
 * once a few instances were created and destroyed, the JVM is *very*
 * fast at creating new instances without much memory overhead. The instance
 * recreation rate for this specific purpose is in tens of thousands per second
 * without a lot of CPU impact and barely any memory overhead.
 * 
 * Note: This class is a slightly modified version of Jason S's
 * ResettableTimer, posted on stackoverflow.com at
 * <http://stackoverflow.com/a/2142661>
 * 
 * @link <http://stackoverflow.com/a/2142661>
 */
public class ResettableTimer {
    final private ScheduledExecutorService scheduler;
    final private long timeout;
    final private TimeUnit timeUnit;
    final private Runnable task;
    // use AtomicReference to manage concurrency 
    // in case reset() gets called from different threads
    final private AtomicReference<ScheduledFuture<?>> ticket
        = new AtomicReference<ScheduledFuture<?>>();


    public ResettableTimer(ScheduledExecutorService scheduler, 
            Duration duration, Runnable task) {
        this(scheduler, duration.toMillis(), TimeUnit.MILLISECONDS, task);
    }
    
    public ResettableTimer(ScheduledExecutorService scheduler, 
            long timeout, TimeUnit timeUnit, Runnable task)
    {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.task = task;
        this.reset(false);
    }

    public ResettableTimer reset(boolean mayInterruptIfRunning) {
        /*
         *  in with the new, out with the old;
         *  this may mean that more than 1 task is scheduled at once for a short time,
         *  but that's not a big deal and avoids some complexity in this code 
         */
        if (ticket.get() != null && ticket.get().isDone()) {
            return this;
        }
        ScheduledFuture<?> newTicket = this.scheduler.schedule(
                this.task, this.timeout, this.timeUnit);
        ScheduledFuture<?> oldTicket = this.ticket.getAndSet(newTicket);
        if (oldTicket != null)
        {
            oldTicket.cancel(mayInterruptIfRunning);
        }
        return this;
    }
}
