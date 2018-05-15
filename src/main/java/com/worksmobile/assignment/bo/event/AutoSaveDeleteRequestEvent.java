package com.worksmobile.assignment.bo.event;

import java.util.List;
import java.util.Set;

import com.worksmobile.assignment.model.NodePtr;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AutoSaveDeleteRequestEvent {
	@Getter List<NodePtr> nodePtrList;
	@Getter Set<Integer> fileIds;
}
