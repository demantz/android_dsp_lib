package com.mantz_it.android_dsp_lib;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
	public ApplicationTest() {
		super(Application.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		createApplication();
		AndroidDSPLib.init(getContext());
	}

	public void testFillPacketIntoSamplePacket8bitSigned() {
		int size = 512;
		IQConverter iqConverter = new IQConverter(IQConverter.FORMAT_8BIT_SIGNED, size);
		SamplePacket result = new SamplePacket(size/2);
		byte[] data = new byte[size];
		for (int i = 0; i < size-1; i+=2) {
			data[i] = (byte) (i/2-128);
			data[i+1] = data[i];
		}
		printArray(data);
		iqConverter.fillPacketIntoSamplePacket(data, result);
		System.out.println("8-bit signed; Real Result:");
		printArray(result.re());
		System.out.println("8-bit signed; Imag Result:");
		printArray(result.im());
	}

	public void testFillPacketIntoSamplePacket8bitSignedWithOffset() {
		int size = 512;
		IQConverter iqConverter = new IQConverter(IQConverter.FORMAT_8BIT_SIGNED, size);
		SamplePacket result = new SamplePacket(size/2 - 10);	// smaller output packet!
		result.setSize(10);		// start at offset
		byte[] data = new byte[size];
		for (int i = 0; i < size-1; i+=2) {
			data[i] = (byte) (i/2-128);
			data[i+1] = data[i];
		}
		printArray(data);
		iqConverter.fillPacketIntoSamplePacket(data, result);
		System.out.println("8-bit signed; Real Result:");
		printArray(result.re());
		System.out.println("8-bit signed; Imag Result:");
		printArray(result.im());
	}

	public void testFillPacketIntoSamplePacket8bitUnsigned() {
		int size = 512;
		IQConverter iqConverter = new IQConverter(IQConverter.FORMAT_8BIT_UNSIGNED, size);
		SamplePacket result = new SamplePacket(size/2);
		byte[] data = new byte[size];
		for (int i = 0; i < size-1; i+=2) {
			data[i] = (byte) (i/2);
			data[i+1] = data[i];
		}
		printUnsignedArray(data);
		iqConverter.fillPacketIntoSamplePacket(data, result);
		System.out.println("8-bit signed; Real Result:");
		printArray(result.re());
		System.out.println("8-bit signed; Imag Result:");
		printArray(result.im());
	}

	public void testFillPacketIntoSamplePacket8bitUnsignedWithOffset() {
		int size = 512;
		IQConverter iqConverter = new IQConverter(IQConverter.FORMAT_8BIT_UNSIGNED, size);
		SamplePacket result = new SamplePacket(size/2 - 10);	// smaller output packet!
		result.setSize(10);		// start at offset
		byte[] data = new byte[size];
		for (int i = 0; i < size-1; i+=2) {
			data[i] = (byte) (i/2);
			data[i+1] = data[i];
		}
		printUnsignedArray(data);
		iqConverter.fillPacketIntoSamplePacket(data, result);
		System.out.println("8-bit signed; Real Result:");
		printArray(result.re());
		System.out.println("8-bit signed; Imag Result:");
		printArray(result.im());
	}

	public void testMixPacketIntoSamplePacket8bitSigned() {
		int size = 10;
		IQConverter iqConverter = new IQConverter(IQConverter.FORMAT_8BIT_SIGNED, size);
		SamplePacket result = new SamplePacket(size/2);
		byte[] data = new byte[size];
		for (int i = 0; i < size; i++) {
			data[i] = (byte) (127);
		}
		iqConverter.setSampleRate(1000000);
		iqConverter.setFrequency(1000000);
		printArray(data);
		iqConverter.mixPacketIntoSamplePacket(data, result, 1250000);
		System.out.println("8-bit signed; Real Result [1]:");
		printArray(result.re());
		System.out.println("8-bit signed; Imag Result [1]:");
		printArray(result.im());
		result.setSize(0);
		iqConverter.mixPacketIntoSamplePacket(data, result, 1250000);
		System.out.println("8-bit signed; Real Result [2]:");
		printArray(result.re());
		System.out.println("8-bit signed; Imag Result [2]:");
		printArray(result.im());
	}

	public void testMixPacketIntoSamplePacket8bitSignedWithOffset() {
		int size = 10;
		IQConverter iqConverter = new IQConverter(IQConverter.FORMAT_8BIT_SIGNED, size);
		SamplePacket result = new SamplePacket(size);		// output packet is too big
		byte[] data = new byte[size];
		for (int i = 0; i < size; i++) {
			data[i] = (byte) (127);
		}
		iqConverter.setSampleRate(1000000);
		iqConverter.setFrequency(1000000);
		printArray(data);
		iqConverter.mixPacketIntoSamplePacket(data, result, 1250000);
		System.out.println("8-bit signed; Real Result [1]:");
		printArray(result.re());
		System.out.println("8-bit signed; Imag Result [1]:");
		printArray(result.im());
		iqConverter.mixPacketIntoSamplePacket(data, result, 1250000);
		System.out.println("8-bit signed; Real Result [2]:");
		printArray(result.re());
		System.out.println("8-bit signed; Imag Result [2]:");
		printArray(result.im());
	}

	public void testMixPacketIntoSamplePacket8bitUnsigned() {
		int size = 10;
		IQConverter iqConverter = new IQConverter(IQConverter.FORMAT_8BIT_UNSIGNED, size);
		SamplePacket result = new SamplePacket(size/2);
		byte[] data = new byte[size];
		for (int i = 0; i < size; i++) {
			data[i] = (byte) (255);
		}
		iqConverter.setSampleRate(1000000);
		iqConverter.setFrequency(1000000);
		printUnsignedArray(data);
		iqConverter.mixPacketIntoSamplePacket(data, result, 1250000);
		System.out.println("8-bit unsigned; Real Result [1]:");
		printArray(result.re());
		System.out.println("8-bit unsigned; Imag Result [1]:");
		printArray(result.im());
		result.setSize(0);
		iqConverter.mixPacketIntoSamplePacket(data, result, 1250000);
		System.out.println("8-bit unsigned; Real Result [2]:");
		printArray(result.re());
		System.out.println("8-bit unsigned; Imag Result [2]:");
		printArray(result.im());
	}

	public void testMixPacketIntoSamplePacket8bitUnsignedWithOffset() {
		int size = 10;
		IQConverter iqConverter = new IQConverter(IQConverter.FORMAT_8BIT_UNSIGNED, size);
		SamplePacket result = new SamplePacket(size);		// output packet is too big
		byte[] data = new byte[size];
		for (int i = 0; i < size; i++) {
			data[i] = (byte) (255);
		}
		iqConverter.setSampleRate(1000000);
		iqConverter.setFrequency(1000000);
		printUnsignedArray(data);
		iqConverter.mixPacketIntoSamplePacket(data, result, 1250000);
		System.out.println("8-bit unsigned; Real Result [1]:");
		printArray(result.re());
		System.out.println("8-bit unsigned; Imag Result [1]:");
		printArray(result.im());
		iqConverter.mixPacketIntoSamplePacket(data, result, 1250000);
		System.out.println("8-bit unsigned; Real Result [2]:");
		printArray(result.re());
		System.out.println("8-bit unsigned; Imag Result [2]:");
		printArray(result.im());
	}

	public void testFirFilter() {
		float[] expectedResultReal 	= {-0.100000f, -0.345000f, -0.827500f, -1.035000f, -1.080000f, -1.020000f, -0.960000f, -0.900000f, -0.840000f, -0.780000f, -0.720000f, -0.660000f, -0.600000f, -0.540000f, -0.480000f, -0.420000f, -0.360000f, -0.300000f, -0.240000f, -0.180000f, -0.120000f, -0.060000f, 0.000000f, 0.060000f, 0.120000f, 0.180000f, 0.240000f, 0.300000f, 0.360000f, 0.420000f, 0.480000f, 0.540000f, 0.600000f, 0.660000f, 0.720000f, 0.780000f, 0.840000f, 0.900000f, 0.960000f, 1.020000f, 1.080000f};
		float[] expectedResultImag 	= {0.100000f, 0.345000f, 0.827500f, 1.035000f, 1.080000f, 1.020000f, 0.960000f, 0.900000f, 0.840000f, 0.780000f, 0.720000f, 0.660000f, 0.600000f, 0.540000f, 0.480000f, 0.420000f, 0.360000f, 0.300000f, 0.240000f, 0.180000f, 0.120000f, 0.060000f, -0.000000f, -0.060000f, -0.120000f, -0.180000f, -0.240000f, -0.300000f, -0.360000f, -0.420000f, -0.480000f, -0.540000f, -0.600000f, -0.660000f, -0.720000f, -0.780000f, -0.840000f, -0.900000f, -0.960000f, -1.020000f, -1.080000f};
		float[] taps 				= {0.1f, 0.25f, 0.5f, 0.25f, 0.1f};
		float[] inputReal			= {-1.000000f, -0.950000f, -0.900000f, -0.850000f, -0.800000f, -0.750000f, -0.700000f, -0.650000f, -0.600000f, -0.550000f, -0.500000f, -0.450000f, -0.400000f, -0.350000f, -0.300000f, -0.250000f, -0.200000f, -0.150000f, -0.100000f, -0.050000f, 0.000000f, 0.050000f, 0.100000f, 0.150000f, 0.200000f, 0.250000f, 0.300000f, 0.350000f, 0.400000f, 0.450000f, 0.500000f, 0.550000f, 0.600000f, 0.650000f, 0.700000f, 0.750000f, 0.800000f, 0.850000f, 0.900000f, 0.950000f, 1.000000f};
		float[] inputImag			= {1.000000f, 0.950000f, 0.900000f, 0.850000f, 0.800000f, 0.750000f, 0.700000f, 0.650000f, 0.600000f, 0.550000f, 0.500000f, 0.450000f, 0.400000f, 0.350000f, 0.300000f, 0.250000f, 0.200000f, 0.150000f, 0.100000f, 0.050000f, 0.000000f, -0.050000f, -0.100000f, -0.150000f, -0.200000f, -0.250000f, -0.300000f, -0.350000f, -0.400000f, -0.450000f, -0.500000f, -0.550000f, -0.600000f, -0.650000f, -0.700000f, -0.750000f, -0.800000f, -0.850000f, -0.900000f, -0.950000f, -1.000000f};
		SamplePacket in = new SamplePacket(inputReal, inputImag, 0, 1000000);
		FirFilter firFilter = new FirFilter(taps, null, 1);

		// Filter the first 20 samples:
		SamplePacket out = new SamplePacket(inputReal, inputImag, 0, 0);
		out.setSize(0);
		firFilter.filterComplexSignal(in, out, 0, 20);
		assertEquals(20, out.size());
		assertEquals(in.getSampleRate(), out.getSampleRate());
		float[] resultReal = out.re();
		float[] resultImag = out.im();
		for(int i = 0; i < 20; i++) {
			assert(floatEquals(expectedResultReal[i], resultReal[i]));
			assert(floatEquals(expectedResultImag[i], resultImag[i]));
		}
		for(int i = 20; i < out.capacity(); i++) {
			assert(floatEquals(inputReal[i], resultReal[i]));
			assert(floatEquals(inputImag[i], resultImag[i]));
		}

		// Filter the next 21 samples:
		out = new SamplePacket(21);
		firFilter.filterComplexSignal(in, out, 20, in.size()-20);
		assertEquals(21, out.size());
		resultReal = out.re();
		resultImag = out.im();
		for(int i = 0; i < 21; i++) {
			assert(floatEquals(expectedResultReal[i+20], resultReal[i]));
			assert(floatEquals(expectedResultImag[i+20], resultImag[i]));
		}
	}

	public void testFirFilterWithRealSignals() {
		float[] expectedResultReal 	= {-0.100000f, -0.345000f, -0.827500f, -1.035000f, -1.080000f, -1.020000f, -0.960000f, -0.900000f, -0.840000f, -0.780000f, -0.720000f, -0.660000f, -0.600000f, -0.540000f, -0.480000f, -0.420000f, -0.360000f, -0.300000f, -0.240000f, -0.180000f, -0.120000f, -0.060000f, 0.000000f, 0.060000f, 0.120000f, 0.180000f, 0.240000f, 0.300000f, 0.360000f, 0.420000f, 0.480000f, 0.540000f, 0.600000f, 0.660000f, 0.720000f, 0.780000f, 0.840000f, 0.900000f, 0.960000f, 1.020000f, 1.080000f};
		float[] taps 				= {0.1f, 0.25f, 0.5f, 0.25f, 0.1f};
		float[] inputReal1			= {-1.000000f, -0.950000f, -0.900000f, -0.850000f, -0.800000f, -0.750000f, -0.700000f, -0.650000f, -0.600000f, -0.550000f, -0.500000f, -0.450000f, -0.400000f, -0.350000f, -0.300000f, -0.250000f, -0.200000f, -0.150000f, -0.100000f, -0.050000f};
		float[] inputReal2			= {0.000000f, 0.050000f, 0.100000f, 0.150000f, 0.200000f, 0.250000f, 0.300000f, 0.350000f, 0.400000f, 0.450000f, 0.500000f, 0.550000f, 0.600000f, 0.650000f, 0.700000f, 0.750000f, 0.800000f, 0.850000f, 0.900000f, 0.950000f, 1.000000f};
		FirFilter firFilter = new FirFilter(taps, null, 1);

		// Filter the first 20 samples:
		SamplePacket in = new SamplePacket(20);
		in.getReAlloc().copyFrom(inputReal1);
		in.setSize(20);
		SamplePacket out = new SamplePacket(41);
		out.setSize(0);
		firFilter.filterRealSignal(in, out, 0, in.size());
		assertEquals(20, out.size());
		float[] resultReal = out.re();
		for(int i = 0; i < 20; i++) {
			assert(floatEquals(expectedResultReal[i], resultReal[i]));
		}

		// Filter the next 20 samples:
		in.getReAlloc().copy1DRangeFrom(0, 20, inputReal2);
		firFilter.filterRealSignal(in, out, 0, in.size());
		assertEquals(40, out.size());
		resultReal = out.re();
		for(int i = 0; i < 20; i++) {
			assert(floatEquals(expectedResultReal[i+20], resultReal[i]));
		}
	}

	public void testFirFilterWithComplexTaps() {
		float[] expectedResultReal 	= {-0.190000f, -0.512500f, -1.055000f, -1.330000f, -1.445000f, -1.360000f, -1.275000f, -1.190000f, -1.105000f, -1.020000f, -0.935000f, -0.850000f, -0.765000f, -0.680000f, -0.595000f, -0.510000f, -0.425000f, -0.340000f, -0.255000f, -0.170000f, -0.085000f, 0.000000f, 0.085000f, 0.170000f, 0.255000f, 0.340000f, 0.425000f, 0.510000f, 0.595000f, 0.680000f, 0.765000f, 0.850000f, 0.935000f, 1.020000f, 1.105000f, 1.190000f, 1.275000f, 1.360000f, 1.445000f, 1.530000f, 1.615000f};
		float[] expectedResultImag 	= {0.000000f, 0.142500f, 0.515000f, 0.630000f, 0.595000f, 0.560000f, 0.525000f, 0.490000f, 0.455000f, 0.420000f, 0.385000f, 0.350000f, 0.315000f, 0.280000f, 0.245000f, 0.210000f, 0.175000f, 0.140000f, 0.105000f, 0.070000f, 0.035000f, -0.000000f, -0.035000f, -0.070000f, -0.105000f, -0.140000f, -0.175000f, -0.210000f, -0.245000f, -0.280000f, -0.315000f, -0.350000f, -0.385000f, -0.420000f, -0.455000f, -0.490000f, -0.525000f, -0.560000f, -0.595000f, -0.630000f, -0.665000f};
		float[] tapsReal			= {0.1f, 0.25f, 0.5f, 0.25f, 0.1f};
		float[] tapsImag			= {0.1f, 0.1f, 0.1f, 0.1f, 0.1f};
		float[] inputReal			= {-0.950000f, -0.900000f, -0.850000f, -0.800000f, -0.750000f, -0.700000f, -0.650000f, -0.600000f, -0.550000f, -0.500000f, -0.450000f, -0.400000f, -0.350000f, -0.300000f, -0.250000f, -0.200000f, -0.150000f, -0.100000f, -0.050000f, 0.000000f, 0.050000f, 0.100000f, 0.150000f, 0.200000f, 0.250000f, 0.300000f, 0.350000f, 0.400000f, 0.450000f, 0.500000f, 0.550000f, 0.600000f, 0.650000f, 0.700000f, 0.750000f, 0.800000f, 0.850000f, 0.900000f, 0.950000f, 1.000000f, 1.050000f};
		float[] inputImag			= {0.950000f, 0.900000f, 0.850000f, 0.800000f, 0.750000f, 0.700000f, 0.650000f, 0.600000f, 0.550000f, 0.500000f, 0.450000f, 0.400000f, 0.350000f, 0.300000f, 0.250000f, 0.200000f, 0.150000f, 0.100000f, 0.050000f, 0.000000f, -0.050000f, -0.100000f, -0.150000f, -0.200000f, -0.250000f, -0.300000f, -0.350000f, -0.400000f, -0.450000f, -0.500000f, -0.550000f, -0.600000f, -0.650000f, -0.700000f, -0.750000f, -0.800000f, -0.850000f, -0.900000f, -0.950000f, -1.000000f, -1.050000f};
		SamplePacket in = new SamplePacket(inputReal, inputImag, 0, 1000000);
		FirFilter firFilter = new FirFilter(tapsReal, tapsImag, 1);

		// Filter the first 20 samples:
		SamplePacket out = new SamplePacket(inputReal, inputImag, 0, 0);
		out.setSize(0);
		firFilter.filterComplexTaps(in, out, 0, 20);
		assertEquals(20, out.size());
		assertEquals(in.getSampleRate(), out.getSampleRate());
		float[] resultReal = out.re();
		float[] resultImag = out.im();
		for(int i = 0; i < 20; i++) {
			assert(floatEquals(expectedResultReal[i], resultReal[i]));
			assert(floatEquals(expectedResultImag[i], resultImag[i]));
		}
		for(int i = 20; i < out.capacity(); i++) {
			assert(floatEquals(inputReal[i], resultReal[i]));
			assert(floatEquals(inputImag[i], resultImag[i]));
		}

		// Filter the next 20 samples:
		out = new SamplePacket(20);
		firFilter.filterComplexTaps(in, out, 20, in.size()-20);
		assertEquals(20, out.size());
		resultReal = out.re();
		resultImag = out.im();
		for(int i = 0; i < 20; i++) {
			assert(floatEquals(expectedResultReal[i+20], resultReal[i]));
			assert(floatEquals(expectedResultImag[i+20], resultImag[i]));
		}
	}

	public void printArray(byte[] array) {
		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			System.out.print(" " + array[i]);
		}
		System.out.println("]");
	}

	public void printUnsignedArray(byte[] array) {
		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			System.out.print(" " + (array[i] & 0xff));
		}
		System.out.println("]");
	}

	public void printArray(float[] array) {
		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			System.out.print(" " + array[i]);
		}
		System.out.println("]");
	}

	public boolean floatEquals(float expected, float actual) {
		return actual < expected+0.0001 && actual > expected-0.0001;
	}
}