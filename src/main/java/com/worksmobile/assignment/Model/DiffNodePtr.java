package com.worksmobile.assignment.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.assignment.Util.JsonUtils;

import lombok.Getter;
import lombok.Setter;

public class DiffNodePtr {
	@Getter @Setter private int board_id1;
	@Getter @Setter private int version1;
	
	@Getter @Setter private int board_id2;
	@Getter @Setter private int version2;
	
	@Override
	public String toString() {
		try {
			return JsonUtils.jsonStringFromObject(this);
		} catch (JsonProcessingException e) {
			return "parse error";
		}
	}
	
	public NodePtr getLeftPtr() {
		return new NodePtr(board_id1, version1);
	}
	
	public NodePtr getRightPtr() {
		return new NodePtr(board_id2, version2);
	}
}

