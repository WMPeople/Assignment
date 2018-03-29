package com.worksmobile.Assignment.Domain;

import lombok.Getter;
import lombok.Setter;

public class BoardDTO {
	@Setter @Getter private int board_id;
	@Setter @Getter private int version;
	@Setter @Getter private int branch;
	@Setter @Getter private String subject;
	@Setter @Getter private String content;
	@Setter @Getter private String created;
	
	@Setter @Getter private String file_name;
	@Setter @Getter private byte[] file_data;
	@Setter @Getter private long file_size;
	
	public BoardDTO() { }
	public BoardDTO(BoardHistoryDTO boardHistoryDTO) {
		setNodePtrDTO(new NodePtrDTO(boardHistoryDTO));

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
