package com.worksmobile.Assignment.Mapper;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.Assignment.Domain.RecentVersionDTO;

@Mapper
public interface RecnetVersionMapper {

	public RecentVersionDTO getRecentVersion(int board_history_id);
	
	public int deleteRecentVersion(int board_history_id);
	
	public void createRecentVersion(RecentVersionDTO boardHistoryDTO);

	public int updateRecentVersion(RecentVersionDTO boardHistoryDTO);
}
