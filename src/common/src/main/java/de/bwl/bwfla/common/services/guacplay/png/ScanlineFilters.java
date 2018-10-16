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

package de.bwl.bwfla.common.services.guacplay.png;

import de.bwl.bwfla.common.services.guacplay.util.MathUtils;
import static de.bwl.bwfla.common.services.guacplay.util.MathUtils.MASK_UNSIGNED_BYTE;


/** A common interface for all stateless scanline's filters. */
interface IScanlineFilter
{
	// Reconstruct the filtered scanline using the bytes
	// in pixels A, B, and C to predict the value for X.
	//     preline:  ... C B ...
	//     curline:  ... A X ...
	// 
	// Abbreviations used in the code of the filters:
	//     R(X) = Reconstructed value of pixel X
	//     F(X) = Filtered value of X
	
	public abstract void reconstruct(int[] curline, int[] preline, byte[] newline, int length, int delta);
}


/** The implementation of the FILTER_NONE filter type. */
final class ScanlineFilterNone implements IScanlineFilter
{
	@Override
	public void reconstruct(int[] curline, int[] preline, byte[] newline, int length, int delta)
	{
		// Reconstruction Function:
		//     R(X) = F(X)
		
		// Symply copy all pixels
		for (int i = 0; i < length; ++i)
			curline[i] = MathUtils.asUByte(newline[i]);
	}
}


/** The implementation of the FILTER_SUB filter type. */
final class ScanlineFilterSub implements IScanlineFilter
{
	@Override
	public void reconstruct(int[] curline, int[] preline, byte[] newline, int length, int delta)
	{
		// Reconstruction Function:
		//     R(X) = R(A) + F(X)

		int value;
		
		// First pixel in a scanline has no left pixel A,
		// hence simply copy the bytes of the first pixel. 
		for (int i = 0; i < delta; ++i)
			curline[i] = MathUtils.asUByte(newline[i]);
		
		// Reconstruct the rest of the pixels
		for (int i = delta, j = 0; i < length; ++i, ++j) {
			value = curline[j] + MathUtils.asUByte(newline[i]);
			curline[i] = value & MASK_UNSIGNED_BYTE;
		}
	}
}


/** The implementation of the FILTER_UP filter type. */
final class ScanlineFilterUp implements IScanlineFilter
{
	@Override
	public void reconstruct(int[] curline, int[] preline, byte[] newline, int length, int delta)
	{
		// Reconstruction Function:
		//     R(X) = R(B) + F(X)
		
		int value;
		
		for (int i = 0; i < length; ++i) {
			value = preline[i] + MathUtils.asUByte(newline[i]);
			curline[i] = value & MASK_UNSIGNED_BYTE;
		}
	}
}


/** The implementation of the FILTER_AVERAGE filter type. */
final class ScanlineFilterAverage implements IScanlineFilter
{
	@Override
	public void reconstruct(int[] curline, int[] preline, byte[] newline, int length, int delta)
	{
		// Reconstruction Function:
		//     R(X) = floor((R(A) + R(B)) / 2) + F(X)
		
		int value;
		
		// First pixel in a scanline has no left pixel A,
		// hence use only the pixel B for reconstruction.
		for (int i = 0; i < delta; ++i) {
			value = (preline[i] >> 1) + MathUtils.asUByte(newline[i]);
			curline[i] = value & MASK_UNSIGNED_BYTE;
		}
		
		// Reconstruct the rest of the pixels
		for (int i = delta, j = 0; i < length; ++i, ++j) {
			value = ((curline[j] + preline[i]) >> 1) + MathUtils.asUByte(newline[i]);
			curline[i] = value & MASK_UNSIGNED_BYTE;
		}
	}
}


/** The implementation of the FILTER_PAETH filter type. */
final class ScanlineFilterPaeth implements IScanlineFilter
{
	@Override
	public void reconstruct(int[] curline, int[] preline, byte[] newline, int length, int delta)
	{
		// Reconstruction Function:
		//     R(X) = PaethPredictor(R(A), R(B), R(C)) + F(X)
		
		int value;
		
		// First pixel in a scanline has no pixels A and C,
		// hence use only the pixel B for reconstruction.
		for (int i = 0; i < delta; ++i) {
			// Finally reconstruct the value
			value = preline[i] + MathUtils.asUByte(newline[i]);
			curline[i] = value & MASK_UNSIGNED_BYTE;
		}
		
		// Reconstruct the rest of the pixels
		for (int i = delta, j = 0; i < length; ++i, ++j) {
			final int a = curline[j];
			final int b = preline[i];
			final int c = preline[j];
		
			// Finally reconstruct the value
			value = ScanlineFilterPaeth.predictor(a, b, c) + MathUtils.asUByte(newline[i]);
			curline[i] = value & MASK_UNSIGNED_BYTE;
		}
	}

	/** Compute the paeth-predictor from passed values. */
	private static final int predictor(int a, int b, int c)
	{
		final int base = a + b - c;
		final int ma = MathUtils.abs(base - a);
		final int mb = MathUtils.abs(base - b);
		final int mc = MathUtils.abs(base - c);
		
		if ((ma <= mb) && (ma <= mc))
			return a;

		else if (mb <= mc)
			return b;
		
		return c;
	}
}
