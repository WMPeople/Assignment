package com.worksmobile.assignment.bo;

import java.io.IOException;

/**
 * 
 * @author khh
 *
 */
public interface Compress {

	public byte[] compress(byte[] byteArr) throws IOException;

	public byte[] deCompress(byte[] compressed) throws IOException;
	
}
