package com.worksmobile.Assignment.Mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;

@Mapper
public interface BoardHistoryMapper {

	public List<BoardHistoryDTO> getHistoryByRootBoardId(@Param("root_board_id")int root_board_id);
	
	public BoardHistoryDTO getHistory(NodePtrDTO nodePtr);
			
	public int deleteHistory(NodePtrDTO nodePtr);
	
	public int createHistory(BoardHistoryDTO boardHistoryDTO);

	public int updateHistoryParentAndRoot(BoardHistoryDTO boardHistoryDTO);

	public List<BoardHistoryDTO> getChildren(NodePtrDTO nodePtr);
}
