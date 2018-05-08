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
    <script>
    var WMoo = document.registerElement('wm-movie', { 
        prototype: Object.create(HTMLElement.prototype) });
    var xFoo = document.createElement('x-foo');
    xFoo.addEventListener('click',function(e){alert('tat');});
    </script>
    <asd-asd>asd</asd-asd>
    <x-foo id="asdasd">xfoo</x-foo>
    <script>document.getElementById('asdasd').innerHTML='zzz';</script>
    <wm-movie>asdasd</wm-movie>
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
					<c:if test="${paging.currentPageNo gt 5}">  											  <!-- 현재 페이지가 5보다 크다면(즉, 6페이지 이상이라면) -->
						<li><a href="javascript:goPage(${paging.prevPageNo}, ${paging.maxPost})">이전</a></li> <!-- 이전페이지 표시 -->
					</c:if>
					<!-- 다른 페이지를 클릭하였을 시, 그 페이지의 내용 및 하단의 페이징 버튼을 생성하는 조건문-->
						<c:forEach var="i" begin="${paging.startPageNo}" end="${paging.endPageNo}" step="1"> <!-- 변수선언 (var="i"), 조건식, 증감식 -->
				            <c:choose>
				                <c:when test="${i eq paging.currentPageNo}"> 
				                    <li class="active"><a href="javascript:goPage(${i}, ${paging.maxPost})">${i}</a></li> <!-- 1페이지부터 10개씩 뽑아내고, 1,2,3페이지순으로 나타내라-->
				                </c:when>
				                	<c:otherwise>
				                    <li><a href="javascript:goPage(${i}, ${paging.maxPost})">${i}</a></li> 
									</c:otherwise>
							</c:choose>
						</c:forEach>
						<!-- begin에 의해서 변수 i는 1이기 때문에, 처음에는 c:when이 수행된다. 그 후 페이징의 숫자 2를 클릭하면 ${i}는 2로변하고, 현재는 ${i}는 1이므로 otherwise를 수행한다
						         그래서 otherwise에 있는 함수를 수행하여 2페이지의 게시물이 나타나고, 반복문 실행으로 다시 forEach를 수행한다. 이제는 i도 2이고, currentPageNo도 2이기 때문에
						     active에 의해서 페이징부분의 2에 대해서만 파란색으로 나타난다. 그리고 나머지 1,3,4,5,이전,다음을 표시하기위해 다시 c:otherwise를 수행하여 페이징도 나타나게한다.-->
					<!-- // 다른 페이지를 클릭하였을 시, 그 페이지의 내용 및 하단의 페이징 버튼을 생성하는 조건문-->
											
					<!-- 소수점 제거 =>-->
					<fmt:parseNumber var="currentPage" integerOnly="true" value="${(paging.currentPageNo-1)/5}"/>
					<fmt:parseNumber var="finalPage" integerOnly="true" value="${(paging.finalPageNo-1)/5}"/>
						
					<c:if test="${currentPage < finalPage}"> <!-- 현재 페이지가 마지막 페이지보다 작으면 '다음'을 표시한다. -->
						<li><a href="javascript:goPage(${paging.nextPageNo}, ${paging.maxPost})">다음</a></li>
					</c:if> 
					<li><a href="javascript:goPage(${paging.numberOfRecords/10},${paging.maxPost})" style = "margin-left : 10px;">끝</a></li>
				</ul>
			</div>
		</c:when>
		</c:choose>
</body>
</html>


