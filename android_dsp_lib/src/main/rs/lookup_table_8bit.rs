/**
 * Android DSP library - Lookup Table for 8 bit input values
 *
 * Module:      lookup_table_8bit.rs
 * Description: This kernel can do type conversions of 8 bit values to float
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
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.mantz_it.android_dsp_lib)

float *lut;                // lookup table (length 256). must be set by caller.
rs_allocation outReal;     // Output Allocation for real array in case of interleaved kernels
rs_allocation outImag;     // Output Allocation for imag array in case of interleaved kernels
uint32_t offset;           // Start index in the output allocations (first index that will be written)
uint32_t len;              // Length of the output allocations (length-1 is max index that will be written)

void convertSignedInterleavedKernel(const char *in, uint32_t x) {
    uint32_t outIndex = x >> 1;
    if(outIndex + offset >= len)
        return; // reached max index
    if(x & 0x01)
        rsSetElementAt_float(outImag, *(lut + *in + 128), outIndex + offset);
    else
        rsSetElementAt_float(outReal, *(lut + *in + 128), outIndex + offset);
}

void convertUnsignedInterleavedKernel(const char *in, uint32_t x) {
    uint32_t outIndex = x >> 1;
    if(outIndex + offset >= len)
        return; // reached max index
    if(x & 0x01)
        rsSetElementAt_float(outImag, *(lut + (*in & 0xff)), outIndex + offset);
    else
        rsSetElementAt_float(outReal, *(lut + (*in & 0xff)), outIndex + offset);
}