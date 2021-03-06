﻿<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery-ui-1.11.0.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/common_ncs.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home_editor.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/context_info.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/semantic.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/transition.css">
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js"></script>
<script src="${pageContext.request.contextPath}/js/crawling.js"></script>
<!-- 상품 정보에 관한 js -->
<script src="${pageContext.request.contextPath}/js/clamp.js"></script>
<script src="${pageContext.request.contextPath}/js/semantic.js"></script>
<script src="${pageContext.request.contextPath}/js/transition.js"></script>
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
            <button class="btn btn-primary" style="float: right;" onclick="location.href='${path}/assignment'">홈으로</button>
        </div>
        <div class="infor _infor">
            <span>게시물 번호 : <span>${board.board_id}</span> <span></span></span>
            <span>버전 : <span>${board.version}</span> <span></span></span>
            <span>최종 수정시간 :  ${board.created_time}</span>
            <span>첨부 파일 :   <a href="${path}/assignment/boards/download/${file.file_id}" name="file">${file.file_name}</a></span>
            <span>파일 용량 : </span>
            <span id="span_fileSize">${file.file_size}</span>
            <button class="btn btn-primary" id="btnAutoList" onclick="location.href='${path}/assignment/autos/${board.board_id}/${board.version}'">자동저장리스트</button>
        <div class="cont _content translateArea" id="contents">
                <textarea name="content3" id="content_detail" style="min-height: 500px; min-width: 700px; display:none;" >${board.content}</textarea>
        </div>
        <div class="btn_box _btn_area _no_print">
            <form action="/assignment/boards/update" method="post" style="display: inline;">
                <input name="board_id" type="text" id="board_id" value="${board.board_id}" style="display:none;">
                <input name="version" type="text" id="version" value="${board.version}" style="display:none;">
                <textarea name="subject" type="text" id="subject" style="display:none;"><c:out value="${board.subject}" escapeXml="true" /></textarea>
                <input name="created_time" type="text" id="created_time" value="${board.created_time}" style="display:none;">
                <textarea name="content" id="content"  style="display:none;"><c:out value="${board.content}" escapeXml="true" /></textarea>
                <input name="file_id" type="text" id="file_id" value="${file.file_id}" style="display:none;">
                <input name="file_name" type="text" id="file_name" value="${file.file_name}" style="display:none;">
                <input name="file_data" type="file" id="file_data" value="${file.file_data}" style="display:none;">
                <input name="file_size" type="text" id="file_size" value="${file.file_size}" style="display:none;">
                <button class="_edit_atc" type="submit">수정</button> 
                <button class="_delete_atc" type="button" onclick="btnDelete(${board.board_id},${board.version});">삭제</button>
            </form>
        </div>
    </div>
</div>
<div id="dialog">
</div>
</body>
</html>