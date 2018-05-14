package com.worksmobile.assignment.bo;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
/**
 * 
 * @author khh
 *
 */
@Component
public class AsyncAwaitHelper{
	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	public BoardHistory waitAndSelectBoardHistory(NodePtr boardHistory) {
		return waitAndGet(() -> {
			return boardHistoryMapper.selectHistory(boardHistory);
		});
	}
	
	public static interface StatementStrategy <T>{
		public T get();
	}
	
	public <T> T waitAndGet(AsyncAwaitHelper.StatementStrategy<T> stmt) {
		await().untilAsserted(() -> assertThat(stmt.get(), not(nullValue())));
		return stmt.get();
	}
}
