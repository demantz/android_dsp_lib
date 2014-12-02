package com.mantz_it.android_dsp_lib;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;

/**
 * <h1>Android DSP library - Mixer 8bit</h1>
 *
 * Module:      Mixer_8Bit.java
 * Description: This class implements methods to do converting (byte->float) and
 * 				down-mixing at the same time.
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
public class Mixer_8Bit {
	private RenderScript rs;
	ScriptC_mixer_8bit script;
	private Allocation realLutAlloc;
	private Allocation imagLutAlloc;
	private Allocation inAlloc;
	private int cosineLength;
	private int cosineFrequency;

	public Mixer_8Bit(int inputSize, int maxCosineLength) {
		rs = AndroidDSPLib.getRenderScript();
		realLutAlloc = Allocation.createSized(rs, Element.F32(rs), maxCosineLength*256);
		imagLutAlloc = Allocation.createSized(rs, Element.F32(rs), maxCosineLength*256);
		inAlloc = Allocation.createSized(rs, Element.I8(rs), inputSize);
		script = new ScriptC_mixer_8bit(rs, AndroidDSPLib.getResources(), R.raw.mixer_8bit);
		script.bind_lutReal(realLutAlloc);
		script.bind_lutImag(imagLutAlloc);
	}

	public int getCosineFrequency() {
		return cosineFrequency;
	}

	public void generateLookupTable(int sampleRate, int mixFrequency, int cosineLength, boolean signed) {
		System.out.println("Generating mixer lookup table of length " + cosineLength + " freq=" + mixFrequency);
		this.cosineLength = cosineLength;
		this.cosineFrequency = mixFrequency;
		script.set_cosineLength(cosineLength);
		script.set_mixFrequency(mixFrequency);
		script.set_sampleRate(sampleRate);
		script.set_signedFlag(signed ? (short)1 : (short)0);
		script.forEach_updateLut(realLutAlloc);		// argument is just a dummy allocation that has the correct length
		script.set_baseIndex(0);
		realLutAlloc.syncAll(Allocation.USAGE_SCRIPT);
		imagLutAlloc.syncAll(Allocation.USAGE_SCRIPT);
	}

	public int mixFromSignedInterleaved8Bit(byte[] in, Allocation outReal, Allocation outImag, int offset, int length) {
		inAlloc.copyFrom(in);
		script.set_outReal(outReal);
		script.set_outImag(outImag);
		script.set_offset(offset);
		script.set_len(length);
		long baseIndex = script.get_baseIndex();
		script.forEach_mixSignedInterleavedKernel(inAlloc);
		int count = Math.min(in.length/2, length-offset);
		script.set_baseIndex((baseIndex+count) % cosineLength);
		return count;
	}

	public int mixFromUnsignedInterleaved8Bit(byte[] in, Allocation outReal, Allocation outImag, int offset, int length) {
		inAlloc.copyFrom(in);
		script.set_outReal(outReal);
		script.set_outImag(outImag);
		script.set_offset(offset);
		script.set_len(length);
		long baseIndex = script.get_baseIndex();
		script.forEach_mixUnsignedInterleavedKernel(inAlloc);
		int count = Math.min(in.length/2, length-offset);
		script.set_baseIndex((baseIndex+count) % cosineLength);
		return count;
	}
}
