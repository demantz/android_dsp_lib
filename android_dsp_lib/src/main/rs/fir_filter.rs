/**
 * Android DSP library - Mixer for 8 bit input values
 *
 * Module:      mixer_8bit.rs
 * Description: This kernel can do frequency shift and conversion of 8 bit values to float
                (e.g. IQ bytes -> floats) by using a lookup table
 *
 * @author Dennis Mantz
 *
 * Copyright (C) 2014 Dennis Mantz
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
#pragma rs java_package_name(com.mantz_it.android_dsp_lib)

float* tapsReal;            // Real part of the filter taps
float* tapsImag;            // Imaginary part of the filter taps (may not be used)
float* remainderReal;       // history samples that are used for the next filter operation
float* remainderImag;
rs_allocation inReal;       // Input packet
rs_allocation inImag;
rs_allocation outReal;      // Output packet
rs_allocation outImag;

uint32_t filterOrder;       // Length of the taps array
uint32_t decimation;        // Decimation factor. set to 1 for no decimation
uint32_t offsetIn;          // Start index in the input allocations
uint32_t offsetOut;         // Start index in the output allocations (first index that will be written)
uint32_t len;               // Length of the output allocations (length-1 is max index that will be written)

/*
 * Will set the remainder samples to zero (initial state)
 */
void clearRemainders() {
    uint32_t i;
    for(i=0; i < filterOrder-1; i++) {
        remainderReal[i] = 0;
        remainderImag[i] = 0;
    }
}

/*
 * Will copy the remaining samples from the input packet (real and imag parts) to the remainder arrays
 */
void updateRemainders() {
    uint32_t i;
    uint32_t index = offsetIn + ((len-offsetOut)*decimation) - filterOrder + 1; // length of the input alloc minus length of the remainders
    for(i=0; i < filterOrder-1; i++) {
        remainderReal[i] = rsGetElementAt_float(inReal, index + i);
        remainderImag[i] = rsGetElementAt_float(inImag, index + i);
    }
}

/*
 * Will copy the remaining samples from the input packet (only real part) to the remainder array
 */
void updateRealRemainders() {
    uint32_t i;
    uint32_t index = offsetIn + ((len-offsetOut)*decimation) - filterOrder + 1; // length of the input alloc minus length of the remainders
    for(i=0; i < filterOrder-1; i++) {
        remainderReal[i] = rsGetElementAt_float(inReal, index + i);
    }
}

/*
 * Kernel: filter a complex signal with real filter taps and decimate
 *         Input is expected in inReal and inImag and output will be stored in outReal and outImag
 * @param out       dummy argument (is not used, but has to have the same size as outReal and outImag)
 * @param x         position inside 'out' (given by the runtime)
 */
void filterAndDecimate(float* out, uint32_t x) {
    int32_t i;
    int32_t inIndex = offsetIn + x * decimation - filterOrder + 1;
    float resultReal = 0;
    float resultImag = 0;

    if(x + offsetOut >= len)
            return; // reached max index

    if(inIndex >= 0) {
        // All input values are located in the in-allocations
        for(i=0; i<=filterOrder; i++) {
            resultReal += rsGetElementAt_float(inReal, inIndex+i) * tapsReal[i];
            resultImag += rsGetElementAt_float(inImag, inIndex+i) * tapsReal[i];
        }
    } else {
        // At least one input value is located in the remainder
        for(i=0; i<=filterOrder; i++) {
            if(inIndex+i < 0) {
                resultReal += remainderReal[inIndex+i+filterOrder-1] * tapsReal[i];
                resultImag += remainderImag[inIndex+i+filterOrder-1] * tapsReal[i];
            } else {
                resultReal += rsGetElementAt_float(inReal, inIndex+i) * tapsReal[i];
                resultImag += rsGetElementAt_float(inImag, inIndex+i) * tapsReal[i];
            }
        }
    }
    rsSetElementAt_float(outReal, resultReal, x + offsetOut);
    rsSetElementAt_float(outImag, resultImag, x + offsetOut);
}

/*
 * Kernel: filter a real signal with real filter taps and decimate
 *         Input is expected in inReal and output will be stored in outReal
 * @param out       dummy argument (is not used, but has to have the same size as outReal)
 * @param x         position inside 'out' (given by the runtime)
 */
void filterRealSignalAndDecimate(float* out, uint32_t x) {
    int32_t i;
    int32_t inIndex = offsetIn + x * decimation - filterOrder + 1;
    float resultReal = 0;

    if(x + offsetOut >= len)
            return; // reached max index

    if(inIndex >= 0) {
        // All input values are located in the in-allocations
        for(i=0; i<=filterOrder; i++)
            resultReal += rsGetElementAt_float(inReal, inIndex+i) * tapsReal[i];
    } else {
        // At least one input value is located in the remainder
        for(i=0; i<=filterOrder; i++) {
            if(inIndex+i < 0)
                resultReal += remainderReal[inIndex+i+filterOrder-1] * tapsReal[i];
            else
                resultReal += rsGetElementAt_float(inReal, inIndex+i) * tapsReal[i];
        }
    }
    rsSetElementAt_float(outReal, resultReal, x + offsetOut);
}

/*
 * Kernel: filter a complex signal with complex filter taps and decimate
 *         Input is expected in inReal and inImag and output will be stored in outReal and outImag
 * @param out       dummy argument (is not used, but has to have the same size as outReal and outImag)
 * @param x         position inside 'out' (given by the runtime)
 */
void filterComplexTapsAndDecimate(float* out, uint32_t x) {
    int32_t i;
    int32_t inIndex = offsetIn + x * decimation - filterOrder + 1;
    float resultReal = 0;
    float resultImag = 0;

    if(x + offsetOut >= len)
            return; // reached max index

    if(inIndex >= 0) {
        // All input values are located in the in-allocations
        for(i=0; i<=filterOrder; i++) {
            resultReal += rsGetElementAt_float(inReal, inIndex+i) * tapsReal[i] - rsGetElementAt_float(inImag, inIndex+i) * tapsImag[i];
            resultImag += rsGetElementAt_float(inReal, inIndex+i) * tapsImag[i] + rsGetElementAt_float(inImag, inIndex+i) * tapsReal[i];
        }
    } else {
        // At least one input value is located in the remainder
        for(i=0; i<=filterOrder; i++) {
            if(inIndex+i < 0) {
                resultReal += remainderReal[inIndex+i+filterOrder-1] * tapsReal[i] - remainderImag[inIndex+i+filterOrder-1] * tapsImag[i];
                resultImag += remainderReal[inIndex+i+filterOrder-1] * tapsImag[i] + remainderImag[inIndex+i+filterOrder-1] * tapsReal[i];
            } else {
                resultReal += rsGetElementAt_float(inReal, inIndex+i) * tapsReal[i] - rsGetElementAt_float(inImag, inIndex+i) * tapsImag[i];
                resultImag += rsGetElementAt_float(inReal, inIndex+i) * tapsImag[i] + rsGetElementAt_float(inImag, inIndex+i) * tapsReal[i];
            }
        }
    }
    rsSetElementAt_float(outReal, resultReal, x + offsetOut);
    rsSetElementAt_float(outImag, resultImag, x + offsetOut);
}