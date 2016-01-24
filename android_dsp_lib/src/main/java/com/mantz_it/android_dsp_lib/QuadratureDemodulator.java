package com.mantz_it.android_dsp_lib;

import android.support.v8.renderscript.RenderScript;

/**
 * Android DSP library - Quadrature Demodulator
 *
 * Module:      QuadratureDemodulator.java
 * Description: This class implements a quadrature demodulator
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
public class QuadratureDemodulator {
	private RenderScript rs;
	private ScriptC_quad_demod script;
	private float gain;

	public QuadratureDemodulator(float gain) {
		this.rs = AndroidDSPLib.getRenderScript();
		this.gain = gain;
		script = new ScriptC_quad_demod(rs, AndroidDSPLib.getResources(), R.raw.quad_demod);
		script.set_gain(gain);
		script.set_historyRe(0);
		script.set_historyIm(0);
	}

	public float getGain() {
		return gain;
	}

	/**
	 * Demodulates the complex samples from the input sample packet to the real component of the output
	 * sample packet. Stops automatically if output sample packet is full.
	 * @param in		input sample packet
	 * @param out		output sample packet
	 * @param offset	offset to use as start index for the input packet
	 * @param length	max number of output samples
	 * @return number of samples written to the output sample packet
	 */
	public int demodulate(SamplePacket in, SamplePacket out, int offset, int length) {
		int outSize = out.size();
		int outputLength = Math.min(outSize + length, out.capacity());
		script.set_offsetIn(offset);
		script.set_offsetOut(outSize);
		script.set_len(outputLength);
		script.set_inReal(in.getReAlloc());
		script.set_inImag(in.getImAlloc());
		script.set_outReal(out.getReAlloc());
		script.forEach_demod(out.getReAlloc());
		out.setSize(outputLength);
		out.setSampleRate(in.getSampleRate());
		script.invoke_saveHistory();
		return (outputLength-outSize);
	}


}
