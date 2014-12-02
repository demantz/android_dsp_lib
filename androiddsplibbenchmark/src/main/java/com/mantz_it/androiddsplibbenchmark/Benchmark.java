package com.mantz_it.androiddsplibbenchmark;

import android.content.Context;

import com.mantz_it.android_dsp_lib.AndroidDSPLib;
import com.mantz_it.android_dsp_lib.IQConverter;
import com.mantz_it.android_dsp_lib.SamplePacket;

/**
 * Android DSP library - Benchmark
 *
 * Module:      Benchmark.java
 * Description: This class will run benchmarks on the DSP library and the legacy implementations
 *              from RF Analyzer and compare them. This will run in a separate Thread.
 *              Results will be stored in a cvs file.
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
public class Benchmark extends Thread {
	private BenchmarkCallback callback;
	private boolean stopRequested = true;
	private static final int PACKETSIZE = 8192;

	public Benchmark(BenchmarkCallback callback, Context context) {
		this.callback = callback;
		AndroidDSPLib.init(context);
	}

	public void startBenchmark() {
		stopRequested = false;
		this.start();
	}

	public void stopBenchmark() {
		stopRequested = true;
	}

	public boolean isRunning() {
		return !stopRequested;
	}

	public void run() {
		callback.println("Benchmark started...");
		// Time variables:
		long millisFillPacketIntoSamplePacket8BitSigned;
		long millisFillPacketIntoSamplePacket8BitSigned_legacy;

		// IQConverter
		int rounds = 10000;
		callback.print("Measure '8-bit signed lookup table' ...");
		millisFillPacketIntoSamplePacket8BitSigned = measureFillPacketIntoSamplePacket8BitSigned(rounds);
		callback.println("         : " + millisFillPacketIntoSamplePacket8BitSigned + " ms");
		callback.print("Measure '8-bit signed lookup table' (legacy) ...");
		millisFillPacketIntoSamplePacket8BitSigned_legacy = measureFillPacketIntoSamplePacket8BitSigned_legacy(rounds);
		callback.println(": " + millisFillPacketIntoSamplePacket8BitSigned_legacy + " ms");

		callback.println("Benchmark finished.");
		callback.onFinish(true);
	}

	public long measureFillPacketIntoSamplePacket8BitSigned(int rounds) {
		IQConverter iqConverter8BitSigned = new IQConverter(IQConverter.FORMAT_8BIT_SIGNED, 2 * PACKETSIZE);
		SamplePacket samplePacket = new SamplePacket(PACKETSIZE);
		byte[] data = new byte[2*PACKETSIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rounds && !stopRequested; i++) {
			iqConverter8BitSigned.fillPacketIntoSamplePacket_8BitSigned(data, samplePacket);
			samplePacket.sync();
			samplePacket.setSize(0);
		}
		return System.currentTimeMillis() - startTime;
	}

	public long measureFillPacketIntoSamplePacket8BitSigned_legacy(int rounds) {
		com.mantz_it.androiddsplibbenchmark.legacyClasses.IQConverter iqConverter8BitSigned =
				new com.mantz_it.androiddsplibbenchmark.legacyClasses.Signed8BitIQConverter();
		com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket samplePacket =
				new com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket(PACKETSIZE);
		byte[] data = new byte[2*PACKETSIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rounds && !stopRequested; i++) {
			iqConverter8BitSigned.fillPacketIntoSamplePacket(data, samplePacket);
			samplePacket.setSize(0);
		}
		return System.currentTimeMillis() - startTime;

	}

	public interface BenchmarkCallback {
		public void print(String msg);
		public void println(String msg);
		public void outputErr(String msg);
		public void onFinish(boolean success);
	}
}
