package com.worksmobile.assignment.bo;

public class CompressionMaker {
	private static Compression compress = new ZipCompression();
	
	public static Compression getCompress() {
		return compress;
	}
}
