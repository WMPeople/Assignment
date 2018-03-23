package com.worksmobile.Assignment.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.worksmobile.Assignment.Domain.BoardDTO;

@Mapper
public interface BoardMapper {
	public List<BoardDTO> boardList() throws Exception;

	public int boardCreate(BoardDTO board) throws Exception;

	public int boardDelete(int id) throws Exception;

	public int boardUpdate(BoardDTO board) throws Exception;

}
