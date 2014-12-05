package com.mantz_it.android_dsp_lib;

/**
 * Android DSP library - Window Functions
 *
 * Module:      Window.java
 * Description: This class contains static methods to create windows (Blackman, ...). Implementations
 *              are copied from other sources (referenced in the methods)
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
public class WindowFunctions {

	/**
	 * Creates a Blackman Window.
	 * Code from https://www.ee.columbia.edu/~ronw/code/MEAPsoft/doc/html/FFT_8java-source.html
	 * All credit goes to the original authors!
	 *
	 * @param ntabs number of samples
	 * @return window samples
	 */
	public static float[] makeBlackmanWindow(int ntabs) {
		// Make a blackman window:
		// w(n)=0.42-0.5cos{(2*PI*n)/(N-1)}+0.08cos{(4*PI*n)/(N-1)};
		float[] window = new float[ntabs];
		for (int i = 0; i < window.length; i++)
			window[i] = 0.42f - 0.5f * (float)Math.cos(2 * Math.PI * i / (ntabs - 1))
					+ 0.08f * (float)Math.cos(4 * Math.PI * i / (ntabs - 1));
		return window;
	}
}
