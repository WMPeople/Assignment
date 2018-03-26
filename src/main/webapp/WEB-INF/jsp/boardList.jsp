﻿<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ko">

<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="js/jquery-ui-1.11.0.js"></script>
<head>
<meta http-equiv="Content-Type" content= "text/html; charset=UTF-8">
<!-- BootStrap CDN -->
<link rel="stylesheet" href="css/jquery-ui-1.11.0.css">
<link rel="stylesheet" href="css/bootstrap.min.3.3.2.css">
<link rel="stylesheet" href="css/bootstrap-theme.min.3.3.2.css">
<title>게시물 목록</title>
</head>
<body>
    <h3>게시물 목록</h3> 
    <button class="btn btn-primary" style="float : right;" onclick="location.href='/Assignment/boardCreate'">글쓰기</button>
   
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
            <td><a href="/Assignment/boardDetail?board_id=${board.board_id}">${board.subject}</a></td>
            <td>${board.created}</td>
			<td> <button class="btn btn-primary" onclick="location.href='/Assignment/boardDelete?board_id=${board.board_id}'">삭제</button> </td> 

        </tr>
        </c:forEach>
    </table>

</body>
</html>


