package com.worksmobile.assignment.bo.event;

import java.util.List;
import java.util.Set;

import com.worksmobile.assignment.model.NodePtr;

import lombok.Getter;

public class AutoSaveDeleteRequestEvent {
	@Getter List<NodePtr> nodePtrList;
	@Getter Set<Integer> fileIds;
	
	public AutoSaveDeleteRequestEvent(List<NodePtr> nodePtrList, Set<Integer> fileIds) {
		this.nodePtrList = nodePtrList;
		this.fileIds = fileIds;
	}
}
