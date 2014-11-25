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
}