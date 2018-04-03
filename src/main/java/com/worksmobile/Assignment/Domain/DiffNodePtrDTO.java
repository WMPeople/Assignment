package com.worksmobile.Assignment.Domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.Assignment.util.Utils;

import lombok.Getter;
import lombok.Setter;

public class DiffNodePtrDTO {
	@Getter @Setter private int board_id1;
	@Getter @Setter private int version1;
	
	@Getter @Setter private int board_id2;
	@Getter @Setter private int version2;
	
	@Override
	public String toString() {
		try {
			return Utils.jsonStringFromObject(this);
		} catch (JsonProcessingException e) {
			return "parse error";
		}
	}
	
	public NodePtrDTO getLeftPtrDTO() {
		return new NodePtrDTO(board_id1, version1);
	}
	
	public NodePtrDTO getRightPtrDTO() {
		return new NodePtrDTO(board_id2, version2);
	}
}

