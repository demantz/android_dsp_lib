package com.mantz_it.android_dsp_lib;

/**
 * Android DSP library - Complex Band Pass Filter
 *
 * Module:      ComplexBandPassFilter.java
 * Description: This class extends the FirFilter class to create a convenient band pass filter with complex taps
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
public class ComplexBandPassFilter extends FirFilter {
	private float gain;
	private float sampleRate;
	private float lowCutOffFrequency;
	private float highCutOffFrequency;
	private float transitionWidth;
	private float attenuation;

	/**
	 * Constructor. Creates a new complex FIR Filter with the given parameters and decimation.
	 * @param decimation			decimation factor
	 * @param gain					filter pass band gain
	 * @param sampleRate			sample rate
	 * @param lowCutOffFrequency	lower cut off frequency (start of pass band)
	 * @param highCutOffFrequency	upper cut off frequency (end of pass band)
	 * @param transitionWidth		width from end of pass band to start stop band
	 * @param attenuation			attenuation of stop band
	 */
	public ComplexBandPassFilter(int decimation, float gain, float sampleRate, float lowCutOffFrequency,
							 float highCutOffFrequency, float transitionWidth, float attenuation) {
		super(designComplexBandPassFilter(gain, sampleRate, lowCutOffFrequency, highCutOffFrequency, transitionWidth, attenuation), decimation);
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
		return super.filterComplexTaps(in,out,offset,length);
	}

	/**
	 * FROM GNU Radio firdes::band_pass_2:
	 * Will calculate the tabs for the specified complex band pass filter
	 *
	 * @param gain					filter pass band gain
	 * @param sampleRate			sample rate
	 * @param lowCutOffFrequency	cut off frequency (beginning of pass band)
	 * @param highCutOffFrequency	cut off frequency (end of pass band)
	 * @param transitionWidth		width from end of pass band to start stop band
	 * @param attenuation			attenuation of stop band
	 * @return float[][] array containing the filter taps: ret[0] are the real taps, ret[1] the imaginary taps
	 */
	public static float[][] designComplexBandPassFilter(float gain,
														float sampleRate,    // Hz
														float lowCutOffFrequency,      // Hz BEGINNING of transition band
														float highCutOffFrequency,      // Hz END of transition band
														float transitionWidth, // Hz width of transition band
														float attenuation)   // attenuation dB
	{
		if (sampleRate <= 0.0) {
			throw new IllegalArgumentException("firdes check failed: sampling_freq > 0");
		}

		if (lowCutOffFrequency < sampleRate * -0.5 || highCutOffFrequency > sampleRate * 0.5) {
			throw new IllegalArgumentException("firdes check failed: -sampling_freq / 2 < fa <= sampling_freq / 2");
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

		// construct the truncated ideal impulse response
		// [sin(x)/x for the low pass case]
		// Note: we calculate the real taps for a low pass and shift them
		float low_pass_cut_off = (highCutOffFrequency - lowCutOffFrequency)/2f;
		float[] tapsLowPass = new float[ntaps];
		float[] w = WindowFunctions.makeBlackmanWindow(ntaps);

		int M = (ntaps - 1) / 2;
		float fwT0 = 2 * (float)Math.PI * low_pass_cut_off / sampleRate;
		for (int n = -M; n <= M; n++) {
			if (n == 0)
				tapsLowPass[n + M] = fwT0 / (float)Math.PI * w[n + M];
			else {
				// a little algebra gets this into the more familiar sin(x)/x form
				tapsLowPass[n + M] = (float)Math.sin(n * fwT0) / (n * (float)Math.PI) * w[n + M];
			}
		}

		// find the factor to normalize the gain, fmax.
		// For low-pass, gain @ zero freq = 1.0
		float fmax = tapsLowPass[0 + M];
		for (int n = 1; n <= M; n++)
			fmax += 2 * tapsLowPass[n + M];
		float actualGain = gain/fmax;    // normalize
		for (int i = 0; i < ntaps; i++)
			tapsLowPass[i] *= actualGain;

		// calc the band pass taps:
		float[][] taps = new float[2][ntaps];
		float freq = (float)Math.PI * (highCutOffFrequency + lowCutOffFrequency)/sampleRate;
		float phase = - freq * ( ntaps/2 );

		for(int i = 0; i < ntaps; i++) {
			taps[0][i] = tapsLowPass[i] * (float)Math.cos(phase);
			taps[1][i] = tapsLowPass[i] * (float)Math.sin(phase);
			phase += freq;
		}

		return taps;
	}

}
