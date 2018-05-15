<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ko">

<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js"></script>
<head>
<meta http-equiv="Content-Type" content= "text/html; charset=UTF-8">
<!-- BootStrap CDN -->
<link rel="stylesheet" href="css/jquery-ui-1.11.0.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<title>게시물 목록</title>
</head>
<body>
    <span id="title" style="float: left; margin-left: 10px; font-size: 30px;"> 게시물 목록</span>
    <span id="cookie_id" style="float: right; margin-right: 10px; font-size: 20px;"> Cookie_id : ${cookie_id}</span>
    <br></br>
    <button class="btn btn-primary" style="float : right;" onclick="location.href='/assignment/boards'">글쓰기</button>
 
    <table class="table">
        <tr>
            <th>게시물번호</th>
            <th>버전</th>
            <th>제목</th>
            <th>최종수정시간</th>
            <th>동작</th>
        </tr>
        
        <c:forEach var="board" items="${board}">
        <tr>
            <td>${board.board_id}</td>
            <td>${board.version}</td>
       		<td><a href="${path}/assignment/boards/${board.board_id}/${board.version}">${board.subject}</a></td>
            <td>${board.created_time}</td>
			<td> 
			<button class="btn btn-primary" id="btnDelete" onclick="btnDelete(${board.board_id},${board.version});">삭제</button> 
			<button class="btn btn-primary" id="btnManagement" onclick="location.href='${path}/assignment/boards/management/${board.board_id}/${board.version}'">버전관리</button>
			</td> 
        </tr>
        </c:forEach>
       
    </table>
    <c:choose>
		<c:when test="${paging.numberOfRecords ne NULL and paging.numberOfRecords ne '' and paging.numberOfRecords ne 0}">
			<div class="text-center marg-top">
				<ul class="pagination">
				<li><a href="javascript:goPage(1, ${paging.maxPost})" style = "margin-right : 10px;">처음</a></li>
					<c:if test="${paging.currentPageNo gt 5}">
						<li><a href="javascript:goPage(${paging.prevPageNo}, ${paging.maxPost})">이전</a></li> 
					</c:if>
						<c:forEach var="i" begin="${paging.startPageNo}" end="${paging.endPageNo}" step="1"> 
				            <c:choose>
				                <c:when test="${i eq paging.currentPageNo}"> 
				                    <li class="active"><a href="javascript:goPage(${i}, ${paging.maxPost})">${i}</a></li> 
				                </c:when>
				                	<c:otherwise>
				                    <li><a href="javascript:goPage(${i}, ${paging.maxPost})">${i}</a></li> 
									</c:otherwise>
							</c:choose>
						</c:forEach>
					<fmt:parseNumber var="currentPage" integerOnly="true" value="${(paging.currentPageNo-1)/5}"/>
					<fmt:parseNumber var="finalPage" integerOnly="true" value="${(paging.finalPageNo-1)/5}"/>
						
					<c:if test="${currentPage < finalPage}"> 
						<li><a href="javascript:goPage(${paging.nextPageNo}, ${paging.maxPost})">다음</a></li>
					</c:if> 
					<li><a href="javascript:goPage(${paging.numberOfRecords/10},${paging.maxPost})" style = "margin-left : 10px;">끝</a></li>
				</ul>
			</div>
		</c:when>
		</c:choose>
</body>
</html>


