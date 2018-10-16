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

package de.bwl.bwfla.eaas.cluster.exception;


public class OutOfResourcesException extends AllocationFailureException 
{
    private static final long serialVersionUID = 1926516309922990672L;

    public OutOfResourcesException() {
        super();
    }

    public OutOfResourcesException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public OutOfResourcesException(String message) {
        super(message);
    }

    public OutOfResourcesException(Throwable throwable) {
        super(throwable);
    }
}