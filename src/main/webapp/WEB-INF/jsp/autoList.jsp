<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ko">


<head>
<meta http-equiv="Content-Type" content= "text/html; charset=UTF-8">
<!-- BootStrap CDN -->
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js"></script>
<title>자동 게시글 목록</title>
</head>
<body>
    <span id="title" style="float: left; margin-left: 10px; font-size: 30px;">자동 게시글 목록</span>
    <span id="cookie_id" style="float: right; margin-right: 10px; font-size: 20px;"> Cookie_id : ${cookie_id}</span>
    <br></br>
 
    <table class="table">
        <tr>
            <th>게시물번호</th>
            <th>버전</th>
            <th>쿠키ID</th>
            <th>제목</th>
            <th>최종수정시간</th>
          
        </tr>
        
        <c:forEach var="boardTemp" items="${boardTemp}">
        <tr>
            <td>${boardTemp.board_id}</td>
            <td>${boardTemp.version}</td>
            <c:if test="${cookie_id eq boardTemp.cookie_id}"></c:if>
            <c:choose>
	              <c:when test="${cookie_id eq boardTemp.cookie_id}"> 
	                  <td style = "color : #ec1313;">${boardTemp.cookie_id}</td>
	              </c:when>
                  <c:otherwise>
                  <td >${boardTemp.cookie_id}</td>
                  </c:otherwise>
	        </c:choose>
       		<td><a href="${path}/assignment/autos/${boardTemp.board_id}/${boardTemp.version}/${boardTemp.cookie_id}">${boardTemp.subject}</a></td>
            <td>${boardTemp.created_time}</td>
			<td> 
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


