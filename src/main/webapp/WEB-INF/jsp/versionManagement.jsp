<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ko">

<head>
<meta http-equiv="Content-Type" content= "text/html; charset=UTF-8">
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js" type="text/javascript"> </script>
<script src="${pageContext.request.contextPath}/js/version.js" type="text/javascript"> </script>
<!-- BootStrap CDN -->
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/common_ncs.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home_editor.min.css">
<title>버전 관리 페이지</title>
</head>
<body>
	
    <h3>버전 관리 페이지 <button class="btn btn-primary" style="float:right;" onclick="location.href='${path}/assignment'">홈으로</button></h3> 
    <button class="btn btn-primary" style="float:right;" onclick="location.href='${pageContext.request.contextPath}/treeViewer/${list[0].root_board_id}'">treeViewer</button>
    
    <!--     DIFF  -->
	<form name="diffForm">
		<input name="board_id1" type="text" id="board_id1" style="display:none;">
		<input name="version1" type="text" id="version1" style="display:none;">
        <input name="board_id2" type="text" id="board_id2" style="display:none;">
        <input name="version2" type="text" id="version2" style="display:none;">
        <button class="btn btn-primary" type="button"  onclick="btnDiff();" >비교</button>
	</form>

    <table class="table" id="table">
        <tr>
            <th>체크박스</th>
            <th>게시물번호</th>
            <th>버전</th>
            <th>제목</th>
            <th>날짜</th>
            <th>상태</th>
            <th>동작</th>
            
        <c:forEach var="boardHistory" items="${list}" varStatus="index">
        <tr>
            <td><input type="checkbox" name="cbox[]" 
            value="${boardHistory.board_id}-${boardHistory.version}"
            onchange="cbox.remain_two(this);"></td>
            <td>${boardHistory.board_id}</td>
            <td>${boardHistory.version}</td>
            <td><a href="${path}/assignment/history/${boardHistory.board_id}/${boardHistory.version}">${boardHistory.history_subject}</a></td>
            <td>${boardHistory.created_time}</td>
            <td>${boardHistory.status}</td>
       		<td>
       		
            <c:choose>
	               <c:when test="${!index.first }"> 
	               		<button class="btn btn-primary" id="btnVersionDelete" 
			       		onclick="btnVersionDelete(${boardHistory.board_id},${boardHistory.version})">삭제</button>
	               		<button class="btn btn-primary" id="btnRecover" 
       					onclick="btnRecover(${boardHistory.board_id},${boardHistory.version})">복원</button>   
	               </c:when>
	               	<c:otherwise>
			       		<button class="btn btn-primary" id="btnDelete" 
			       		onclick="btnDelete(${boardHistory.board_id},${boardHistory.version})">삭제</button>
					</c:otherwise>
			</c:choose>
       		</td>
        </tr>
        </c:forEach>
    </table>

</body>
</html>


