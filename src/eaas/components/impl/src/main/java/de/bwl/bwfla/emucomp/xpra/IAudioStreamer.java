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

package de.bwl.bwfla.emucomp.xpra;

import java.util.concurrent.TimeUnit;


public interface IAudioStreamer
{
    String pollServerControlMessage(long timeout, TimeUnit unit) throws InterruptedException;
    void postClientControlMessage(char[] payload) throws IllegalArgumentException;
    void postClientControlMessage(char[] payload, int offset, int length) throws IllegalArgumentException;
    void postClientControlMessage(String payload) throws IllegalArgumentException;
    void play();
    void stop();
    void close();
    boolean isClosed();
}
