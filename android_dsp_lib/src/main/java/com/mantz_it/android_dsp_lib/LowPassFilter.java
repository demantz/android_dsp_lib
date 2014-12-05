package com.mantz_it.android_dsp_lib;

/**
 * Android DSP library - Low Pass Filter
 *
 * Module:      LowPassFilter.java
 * Description: This class extends the FirFilter class to create a convenient low pass filter
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
public class LowPassFilter extends FirFilter {
	private float gain;
	private float sampleRate;
	private float cutOffFrequency;
	private float transitionWidth;
	private float attenuation;

	/**
	 * Constructor. Creates a new FIR Filter with the given parameters and decimation.
	 * @param decimation			decimation factor
	 * @param gain					filter pass band gain
	 * @param sampleRate			sample rate
	 * @param cutOffFrequency		cut off frequency (end of pass band)
	 * @param transitionWidth		width from end of pass band to start stop band
	 * @param attenuation			attenuation of stop band
	 */
	public LowPassFilter(int decimation, float gain, float sampleRate, float cutOffFrequency, float transitionWidth, float attenuation) {
		super(designLowPassFilter(gain, sampleRate, cutOffFrequency, transitionWidth, attenuation), null, decimation);
		this.gain = gain;
		this.sampleRate = sampleRate;
		this.cutOffFrequency = cutOffFrequency;
		this.transitionWidth = transitionWidth;
		this.attenuation = attenuation;
	}

	public float getGain() {
		return gain;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	public float getCutOffFrequency() {
		return cutOffFrequency;
	}

	public float getTransitionWidth() {
		return transitionWidth;
	}

	public float getAttenuation() {
		return attenuation;
	}

	public int filter(SamplePacket in, SamplePacket out, int offset, int length) {
		return super.filterComplexSignal(in,out,offset,length);
	}

	public int filterReal(SamplePacket in, SamplePacket out, int offset, int length) {
		return super.filterRealSignal(in,out,offset,length);
	}

	/**
	 * FROM GNU Radio firdes::low_pass_2:
	 * Will calculate the tabs for the specified low pass filter
	 *
	 * @param gain					filter pass band gain
	 * @param sampleRate			sample rate
	 * @param cutOffFrequency		cut off frequency (end of pass band)
	 * @param transitionWidth		width from end of pass band to start stop band
	 * @param attenuation			attenuation of stop band
	 * @return filter taps
	 */
	public static float[] designLowPassFilter(float gain, float sampleRate, float cutOffFrequency, float transitionWidth, float attenuation) {
		if (sampleRate <= 0.0) {
			throw new IllegalArgumentException("firdes check failed: sampling_freq > 0");
		}

		if (cutOffFrequency <= 0.0 || cutOffFrequency > sampleRate / 2) {
			throw new IllegalArgumentException("firdes check failed: 0 < fa <= sampling_freq / 2");
		}

		if (transitionWidth <= 0) {
			throw new IllegalArgumentException("firdes check failed: transition_width > 0");
		}

		// Calculate number of tabs
		// Based on formula from Multirate Signal Processing for
		// Communications Systems, fredric j harris
		int ntaps = (int)(attenuation*sampleRate/(22.0*transitionWidth));
		if ((ntaps & 1) == 0)	// if even...
			ntaps++;		// ...make odd

		// construct the truncated ideal impulse response
		// [sin(x)/x for the low pass case]

		float[] taps = new float[ntaps];
		float[] w = WindowFunctions.makeBlackmanWindow(ntaps);

		int M = (ntaps - 1) / 2;
		float fwT0 = 2 * (float)Math.PI * cutOffFrequency / sampleRate;
		for (int n = -M; n <= M; n++) {
			if (n == 0)
				taps[n + M] = fwT0 / (float)Math.PI * w[n + M];
			else {
				// a little algebra gets this into the more familiar sin(x)/x form
				taps[n + M] = (float)Math.sin(n * fwT0) / (n * (float)Math.PI) * w[n + M];
			}
		}

		// find the factor to normalize the gain, fmax.
		// For low-pass, gain @ zero freq = 1.0

		float fmax = taps[0 + M];
		for (int n = 1; n <= M; n++)
			fmax += 2 * taps[n + M];

		float actualGain = gain/fmax;    // normalize

		for (int i = 0; i < ntaps; i++)
			taps[i] *= actualGain;

		return taps;
	}

}
