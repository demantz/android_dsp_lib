package com.mantz_it.androiddsplibbenchmark;

import android.content.Context;

import com.mantz_it.android_dsp_lib.AndroidDSPLib;
import com.mantz_it.android_dsp_lib.FirFilter;
import com.mantz_it.android_dsp_lib.IQConverter;
import com.mantz_it.android_dsp_lib.LowPassFilter;
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
	private String csvValues = null;
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

	public String getCsvValues() {
		return csvValues;
	}

	public void run() {
		callback.println("Benchmark started. Will take about 2 minutes.");
		callback.println("Packet size: " + PACKETSIZE + " samples (IQ)\n");
		int rounds;
		int threads = 4;
		// Time variables:
		long millisFillPacketIntoSamplePacket8BitSigned;
		long millisFillPacketIntoSamplePacket8BitSigned_legacy;
		long millisMixPacketIntoSamplePacket8BitSigned;
		long millisMixPacketIntoSamplePacket8BitSigned_legacy;
		long millisLowPassFilter;
		long millisLowPassFilter_legacy;
		long millisDecimatingLowPassFilter;
		long millisDecimatingLowPassFilter_legacy;
		long millisLowPassFilterThreaded;
		long millisLowPassFilterThreaded_legacy;
		long millisLowPassFilter9Taps;
		long millisLowPassFilter9Taps_legacy;

		// IQConverter: lookup
		rounds = 10000;
		callback.println("Measure '8-bit signed lookup table' ("+rounds+" rounds)");
		callback.print("DSP lib ...");
		millisFillPacketIntoSamplePacket8BitSigned = measureFillPacketIntoSamplePacket8BitSigned(rounds);
		callback.println("\t: " + millisFillPacketIntoSamplePacket8BitSigned + " ms ("
				+ rounds*PACKETSIZE*1000l/millisFillPacketIntoSamplePacket8BitSigned + " Sps)");
		callback.print("Legacy ...");
		millisFillPacketIntoSamplePacket8BitSigned_legacy = measureFillPacketIntoSamplePacket8BitSigned_legacy(rounds);
		callback.println("\t: " + millisFillPacketIntoSamplePacket8BitSigned_legacy + " ms ("
				+ rounds*PACKETSIZE*1000l/millisFillPacketIntoSamplePacket8BitSigned_legacy + " Sps)");
		if(stopRequested) {
			callback.println("aborted!\n");
			callback.onFinish(false);
			return;
		} else {
			callback.println(String.format("Performance gain is %d%%\n", (int)(100 * (1f - (float)millisFillPacketIntoSamplePacket8BitSigned/millisFillPacketIntoSamplePacket8BitSigned_legacy))));
		}

		// IQConverter: mix
		rounds = 10000;
		callback.println("Measure '8-bit signed mixing' ("+rounds+" rounds)");
		callback.print("DSP lib ...");
		millisMixPacketIntoSamplePacket8BitSigned = measureMixPacketIntoSamplePacket8BitSigned(rounds);
		callback.println("\t: " + millisMixPacketIntoSamplePacket8BitSigned + " ms ("
				+ rounds*PACKETSIZE*1000l/millisMixPacketIntoSamplePacket8BitSigned + " Sps)");
		callback.print("Legacy ...");
		millisMixPacketIntoSamplePacket8BitSigned_legacy = measureMixPacketIntoSamplePacket8BitSigned_legacy(rounds);
		callback.println("\t: " + millisMixPacketIntoSamplePacket8BitSigned_legacy + " ms ("
				+ rounds*PACKETSIZE*1000l/millisMixPacketIntoSamplePacket8BitSigned_legacy + " Sps)");
		if(stopRequested) {
			callback.println("aborted!\n");
			callback.onFinish(false);
			return;
		}else {
			callback.println(String.format("Performance gain is %d%%\n", (int)(100 * (1f - (float)millisMixPacketIntoSamplePacket8BitSigned/millisMixPacketIntoSamplePacket8BitSigned_legacy))));
		}

		// LowPassFilter
		rounds = 500;
		callback.println("Measure 'LowPassFilter' ("+rounds+" rounds)");
		callback.print("DSP lib ... ");
		millisLowPassFilter = measureDecimatingLowPassFilter(rounds, 1);
		callback.println("\t: " + millisLowPassFilter + " ms ("
				+ rounds*PACKETSIZE*1000l/millisLowPassFilter + " Sps)");
		callback.print("Legacy ... ");
		millisLowPassFilter_legacy = measureDecimatingLowPassFilter_legacy(rounds, 1);
		callback.println("\t: " + millisLowPassFilter_legacy + " ms ("
				+ rounds*PACKETSIZE*1000l/millisLowPassFilter_legacy + " Sps)");
		if(stopRequested) {
			callback.println("aborted!\n");
			callback.onFinish(false);
			return;
		} else {
			callback.println(String.format("Performance gain is %d%%\n", (int)(100 * (1f - (float)millisLowPassFilter/millisLowPassFilter_legacy))));
		}

		// LowPassFilter (decimating by 4)
		rounds = 500;
		callback.println("Measure 'LowPassFilter' ("+rounds+" rounds)");
		callback.print("DSP lib ... ");
		millisDecimatingLowPassFilter = measureDecimatingLowPassFilter(rounds, 4);
		callback.println("\t: " + millisDecimatingLowPassFilter + " ms ("
				+ rounds*PACKETSIZE*1000l/millisDecimatingLowPassFilter + " Sps)");
		callback.print("Legacy ... ");
		millisDecimatingLowPassFilter_legacy = measureDecimatingLowPassFilter_legacy(rounds, 4);
		callback.println("\t: " + millisDecimatingLowPassFilter_legacy + " ms ("
				+ rounds*PACKETSIZE*1000l/millisDecimatingLowPassFilter_legacy + " Sps)");
		if(stopRequested) {
			callback.println("aborted!\n");
			callback.onFinish(false);
			return;
		} else {
			callback.println(String.format("Performance gain is %d%%\n", (int)(100 * (1f - (float)millisDecimatingLowPassFilter/millisDecimatingLowPassFilter_legacy))));
		}

		// LowPassFilter Threaded
		rounds = 500 / threads;
		callback.println("Measure 'LowPassFilter' (" + threads + " parallel threads) ("+rounds+" rounds)");
		callback.print("DSP lib ... ");
		millisLowPassFilterThreaded = measureLowPassFilterThreaded(rounds, threads);
		callback.println("\t: " + millisLowPassFilterThreaded + " ms ("
				+ rounds*PACKETSIZE*1000l/millisLowPassFilterThreaded + " Sps)");
		callback.print("Legacy ... ");
		millisLowPassFilterThreaded_legacy = measureLowPassFilterThreaded_legacy(rounds, threads);
		callback.println("\t\t: " + millisLowPassFilterThreaded_legacy + " ms ("
				+ rounds*PACKETSIZE*1000l/millisLowPassFilterThreaded_legacy + " Sps)");
		if(stopRequested) {
			callback.println("aborted!\n");
			callback.onFinish(false);
			return;
		} else {
			callback.println(String.format("Performance gain is %d%%\n", (int)(100 * (1f - (float)millisLowPassFilterThreaded/millisLowPassFilterThreaded_legacy))));
		}

		// LowPassFilter with 9 taps
		rounds = 500;
		callback.println("Measure 'LowPassFilter' ("+rounds+" rounds)");
		callback.print("DSP lib ... ");
		millisLowPassFilter9Taps = measureLowPassFilter9Taps(rounds);
		callback.println("\t: " + millisLowPassFilter9Taps + " ms ("
				+ rounds*PACKETSIZE*1000l/millisLowPassFilter9Taps + " Sps)");
		callback.print("Legacy ... ");
		millisLowPassFilter9Taps_legacy = measureLowPassFilter9Taps_legacy(rounds);
		callback.println("\t: " + millisLowPassFilter9Taps_legacy + " ms ("
				+ rounds*PACKETSIZE*1000l/millisLowPassFilter9Taps_legacy + " Sps)");
		if(stopRequested) {
			callback.println("aborted!\n");
			callback.onFinish(false);
			return;
		} else {
			callback.println(String.format("Performance gain is %d%%\n", (int)(100 * (1f - (float)millisLowPassFilter9Taps/millisLowPassFilter9Taps_legacy))));
		}

		// prepare the csv string:
		csvValues = String.format("%d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d",
				millisFillPacketIntoSamplePacket8BitSigned,
				millisFillPacketIntoSamplePacket8BitSigned_legacy,
				millisMixPacketIntoSamplePacket8BitSigned,
				millisMixPacketIntoSamplePacket8BitSigned_legacy,
				millisLowPassFilter,
				millisLowPassFilter_legacy,
				millisDecimatingLowPassFilter,
				millisDecimatingLowPassFilter_legacy,
				millisLowPassFilterThreaded,
				millisLowPassFilterThreaded_legacy,
				millisLowPassFilter9Taps,
				millisLowPassFilter9Taps_legacy);

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

	public long measureMixPacketIntoSamplePacket8BitSigned(int rounds) {
		IQConverter iqConverter8BitSigned = new IQConverter(IQConverter.FORMAT_8BIT_SIGNED, 2 * PACKETSIZE);
		iqConverter8BitSigned.setFrequency(97000000);
		iqConverter8BitSigned.setSampleRate(1000000);
		SamplePacket samplePacket = new SamplePacket(PACKETSIZE);
		byte[] data = new byte[2*PACKETSIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rounds && !stopRequested; i++) {
			iqConverter8BitSigned.mixPacketIntoSamplePacket_8BitSigned(data, samplePacket, 96900000);
			samplePacket.sync();
			samplePacket.setSize(0);
		}
		return System.currentTimeMillis() - startTime;
	}

	public long measureMixPacketIntoSamplePacket8BitSigned_legacy(int rounds) {
		com.mantz_it.androiddsplibbenchmark.legacyClasses.IQConverter iqConverter8BitSigned =
				new com.mantz_it.androiddsplibbenchmark.legacyClasses.Signed8BitIQConverter();
		iqConverter8BitSigned.setFrequency(97000000);
		iqConverter8BitSigned.setSampleRate(1000000);
		com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket samplePacket =
				new com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket(PACKETSIZE);
		byte[] data = new byte[2*PACKETSIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rounds && !stopRequested; i++) {
			iqConverter8BitSigned.mixPacketIntoSamplePacket(data, samplePacket, 96900000);
			samplePacket.setSize(0);
		}
		return System.currentTimeMillis() - startTime;
	}

	public long measureDecimatingLowPassFilter(int rounds, int decimation) {
		LowPassFilter lowPassFilter = new LowPassFilter(decimation, 1, 1000000, 100000, 10000, 40);
		callback.print("("+lowPassFilter.getNumberOfTaps()+" taps; decimate by " + decimation+") ");
		float[] data = new float[PACKETSIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (float) i;
		}
		SamplePacket in = new SamplePacket(data,data,0,1000000);
		SamplePacket out = new SamplePacket(PACKETSIZE);

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rounds && !stopRequested; i++) {
			lowPassFilter.filter(in, out, 0, in.size());
			out.setSize(0);
		}
		return System.currentTimeMillis() - startTime;
	}

	public long measureDecimatingLowPassFilter_legacy(int rounds, int decimation) {
		com.mantz_it.androiddsplibbenchmark.legacyClasses.FirFilter lowPassFilter =
				com.mantz_it.androiddsplibbenchmark.legacyClasses.FirFilter.createLowPass(decimation, 1, 1000000, 100000, 10000, 40);
		callback.print("("+lowPassFilter.getNumberOfTaps()+" taps; decimate by " + decimation+") ");
		float[] data = new float[PACKETSIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (float) i;
		}
		com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket in =
				new com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket(data,data,0,1000000);
		com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket out =
				new com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket(PACKETSIZE);

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rounds && !stopRequested; i++) {
			lowPassFilter.filter(in, out, 0, in.size());
			out.setSize(0);
		}
		return System.currentTimeMillis() - startTime;
	}

	public long measureLowPassFilterThreaded(final int rounds, int threads) {
		LowPassFilter[] lowPassFilter = new LowPassFilter[threads];
		for (int i = 0; i < threads; i++)
			lowPassFilter[i] = new LowPassFilter(1, 1, 1000000, 100000, 10000, 40);
		callback.print("(" + lowPassFilter[0].getNumberOfTaps() + " taps) ");
		final float[] data = new float[PACKETSIZE];
		for (int i = 0; i < data.length; i++)
			data[i] = (float) i;

		class WorkerThread extends Thread {
			LowPassFilter lowPassFilter;
			public WorkerThread(LowPassFilter filter) {
				this.lowPassFilter = filter;
			}
			public void run() {
				SamplePacket in = new SamplePacket(data, data, 0, 1000000);
				SamplePacket out = new SamplePacket(PACKETSIZE);
				for (int i = 0; i < rounds && !stopRequested; i++) {
					lowPassFilter.filter(in, out, 0, in.size());
					out.setSize(0);
				}
			}
		}

		WorkerThread[] workerThreads = new WorkerThread[threads];
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < threads; i++) {
			workerThreads[i] = new WorkerThread(lowPassFilter[i]);
			workerThreads[i].start();
		}
		for (int i = 0; i < threads; i++) {
			try {
				workerThreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return System.currentTimeMillis() - startTime;
	}

	public long measureLowPassFilterThreaded_legacy(final int rounds, int threads) {
		com.mantz_it.androiddsplibbenchmark.legacyClasses.FirFilter[] lowPassFilter =
				new com.mantz_it.androiddsplibbenchmark.legacyClasses.FirFilter[threads];
		for (int i = 0; i < threads; i++)
			lowPassFilter[i] = com.mantz_it.androiddsplibbenchmark.legacyClasses.FirFilter.createLowPass(1, 1, 1000000, 100000, 10000, 40);
		callback.print("(" + lowPassFilter[0].getNumberOfTaps() + " taps) ");
		final float[] data = new float[PACKETSIZE];
		for (int i = 0; i < data.length; i++)
			data[i] = (float) i;

		class WorkerThread extends Thread {
			com.mantz_it.androiddsplibbenchmark.legacyClasses.FirFilter lowPassFilter;
			public WorkerThread(com.mantz_it.androiddsplibbenchmark.legacyClasses.FirFilter filter) {
				this.lowPassFilter = filter;
			}
			public void run() {
				com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket in =
						new com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket(data,data,0,1000000);
				com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket out =
						new com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket(PACKETSIZE);
				for (int i = 0; i < rounds && !stopRequested; i++) {
					lowPassFilter.filter(in, out, 0, in.size());
					out.setSize(0);
				}
			}
		}

		WorkerThread[] workerThreads = new WorkerThread[threads];
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < threads; i++) {
			workerThreads[i] = new WorkerThread(lowPassFilter[i]);
			workerThreads[i].start();
		}
		for (int i = 0; i < threads; i++) {
			try {
				workerThreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return System.currentTimeMillis() - startTime;
	}

	public long measureLowPassFilter9Taps(int rounds) {
		LowPassFilter lowPassFilter = new LowPassFilter(1, 1, 1000000, 100000, 100000, 20);
		callback.print("("+lowPassFilter.getNumberOfTaps()+" taps) ");
		float[] data = new float[PACKETSIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (float) i;
		}
		SamplePacket in = new SamplePacket(data,data,0,1000000);
		SamplePacket out = new SamplePacket(PACKETSIZE);

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rounds && !stopRequested; i++) {
			lowPassFilter.filter(in, out, 0, in.size());
			out.setSize(0);
		}
		return System.currentTimeMillis() - startTime;
	}

	public long measureLowPassFilter9Taps_legacy(int rounds) {
		com.mantz_it.androiddsplibbenchmark.legacyClasses.FirFilter lowPassFilter =
				com.mantz_it.androiddsplibbenchmark.legacyClasses.FirFilter.createLowPass(1, 1, 1000000, 100000, 100000, 20);
		callback.print("("+lowPassFilter.getNumberOfTaps()+" taps) ");
		float[] data = new float[PACKETSIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (float) i;
		}
		com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket in =
				new com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket(data,data,0,1000000);
		com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket out =
				new com.mantz_it.androiddsplibbenchmark.legacyClasses.SamplePacket(PACKETSIZE);

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rounds && !stopRequested; i++) {
			lowPassFilter.filter(in, out, 0, in.size());
			out.setSize(0);
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
