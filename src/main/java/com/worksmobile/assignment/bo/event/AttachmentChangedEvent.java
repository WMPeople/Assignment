package com.worksmobile.assignment.bo.event;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AttachmentChangedEvent{
	
	@Getter private Set<Integer> fileIdSet = new HashSet<>();
	
	public AttachmentChangedEvent(int fileId) {
		fileIdSet.add(fileId);
	}
}
