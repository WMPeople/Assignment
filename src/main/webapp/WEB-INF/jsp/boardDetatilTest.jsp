<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/common_ncs.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home_editor.min.css">
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/boardTest.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>게시물 상세보기</title>
</head>
<body>
    <pre></pre>
    <div class="board_view">
        <div class="subject _title_area">
            <span class="txt_noti _attention_txt_noti" style="display: none;"></span>
            <span class="_attention"></span>
            <h3 class="txt _title translateArea">
                <font class="NaverTrans-Parent">
                    <font class="NaverTrans-Child">제목 : ${board.subject}</font>
                </font>
            </h3>
        </div>
        <div class="infor _infor">
            <span class="name">게시물 번호 : <span class="_group">${board.board_id}</span> <span class="_company"></span></span>
            <span class="name">버전 : <span class="_group">${board.version}</span> <span class="_company"></span></span>
            <span class="date">최종 수정시간 :  ${board.created_time}</span>
            <span class="date">첨부 파일 :   <a href="${path}/assignment/boards/download/${file.file_id}" name="file">${file.file_name}     </a> (${file.file_size})</span>
            <button class="btn btn-primary" id="btnAutoList" onclick="location.href='${path}/assignment/autos/${board.board_id}/${board.version}'">자동저장리스트</button>
        <div class="cont _content translateArea" id="contents">
                <textarea name="content3" id="content3" style="min-height: 500px; min-width: 700px;" readonly>${board.content}</textarea>
        </div>
        <div class="btn_box _btn_area _no_print">
            <form action="/assignment/boards/updateTest" method="post" style="display: inline;">
                <input name="board_id" type="text" id="board_id" value="${board.board_id}" style="display:none;">
                <input name="version" type="text" id="version" value="${board.version}" style="display:none;">
                <input name="cookie_id" type="text" id="cookie_id" value="${board.cookie_id}" style="display:none;">
                <input name="subject" type="text" id="subject" value="${board.subject}" style="display:none;">
                <input name="created_time" type="text" id="created_time" value="${board.created_time}" style="display:none;">
                <textarea name="content" id="content"  style="display:none;"><c:out value="${board.content}" escapeXml="true" /></textarea>
                <input name="file_id" type="text" id="file_id" value="${file.file_id}" style="display:none;">
                <input name="file_name" type="text" id="file_name" value="${file.file_name}" style="display:none;">
                <input name="file_data" type="file" id="file_data" value="${file.file_data}" style="display:none;">
                <input name="file_size" type="text" id="file_size" value="${file.file_size}" style="display:none;">
                <button class="_edit_atc" type="submit">수정</button> 
                <c:if test="${board.cookie_id eq 'LEAF_NODE_COOKIE_ID'}">
                <button class="_delete_atc" type="button" onclick="btnDelete(${board.board_id},${board.version});">삭제</button>
                </c:if>
            </form>
        </div>
    </div>
</div>
</body>
</html>