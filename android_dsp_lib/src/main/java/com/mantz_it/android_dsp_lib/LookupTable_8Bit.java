package com.mantz_it.android_dsp_lib;

import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;

/**
 * Android DSP library - Lookup Table 8bit
 *
 * Module:      LookupTable_8Bit.java
 * Description: This class can do type conversion (e.g. IQ bytes -> floats) by using a lookup table
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
public class LookupTable_8Bit {
	private RenderScript rs;
	private ScriptC_lookup_table_8bit script;
	private Allocation lut;
	private Allocation inAlloc;

	public LookupTable_8Bit(int inputSize, float[] lookupTable) {
		if(lookupTable == null || lookupTable.length != 256)
			throw new IllegalArgumentException("8-bit lookup table has to have exactly 256 elements!");

		rs = AndroidDSPLib.getRenderScript();

		lut = Allocation.createSized(rs, Element.F32(rs), 256);
		inAlloc = Allocation.createSized(rs, Element.I8(rs), inputSize);
		lut.copyFrom(lookupTable);

		script = new ScriptC_lookup_table_8bit(rs, AndroidDSPLib.getResources(), R.raw.lookup_table_8bit);
		script.bind_lut(lut);
	}

	public void convertFromSignedInterleaved8Bit(byte[] in, Allocation outReal, Allocation outImag, int offset, int length) {
		inAlloc.copyFrom(in);
		script.set_outReal(outReal);
		script.set_outImag(outImag);
		script.set_offset(offset);
		script.set_len(length);
		script.forEach_convertSignedInterleavedKernel(inAlloc);
	}

	public void convertFromUnsignedInterleaved8Bit(byte[] in, Allocation outReal, Allocation outImag, int offset, int length) {
		inAlloc.copyFrom(in);
		script.set_outReal(outReal);
		script.set_outImag(outImag);
		script.set_offset(offset);
		script.set_len(length);
		script.forEach_convertUnsignedInterleavedKernel(inAlloc);
	}

	public static float[] createSigned8BitLookupTable() {
		float[] lut = new float[256];
		for (int i = 0; i < 256; i++)
			lut[i] = (i-128) / 128.0f;
		return lut;
	}

	public static float[] createUnsigned8BitLookupTable() {
		float[] lut = new float[256];
		for (int i = 0; i < 256; i++)
			lut[i] = (i-127.4f) / 128.0f;
		return lut;
	}
}
