package com.worksmobile.assignment.bo;

public class CompressMaker {
	private static Compress compress = new ZipCompress();
	
	public static Compress getCompress() {
		return compress;
	}
}
