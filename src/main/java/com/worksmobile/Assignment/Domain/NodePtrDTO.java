	@Override
	public boolean equals(Object arg0) {
		NodePtrDTO dto = (NodePtrDTO)arg0;
		return	board_id == dto.board_id &&
				version == dto.version &&
				branch == dto.branch;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("board_id : ").append(board_id)
				.append("version : ").append(version)
				.append("branch : ").append(branch);
		return builder.toString();
	}
}
	private static boolean checkEquals(Integer lhs, Integer rhs) {
		return lhs == null ? (rhs == null ? true : false) : lhs.equals(rhs);
	}
	
	@Override
	public boolean equals(Object arg0) {
		NodePtrDTO dto = (NodePtrDTO)arg0;
		return	checkEquals(board_id, dto.board_id) &&
				checkEquals(version, dto.version) &&
				checkEquals(branch, dto.branch);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("board_id : ").append(board_id)
				.append("version : ").append(version)
				.append("branch : ").append(branch);
		return builder.toString();
	}
}

