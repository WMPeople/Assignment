package com.worksmobile.assignment.bo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No such leaf node id")
public class NotLeafNodeException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotLeafNodeException(String message) {
		super(message);
	}
}
