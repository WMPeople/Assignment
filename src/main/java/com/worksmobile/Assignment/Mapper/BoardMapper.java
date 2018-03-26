package com.worksmobile.Assignment.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.worksmobile.Assignment.Domain.BoardDTO;

@Mapper
public interface BoardMapper {
	public List<BoardDTO> boardList();

	public int boardCreate(BoardDTO board);

	public int boardDelete(int id);

	public int boardUpdate(BoardDTO board);

	public BoardDTO getArticle(int id);

}
