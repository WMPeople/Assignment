package com.worksmobile.assignment.bo.event;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public class AttachmentChangedEvent{
	
	@Getter private Set<Integer> fileIdSet = new HashSet<>();
	
	public AttachmentChangedEvent(int fileId) {
		fileIdSet.add(fileId);
	}
	
	public AttachmentChangedEvent(Set<Integer> ids) {
		fileIdSet.addAll(ids);
	}
}
