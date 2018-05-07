package com.worksmobile.assignment.bo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No such node ptr")
public class NotExistNodePtrException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NotExistNodePtrException(String str) {
		super(str);
	}
}
