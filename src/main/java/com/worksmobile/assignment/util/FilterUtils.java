package com.worksmobile.assignment.util;

import com.nhncorp.lucy.security.xss.XssPreventer;

public class FilterUtils {
	public static String dirtyToClean(String dirty) {

		return XssPreventer.escape(dirty);

	}

}
