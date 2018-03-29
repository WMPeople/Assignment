package com.worksmobile.Assignment.Mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;

@Mapper
public interface BoardHistoryMapper {

	public BoardHistoryDTO getHistory(NodePtrDTO nodePtr);
			
	public int deleteHistory(NodePtrDTO nodePtr);
	
	public int createHistory(BoardHistoryDTO boardHistoryDTO);

	public int updateHistoryParent(BoardHistoryDTO boardHistoryDTO);

	public Integer getLastbranch(@Param("board_id")int board_id, @Param("version")int version);

	public List<BoardHistoryDTO> getChildren(NodePtrDTO nodePtr);
}
