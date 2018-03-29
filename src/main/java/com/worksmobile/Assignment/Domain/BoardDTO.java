package com.worksmobile.Assignment.Domain;

import lombok.Getter;
import lombok.Setter;

public class BoardDTO extends NodePtrDTO{
	@Setter @Getter private String subject;
	@Setter @Getter private String content;
	@Setter @Getter private String created;
	
	@Setter @Getter private String file_name;
	@Setter @Getter private byte[] file_data;
	@Setter @Getter private long file_size;
	
	public BoardDTO() { }
	/***
	 * BoardHistoryDTO의 공통된 내용을 전부 가져옵니다.
	 * 내용을 압축 해제 역할도 담당.
	 * @param boardHistoryDTO
	 */
	public BoardDTO(BoardHistoryDTO boardHistoryDTO) {
		setNodePtrDTO(boardHistoryDTO);

		subject = boardHistoryDTO.getHistory_subject();
		// TODO : unzipping
		//content = boardHistoryDTO.getHistory_content();
		created = boardHistoryDTO.getCreated();

		file_name = boardHistoryDTO.getFile_name();
		file_data = boardHistoryDTO.getFile_data();
		file_size = boardHistoryDTO.getFile_size();
	}

	public void setNodePtrDTO(NodePtrDTO nodePtrDTO) {
		board_id = nodePtrDTO.getBoard_id();
		version = nodePtrDTO.getVersion();
		branch = nodePtrDTO.getBranch();
	}
}
