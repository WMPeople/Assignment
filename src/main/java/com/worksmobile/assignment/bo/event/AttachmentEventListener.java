package com.worksmobile.assignment.bo.event;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.worksmobile.assignment.bo.FileService;

@Component
public class AttachmentEventListener {
	@Autowired
	FileService fileService;
	
	@EventListener
	public void eventListener(AttachmentChangedEvent event) {
		Set<Integer> ids = event.getFileIdSet();
		fileService.deleteNoMoreUsingFile(ids);
	}
}
