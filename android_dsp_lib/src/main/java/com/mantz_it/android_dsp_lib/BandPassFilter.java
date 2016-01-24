package com.mantz_it.android_dsp_lib;

/**
 * Android DSP library - Band Pass Filter
 *
 * Module:      BandPassFilter.java
 * Description: This class extends the FirFilter class to create a convenient band pass filter with real taps
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
public class BandPassFilter extends FirFilter {
	private float gain;
	private float sampleRate;
	private float lowCutOffFrequency;
	private float highCutOffFrequency;
	private float transitionWidth;
	private float attenuation;

	/**
	 * Constructor. Creates a new FIR Filter with the given parameters and decimation.
	 * @param decimation			decimation factor
	 * @param gain					filter pass band gain
	 * @param sampleRate			sample rate
	 * @param lowCutOffFrequency	lower cut off frequency (start of pass band)
	 * @param highCutOffFrequency	upper cut off frequency (end of pass band)
	 * @param transitionWidth		width from end of pass band to start stop band
	 * @param attenuation			attenuation of stop band
	 */
	public BandPassFilter(int decimation, float gain, float sampleRate, float lowCutOffFrequency,
								 float highCutOffFrequency, float transitionWidth, float attenuation) {
		super(designBandPassFilter(gain, sampleRate, lowCutOffFrequency, highCutOffFrequency, transitionWidth, attenuation), null, decimation);
		this.gain = gain;
		this.sampleRate = sampleRate;
		this.lowCutOffFrequency = lowCutOffFrequency;
		this.highCutOffFrequency = highCutOffFrequency;
		this.transitionWidth = transitionWidth;
		this.attenuation = attenuation;
	}

	public float getGain() {
		return gain;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	public float getLowCutOffFrequency() {
		return lowCutOffFrequency;
	}

	public float getHighCutOffFrequency() {
		return highCutOffFrequency;
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
		return super.filterRealSignal(in, out, offset, length);
	}

	/**
	 * FROM GNU Radio firdes::band_pass_2:
	 * Will calculate the tabs for the specified band pass filter
	 *
	 * @param gain					filter pass band gain
	 * @param sampleRate			sample rate
	 * @param lowCutOffFrequency	cut off frequency (beginning of pass band)
	 * @param highCutOffFrequency	cut off frequency (end of pass band)
	 * @param transitionWidth		width from end of pass band to start stop band
	 * @param attenuation			attenuation of stop band
	 * @return float[] array containing the filter taps
	 */
	public static float[] designBandPassFilter(float gain,
														float sampleRate,    // Hz
														float lowCutOffFrequency,      // Hz BEGINNING of transition band
														float highCutOffFrequency,      // Hz END of transition band
														float transitionWidth, // Hz width of transition band
														float attenuation)   // attenuation dB
	{
		if (sampleRate <= 0.0) {
			throw new IllegalArgumentException("firdes check failed: sampling_freq > 0");
		}

		if (lowCutOffFrequency <= 0 || lowCutOffFrequency > sampleRate * 0.5) {
			throw new IllegalArgumentException("firdes check failed: 0 < lowCutOffFrequency <= sampling_freq / 2");
		}

		if (highCutOffFrequency <= 0 || highCutOffFrequency > sampleRate * 0.5) {
			throw new IllegalArgumentException("firdes check failed: 0 < highCutOffFrequency <= sampling_freq / 2");
		}

		if (lowCutOffFrequency >= highCutOffFrequency) {
			throw new IllegalArgumentException("firdes check failed: low_cutoff_freq >= high_cutoff_freq");
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


		float[] taps = new float[ntaps];
		float[] w = WindowFunctions.makeBlackmanWindow(ntaps);

		int M = (ntaps - 1) / 2;
		float fwT0 = 2 * (float)Math.PI * lowCutOffFrequency / sampleRate;
		float fwT1 = 2 * (float)Math.PI * highCutOffFrequency / sampleRate;
		for (int n = -M; n <= M; n++) {
			if (n == 0)
				taps[n + M] = (fwT1 - fwT0) / (float)Math.PI * w[n + M];
			else {
				taps[n + M] = taps[n + M] =  ((float)Math.sin(n * fwT1) - (float)Math.sin(n * fwT0)) / (n * (float)Math.PI) * w[n + M];
			}
		}

		// find the factor to normalize the gain, fmax.
		// For band-pass, gain @ zero freq = 1.0
		float fmax = taps[0 + M];
		for (int n = 1; n <= M; n++)
			fmax += 2 * taps[n + M] * (float)Math.cos(n * (fwT0 + fwT1) * 0.5);
		float actualGain = gain/fmax;    // normalize
		for (int i = 0; i < ntaps; i++)
			taps[i] *= actualGain;

		return taps;
	}

}
