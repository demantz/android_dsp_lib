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

float *lutReal;            // real lookup table (length 256*cosineLength). calculated by updateLut()
float *lutImag;            // imag lookup table (length 256*cosineLength). calculated by updateLut()
uint32_t baseIndex;        // base index for the lookup table
uint32_t cosineLength;     // length of the cosine (lookup table length / 256)
uint32_t mixFrequency;     // frequency of the cosine that is mixed to the signal
uint32_t sampleRate;       // sample rate of the incoming signals
uint8_t signedFlag;        // flag that indicates if lookup table should be based on signed (!=0) or unsigned (==0) values
rs_allocation outReal;     // Output Allocation for real array
rs_allocation outImag;     // Output Allocation for imag array
uint32_t offset;           // Start index in the output allocations (first index that will be written)
uint32_t len;              // Length of the output allocations (length-1 is max index that will be written)

void updateLut(const float *in, uint32_t x) {
    if(x < cosineLength) {
        if( signedFlag != 0 ) { // signed
            float cosineAtT = cos(2 * M_PI * mixFrequency * x / sampleRate);
            float sineAtT   = sin(2 * M_PI * mixFrequency * x / sampleRate);
            for (int i = 0; i < 256; i++) {
                *(lutReal + x*256 + i) = (i - 128) / 128.0f * cosineAtT;
                *(lutImag + x*256 + i) = (i - 128) / 128.0f * sineAtT;
            }
        } else { // unsigned
            float cosineAtT = cos(2 * M_PI * mixFrequency * x / sampleRate);
            float sineAtT   = sin(2 * M_PI * mixFrequency * x / sampleRate);
            for (int i = 0; i < 256; i++) {
                *(lutReal + x*256 + i) = (i - 127.4f) / 128.0f * cosineAtT;
                *(lutImag + x*256 + i) = (i - 127.4f) / 128.0f * sineAtT;
            }
        }
    }
}

void mixSignedInterleavedKernel(const char *in, uint32_t x) {
    uint32_t outIndex = x >> 1;
    if(outIndex + offset >= len)
        return; // reached max index
    uint32_t lutOffset = (baseIndex + outIndex) % cosineLength;
    if(x & 0x01)
        rsSetElementAt(outImag, lutImag + lutOffset*256 + *in + 128, outIndex + offset);
    else
        rsSetElementAt(outReal, lutReal + lutOffset*256 + *in + 128, outIndex + offset);
}

void mixUnsignedInterleavedKernel(const char *in, uint32_t x) {
    uint32_t outIndex = x >> 1;
    if(outIndex + offset >= len)
        return; // reached max index
    uint32_t lutOffset = (baseIndex + outIndex) % cosineLength;
    if(x & 0x01)
        rsSetElementAt(outImag, lutImag + lutOffset*256 + (*in & 0xff), outIndex + offset);
    else
        rsSetElementAt(outReal, lutReal + lutOffset*256 + (*in & 0xff), outIndex + offset);
}