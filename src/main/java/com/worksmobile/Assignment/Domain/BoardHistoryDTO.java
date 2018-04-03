﻿package com.worksmobile.Assignment.Domain;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

public class BoardHistoryDTO extends NodePtrDTO{
	@Getter @Setter private String created;
	@Getter @Setter private String status;
	@Getter @Setter private String history_subject;
	@Getter @Setter private byte[] history_content;

	@Getter @Setter private Integer parent_board_id = null;
	@Getter @Setter private Integer parent_version = null;

	@Setter@Getter private int file_id;
	
	public static final String STATUS_CREATED = "Created";
	public static final String STATUS_MODIFIED = "Modified";
	public static final String STATUS_RECOVERED= "Recovered";
	
	public BoardHistoryDTO() {};
	/***
	 * 게시글을 새롭게 만들때 사용. (내용 제외)공통된 내용을 전부 복사합니다.
	 * 중요! : 게시글 내용 압축은 담당하지 않습니다.
	 * @param article 새로운 게시글의 내용.
	 * @param nodePtrDTO 게시글의 포인터
	 * @throws IOException 
	 */
	public BoardHistoryDTO(BoardDTO article, NodePtrDTO nodePtrDTO, String status) {
		board_id = nodePtrDTO.getBoard_id();
		version = nodePtrDTO.getVersion() ;
		this.status = status;
		history_subject = article.getSubject();
		file_id = article.getFile_id();
	}
	
	public void setParentNodePtr(NodePtrDTO parentNodePtrDTO) {
		parent_board_id = parentNodePtrDTO.getBoard_id();
		parent_version = parentNodePtrDTO.getVersion();
	}
	
	public NodePtrDTO getParentPtrDTO() {
		return new NodePtrDTO(parent_board_id, parent_version);
	}
}