package com.worksmobile.assignment.bo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

@Service
public class BoardService {
	@Autowired
	private BoardMapper boardMapper;
	
	public boolean isLeaf(final NodePtr nodePtr) {
		Board board = boardMapper.viewDetail(nodePtr.toMap());

		if (board != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public NodePtr createNewArticle(Board article) {
		if (article.getSubject().length() == 0) {
			throw new RuntimeException("제목이 비어있을 수 없습니다.");
		}
		if (article.getContent() == null || article.getContent().length() == 0) {
			throw new RuntimeException("내용이 null 이거나 비어 있습니다. content : " + article.getContent());
		}
		article.setBoard_id(NodePtr.ISSUE_NEW_BOARD_ID);
		int insertedRowCnt = boardMapper.createBoard(article);
		if (insertedRowCnt != 1) {
			throw new RuntimeException("createArticle메소드에서 createBoard error" + article);
		}
		return new NodePtr(article);
	}
	
	/**
	 * 충돌관리의 역할을 가지고 있습니다.
	 * @param article
	 * @param paretNodePtr
	 * @return
	 */
	public NodePtr modifyArticle(Board article, NodePtr paretNodePtr) {
		article.setVersion(paretNodePtr.getVersion() + 1);
		article.setRoot_board_id(paretNodePtr.getRoot_board_id());

		if(isLeaf(paretNodePtr)) {
			article.setBoard_id(paretNodePtr.getBoard_id());
			return updateArticle(article, paretNodePtr);
		} else {
			return createNewArticle(article);
		}
	}
	
	public Board selectArticle(NodePtr nodePtr) {
		return boardMapper.viewDetail(nodePtr.toMap());
	}

	private NodePtr updateArticle(Board article, NodePtr oldPtr) {
		int updatedCnt = boardMapper.updateArticle(article, oldPtr);
		if(updatedCnt != 1) {
			String articleJson = JsonUtils.jsonStringIfExceptionToString(article);
			String parentPtrJson = JsonUtils.jsonStringIfExceptionToString(oldPtr);
			throw new RuntimeException("updateArticle 실패 article : " + articleJson + "\n" +
										"parentNodePtr : " + parentPtrJson);
		}
		return new NodePtr(article);
	}

	public void deleteBoard(NodePtr leafPtr) {
		int deletedCnt = boardMapper.deleteBoard(leafPtr.toMap());
		if(deletedCnt != 1) {
			throw new RuntimeException("삭제가 개수가 맞지 않습니다. cnt : " + deletedCnt);
		}
	}
}