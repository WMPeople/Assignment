package com.worksmobile.Assignment.Mapper;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.Assignment.Domain.BranchDTO;

@Mapper
public interface BranchMapper {
	
	public BranchDTO getBranch(int branch_id);
	
	// next_version이 0이면 무시하고 삭제합니다. 
	public int deleteBranch(BranchDTO branchDTO);
	
	// next_version이 0이면 null을 적용합니다.
	public void createBranch(BranchDTO branchDTO);

	public int updateBranch(BranchDTO branchDTO);
}
