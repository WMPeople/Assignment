package com.worksmobile.Assignment.Mapper;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.Assignment.Domain.BoardDTO;

@Mapper
public interface TempBoardMapper {

	public BoardDTO getRecentVersion(int board_history_id);
	
	public int deleteRecentVersion(int board_history_id);
	
	public void createRecentVersion(BoardDTO boardHistoryDTO);

	public int updateRecentVersion(BoardDTO boardHistoryDTO);
}
