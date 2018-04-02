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
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
</head>
<body>

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
			<span class="name">브랜치 : <span class="_group">${board.branch}</span> <span class="_company"></span></span>
			<span class="date">최종 수정시간 :  ${board.created}</span>
			<span class="date">첨부 파일 :   <a href="${path}/Assignment/boards/download/${board.board_id}/${board.version}/${board.branch}" name="file">${board.file_name}     </a> (${board.file_size})</span>

		<div class="cont _content translateArea" id="content"><p>
		<span style="font-family: 나눔고딕, NanumGothic, sans-serif;">
		<font class="NaverTrans-Parent"><font class="NaverTrans-Child"> 
		<pre>${board.content}</pre>	
		</font>
		</font></span></p>
		</div>

		<div class="btn_box _btn_area _no_print">
			<form action="/Assignment/boards/update" method="post" >
				<input name="board_id" type="text" id="board_id" value="${board.board_id}" style="display:none;">
				<input name="version" type="text" id="version" value="${board.version}" style="display:none;">
				<input name="branch" type="text" id="branch" value="${board.branch}" style="display:none;">
	            <input name="subject" type="text" id="subject" value="${board.subject}" style="display:none;">
	            <input name="created" type="text" id="created" value="${board.created}" style="display:none;">
	            <input name="content" type="text" id="content" value="${board.content}" style="display:none;">
	            <input name="file_name" type="text" id="file_name" value="${board.file_name}" style="display:none;">
	            <input name="file_data" type="file" id="file_data" value="${board.file_data}" style="display:none;">
	            <input name="file_size" type="text" id="file_size" value="${board.file_size}" style="display:none;">
	            <button class="_edit_atc" type="submit">수정</button>	
	            <button class="_delete_atc" type="button" onclick="btnDelete(${board.board_id},${board.version},${board.branch});">삭제</button>
       		</form>
		</div>
	</div>
</div>
<script>
$(document).ready(function(){
	var text = $('#content > pre').text().replace(/\n/g, "<br>");
	$('body > div > div.infor._infor > div.btn_box._btn_area._no_print > form > input[type="text"]:nth-child(6)').val(text);
// 	$('#content').val($('#content > pre').text());
 });

function btnDelete(board_id,version,branch){
    $.ajax({
        type: "DELETE",
        url: "${path}/Assignment/boards/"+board_id+"/"+version+"/"+branch,
        success: function(result){
        	if(result == 'success'){
        		alert("삭제완료");
        		location.href = "/Assignment/";
        	}
        }
    })
}


</script>
</body>
</html>