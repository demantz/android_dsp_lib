/**
 * Android DSP library - Quadrature Demodulator
 *
 * Module:      quad_demod.rs
 * Description: This kernel can do quadrature demodulation
 *
 * @author Dennis Mantz
 *
 * Copyright (C) 2016 Dennis Mantz
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.mantz_it.android_dsp_lib)

float gain;                 // Quadrature gain
float historyRe;            // history sample from the last demodulation cycle
float historyIm;
rs_allocation inReal;       // Input packet
rs_allocation inImag;
rs_allocation outReal;      // Output packet

uint32_t offsetIn;          // Start index in the input allocations
uint32_t offsetOut;         // Start index in the output allocations (first index that will be written)
uint32_t len;               // Length of the output allocations (len-1 is max index that will be written)

/*
 * Will copy the last sample from the input packet (real and imag parts) to the history variables
 */
void saveHistory() {
    historyRe = rsGetElementAt_float(inReal, offsetIn + len -1);
    historyIm = rsGetElementAt_float(inImag, offsetIn + len -1);
}

/*
 * Kernel: demodulate a complex signal
 *         Input is expected in inReal and inImag and output will be stored in outReal
 * @param out       dummy argument (is not used, but has to have the same size as outReal)
 * @param x         position inside 'out' (given by the runtime)
 */
void demod(float* out, uint32_t x) {
    float imInPrev, imIn, reInPrev, reIn;
    float resultReal = 0;
    float resultImag = 0;

    if(x + offsetOut >= len)
            return; // reached max index

    if(x == 0) {
        // Use the history Sample
        reIn = rsGetElementAt_float(inReal, offsetIn);
        imIn = rsGetElementAt_float(inImag, offsetIn);
        resultReal = reIn * historyRe + imIn * historyIm;
        resultImag = imIn * historyRe - reIn * historyIm;
    } else {
        reIn = rsGetElementAt_float(inReal, offsetIn + x);
        imIn = rsGetElementAt_float(inImag, offsetIn + x);
        reInPrev = rsGetElementAt_float(inReal, offsetIn + x-1);
        imInPrev = rsGetElementAt_float(inImag, offsetIn + x-1);
        resultReal = reIn * reInPrev + imIn * imInPrev;
        resultImag = imIn * reInPrev - reIn * imInPrev;
    }
    rsSetElementAt_float(outReal, gain * atan2(resultImag, resultReal), offsetOut + x);
}