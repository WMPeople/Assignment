package com.worksmobile.assignment.bo;

import java.io.IOException;

public interface Compress {

	public byte[] compress(String plainText) throws IOException;

	public String deCompress(byte[] compressed) throws IOException;
	
}
