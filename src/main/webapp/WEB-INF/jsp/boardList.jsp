<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ko">

<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<head>
<meta http-equiv="Content-Type" content= "text/html; charset=UTF-8">
<!-- BootStrap CDN -->
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<title>게시물 목록</title>
</head>
<body>
    <h3>게시물 목록</h3> 
    <button class="btn btn-primary" style="float : right;" onclick="location.href='/Assignment/boardCreate'">글쓰기</button>
      <button class="btn btn-primary" style="float : right;" onclick="location.href='/Assignment/boardTemp'">비교테스트</button>
   
    <table class="table">
        <tr>
            <th>게시물번호</th>
            <th>제목</th>
            <th>최종수정시간</th>
            <th>삭제</th>
            
        </tr>
        <c:forEach var="board" items="${list}">
        <tr>
            <td>${board.board_id}</td>
<%--             <td><a href="/Assignment/boardDetail?board_id=${board.board_id}">${board.subject}</a></td> --%>
            <td><a href="${path}/Assignment/boards?board_id=${board.board_id}">${board.subject}</a></td>
            
            <td>${board.created}</td>
			<td> <button class="btn btn-primary" id="btnDelete2" onclick="btnDelete('${board.board_id}');">삭제</button> </td> 

        </tr>
        </c:forEach>
    </table>
<script>
$(document).ready(function(){
    /* 게시글 관련 */
    // 1. 게시글 수정
	
    
 });
function btnDelete(board_id){
    $.ajax({
        type: "DELETE",
        url: "${path}/Assignment/boards/"+board_id,
        success: function(result){
           alert("삭제완료")
           location.reload();
        }
    })
}

</script>
</body>
</html>


