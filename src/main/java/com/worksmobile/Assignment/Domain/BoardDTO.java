package com.worksmobile.Assignment.Domain;

import org.springframework.web.multipart.MultipartFile;

public class BoardDTO {
	private int id;
	private String subject;
	private String content;
	private String created;
	private MultipartFile attachment;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public MultipartFile getAttachment() {
		return attachment;
	}
	public void setAttachment(MultipartFile attachment) {
		this.attachment = attachment;
	}


}
