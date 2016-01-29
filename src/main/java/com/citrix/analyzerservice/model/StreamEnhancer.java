package com.citrix.analyzerservice.model;

/**
 * @author Xi Luo
 *
 */
public class StreamEnhancer {

	private double[] QNr;
	private double[] QuantumsInJitterBuffer;
	private double[] PacketScaleFast;
	private double[] PacketScaleSlow;
	private double[] PacketScaleFactor;
	private double[] QuantumUnderrunCounter;
	private double[] QuantumType;
	private double[] PopTimeDelta;
	
	public StreamEnhancer(double[] qNr, double[] quantumsInJitterBuffer, double[] packetScaleFast,
			double[] packetScaleSlow, double[] packetScaleFactor, double[] quantumUnderrunCounter, double[] quantumType,
			double[] popTimeDelta) {
		QNr = qNr;
		QuantumsInJitterBuffer = quantumsInJitterBuffer;
		PacketScaleFast = packetScaleFast;
		PacketScaleSlow = packetScaleSlow;
		PacketScaleFactor = packetScaleFactor;
		QuantumUnderrunCounter = quantumUnderrunCounter;
		QuantumType = quantumType;
		PopTimeDelta = popTimeDelta;
	}

	public double[] getQNr() {
		return QNr;
	}

	public double[] getQuantumsInJitterBuffer() {
		return QuantumsInJitterBuffer;
	}

	public double[] getPacketScaleFast() {
		return PacketScaleFast;
	}

	public double[] getPacketScaleSlow() {
		return PacketScaleSlow;
	}

	public double[] getPacketScaleFactor() {
		return PacketScaleFactor;
	}

	public double[] getQuantumUnderrunCounter() {
		return QuantumUnderrunCounter;
	}

	public double[] getQuantumType() {
		return QuantumType;
	}

	public double[] getPopTimeDelta() {
		return PopTimeDelta;
	}

}
