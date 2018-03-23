package com.worksmobile.Assignment.Domain;

public class BoardHistoryDTO {
	private float board_history_id;
	private String created;
	private String status;
	private int board_id;
	
	public float getBoard_history_id() {
		return board_history_id;
	}
	public void setBoard_history_id(float board_history_id) {
		this.board_history_id = board_history_id;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getBoard_id() {
		return board_id;
	}
	public void setBoard_id(int board_id) {
		this.board_id = board_id;
	}

}
