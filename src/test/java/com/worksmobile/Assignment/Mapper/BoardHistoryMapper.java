package com.worksmobile.Assignment.Mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.worksmobile.Assignment.Domain.BoardHistoryDTO;

@Mapper
public interface BoardHistoryMapper {

	public List<BoardHistoryDTO> getHistoryByBoardId(int board_id);

	public List<BoardHistoryDTO> getHistoryByHistoryId(int board_history_id);

	public BoardHistoryDTO getHistoryBySpecificOne(@Param("board_history_id")int board_history_id, 
													@Param("version")int version,
													@Param("branch_id")int branch_id);
	
	public int deleteHistoryByHistoryId(int board_history_id);
	
	public int deleteHistoryBySpecificOne(	@Param("board_history_id")int board_history_id, 
											@Param("version")int version,
											@Param("branch_id")int branch_id);
	
	public void createHistory(BoardHistoryDTO boardHistoryDTO);

	public int updateHistory(BoardHistoryDTO boardHistoryDTO);
}
