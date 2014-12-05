package com.mantz_it.android_dsp_lib;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;

/**
 * Android DSP library - FIR Filter
 *
 * Module:      FirFilter.java
 * Description: This class implements a FIR filter with real taps
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
public class FirFilter {
	private RenderScript rs;
	private ScriptC_fir_filter script;
	private float[] tapsReal;
	private float[] tapsImag;
	private Allocation tapsRealAlloc;
	private Allocation tapsImagAlloc;
	private Allocation remainderRealAlloc;
	private Allocation remainderImagAlloc;
	private int decimation;

	public FirFilter(float[][] taps, int decimation) {
		this(taps[0], taps[1], decimation);
	}

	public FirFilter(float[] tapsReal, float[] tapsImag, int decimation) {
		if(tapsReal == null)
			throw new NullPointerException("real taps cannot be null!");

		this.rs = AndroidDSPLib.getRenderScript();
		this.tapsReal = tapsReal;
		this.tapsImag = tapsImag;
		this.decimation = decimation;
		this.tapsRealAlloc = Allocation.createSized(rs, Element.F32(rs), tapsReal.length);
		this.tapsRealAlloc.copyFrom(tapsReal);
		if(tapsImag != null) {
			if(tapsReal.length != tapsImag.length)
				throw new IllegalArgumentException("real taps and imaginary taps have to be of the same length!");
			this.tapsImagAlloc = Allocation.createSized(rs, Element.F32(rs), tapsImag.length);
			this.tapsImagAlloc.copyFrom(tapsImag);
		}
		this.remainderRealAlloc = Allocation.createSized(rs, Element.F32(rs), tapsReal.length - 1);
		this.remainderImagAlloc = Allocation.createSized(rs, Element.F32(rs), tapsReal.length - 1);

		script = new ScriptC_fir_filter(rs, AndroidDSPLib.getResources(), R.raw.fir_filter);
		script.set_filterOrder(tapsReal.length);
		script.set_decimation(decimation);
		script.bind_tapsReal(tapsRealAlloc);
		if(tapsImag != null)
			script.bind_tapsImag(tapsImagAlloc);
		script.bind_remainderReal(remainderRealAlloc);
		script.bind_remainderImag(remainderImagAlloc);
		script.invoke_clearRemainders();
	}

	public int getDecimation() {
		return decimation;
	}

	public int getNumberOfTaps() {
		return tapsReal.length;
	}

	/**
	 * Filters the complex samples from the input sample packet with real taps and appends filter output to the output
	 * sample packet. Stops automatically if output sample packet is full.
	 * @param in		input sample packet
	 * @param out		output sample packet
	 * @param offset	offset to use as start index for the input packet
	 * @param length	max number of samples processed from the input packet (must be multiple of decimation)
	 * @return number of samples consumed from the input packet
	 */
	public int filterComplexSignal(SamplePacket in, SamplePacket out, int offset, int length) {
		int outSize = out.size();
		int outputLength = Math.min(outSize + (length / decimation), out.capacity());
		script.set_offsetIn(offset);
		script.set_offsetOut(outSize);
		script.set_len(outputLength);
		script.set_inReal(in.getReAlloc());
		script.set_inImag(in.getImAlloc());
		script.set_outReal(out.getReAlloc());
		script.set_outImag(out.getImAlloc());
		script.forEach_filterAndDecimate(out.getReAlloc());
		out.setSize(outputLength);
		out.setSampleRate(in.getSampleRate()/decimation);
		out.getReAlloc().syncAll(Allocation.USAGE_SCRIPT);
		script.invoke_updateRemainders();
		return (outputLength-outSize) * decimation;
	}

	/**
	 * Filters the real samples from the input sample packet with real taps and appends filter output to the output
	 * sample packet. Stops automatically if output sample packet is full.
	 * @param in		input sample packet
	 * @param out		output sample packet
	 * @param offset	offset to use as start index for the input packet
	 * @param length	max number of samples processed from the input packet (must be multiple of decimation)
	 * @return number of samples consumed from the input packet
	 */
	public int filterRealSignal(SamplePacket in, SamplePacket out, int offset, int length) {
		int outSize = out.size();
		int outputLength = Math.min(outSize + (length / decimation), out.capacity());
		script.set_offsetIn(offset);
		script.set_offsetOut(outSize);
		script.set_len(outputLength);
		script.set_inReal(in.getReAlloc());
		script.set_outReal(out.getReAlloc());
		script.forEach_filterRealSignalAndDecimate(out.getReAlloc());
		out.setSize(outputLength);
		out.setSampleRate(in.getSampleRate()/decimation);
		out.getReAlloc().syncAll(Allocation.USAGE_SCRIPT);
		script.invoke_updateRealRemainders();
		return (outputLength-outSize) * decimation;
	}

	/**
	 * Filters the complex samples from the input sample packet with complex taps and appends filter output to the output
	 * sample packet. Stops automatically if output sample packet is full.
	 * @param in		input sample packet
	 * @param out		output sample packet
	 * @param offset	offset to use as start index for the input packet
	 * @param length	max number of samples processed from the input packet (must be multiple of decimation)
	 * @return number of samples consumed from the input packet
	 */
	public int filterComplexTaps(SamplePacket in, SamplePacket out, int offset, int length) {
		int outSize = out.size();
		int outputLength = Math.min(outSize + (length / decimation), out.capacity());
		script.set_offsetIn(offset);
		script.set_offsetOut(outSize);
		script.set_len(outputLength);
		script.set_inReal(in.getReAlloc());
		script.set_inImag(in.getImAlloc());
		script.set_outReal(out.getReAlloc());
		script.set_outImag(out.getImAlloc());
		script.forEach_filterComplexTapsAndDecimate(out.getReAlloc());
		out.setSize(outputLength);
		out.setSampleRate(in.getSampleRate()/decimation);
		out.getReAlloc().syncAll(Allocation.USAGE_SCRIPT);
		script.invoke_updateRemainders();
		return (outputLength-outSize) * decimation;
	}
}
