package com.mantz_it.android_dsp_lib;

import android.util.Log;

/**
 * Android DSP library - IQ Converter
 *
 * Module:      IQConverter.java
 * Description: This class can fill raw interleaved IQ arrays into SamplePackets by using a lookup table.
 *              It also can perform down-mixing and convertion in one step
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
public class IQConverter {
	public static final int FORMAT_8BIT_SIGNED = 0;
	public static final int FORMAT_8BIT_UNSIGNED = 1;
	public static final int FORMAT_16BIT_SIGNED = 2;
	public static final int FORMAT_16BIT_UNSIGNED = 3;
	private static final String LOGTAG = "IQConverter";
	private int format;
	private int packetSize;
	private long frequency = 0;							// Baseband frequency of the converted samples (is put into the SamplePacket)
	private int sampleRate = 0;							// Sample rate of the converted samples (is put into the SamplePacket)
	private boolean mixerLookupTableInvalid = true;		// Indicates that the lookup table of the mixer has to be regenerated
	private static final int MAX_COSINE_LENGTH = 500;	// Max length of the mixer lookup table
	private Mixer_8Bit mixer8Bit;
	private LookupTable_8Bit lookupTable8Bit;

	public IQConverter(int format, int packetSize) {
		this.format = format;
		this.packetSize = packetSize;
		switch (format) {
			case FORMAT_8BIT_SIGNED:
				lookupTable8Bit = new LookupTable_8Bit(packetSize, LookupTable_8Bit.createSigned8BitLookupTable());
				mixer8Bit = new Mixer_8Bit(packetSize, MAX_COSINE_LENGTH);
				break;
			case FORMAT_8BIT_UNSIGNED:
				lookupTable8Bit = new LookupTable_8Bit(packetSize, LookupTable_8Bit.createUnsigned8BitLookupTable());
				mixer8Bit = new Mixer_8Bit(packetSize, MAX_COSINE_LENGTH);
				break;
			default:
				Log.e(LOGTAG, "fillPacketIntoSamplePacket: invalid format: " + format);
		}
	}

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		if(this.sampleRate != sampleRate) {
			this.sampleRate = sampleRate;
			this.mixerLookupTableInvalid = true;
		}
	}

	protected int calcOptimalCosineLength(int cosineFrequency) {
		// look for the best fitting array size to hold one or more full cosine cycles:
		double cycleLength = sampleRate / Math.abs((double)cosineFrequency);
		int bestLength = (int) cycleLength;
		double bestLengthError = Math.abs(bestLength-cycleLength);
		for (int i = 1; i*cycleLength < MAX_COSINE_LENGTH ; i++) {
			if(Math.abs(i*cycleLength - (int)(i*cycleLength)) < bestLengthError) {
				bestLength = (int)(i*cycleLength);
				bestLengthError = Math.abs(bestLength - (i*cycleLength));
			}
		}
		return bestLength;
	}

	public void fillPacketIntoSamplePacket(byte[] packet, SamplePacket samplePacket) {
		switch (format) {
			case FORMAT_8BIT_SIGNED:
				fillPacketIntoSamplePacket_8BitSigned(packet, samplePacket);
				break;
			case FORMAT_8BIT_UNSIGNED:
				fillPacketIntoSamplePacket_8BitUnsigned(packet, samplePacket);
				break;
			default:
				Log.e(LOGTAG, "fillPacketIntoSamplePacket: invalid format: " + format);
				break;
		}
	}

	public void fillPacketIntoSamplePacket_8BitSigned(byte[] packet, SamplePacket samplePacket) {
		int size = samplePacket.size();
		int capacity = samplePacket.capacity();
		lookupTable8Bit.convertFromSignedInterleaved8Bit(packet, samplePacket.getReAlloc(), samplePacket.getImAlloc(), size, capacity);
		size += packet.length / 2;
		if(size > capacity)
			size = capacity;
		samplePacket.setSize(size);
	}

	public void fillPacketIntoSamplePacket_8BitUnsigned(byte[] packet, SamplePacket samplePacket) {
		int size = samplePacket.size();
		int capacity = samplePacket.capacity();
		lookupTable8Bit.convertFromUnsignedInterleaved8Bit(packet, samplePacket.getReAlloc(), samplePacket.getImAlloc(), size, capacity);
		size += packet.length / 2;
		if(size > capacity)
			size = capacity;
		samplePacket.setSize(size);
	}

	public int mixPacketIntoSamplePacket(byte[] packet, SamplePacket samplePacket, long channelFrequency) {
		switch (format) {
			case FORMAT_8BIT_SIGNED:
				return mixPacketIntoSamplePacket_8BitSigned(packet, samplePacket, channelFrequency);
			case FORMAT_8BIT_UNSIGNED:
				return mixPacketIntoSamplePacket_8BitUnsigned(packet, samplePacket, channelFrequency);
			default:
				Log.e(LOGTAG, "fillPacketIntoSamplePacket: invalid format: " + format);
				return -1;
		}
	}

	public int mixPacketIntoSamplePacket_8BitSigned(byte[] packet, SamplePacket samplePacket, long channelFrequency) {
		// If mix frequency is too low, just add the sample rate (sampled spectrum is periodic):
		int mixFrequency = (int) (channelFrequency - frequency);
		if(mixFrequency == 0 || (sampleRate / Math.abs(mixFrequency) > MAX_COSINE_LENGTH))
			mixFrequency += sampleRate;

		// Only generate lookupTable if invalid:
		if(mixerLookupTableInvalid || mixFrequency != mixer8Bit.getCosineFrequency()) {
			int bestLength = calcOptimalCosineLength(mixFrequency);
			mixer8Bit.generateLookupTable(sampleRate, mixFrequency, bestLength, true);
			mixerLookupTableInvalid = false;
		}

		// mix and convert packet:
		int size = samplePacket.size();
		int count = mixer8Bit.mixFromSignedInterleaved8Bit(packet, samplePacket.getReAlloc(), samplePacket.getImAlloc(), size, samplePacket.capacity());
		samplePacket.setSize(size + count);
		return count;
	}

	public int mixPacketIntoSamplePacket_8BitUnsigned(byte[] packet, SamplePacket samplePacket, long channelFrequency) {
		// If mix frequency is too low, just add the sample rate (sampled spectrum is periodic):
		int mixFrequency = (int) (channelFrequency - frequency);
		if(mixFrequency == 0 || (sampleRate / Math.abs(mixFrequency) > MAX_COSINE_LENGTH))
			mixFrequency += sampleRate;

		// Only generate lookupTable if invalid:
		if(mixerLookupTableInvalid || mixFrequency != mixer8Bit.getCosineFrequency()) {
			int bestLength = calcOptimalCosineLength(mixFrequency);
			mixer8Bit.generateLookupTable(sampleRate, mixFrequency, bestLength, false);
			mixerLookupTableInvalid = false;
		}

		// mix and convert packet:
		int size = samplePacket.size();
		int count = mixer8Bit.mixFromUnsignedInterleaved8Bit(packet, samplePacket.getReAlloc(), samplePacket.getImAlloc(), size, samplePacket.capacity());
		samplePacket.setSize(size + count);
		return count;
	}
}
