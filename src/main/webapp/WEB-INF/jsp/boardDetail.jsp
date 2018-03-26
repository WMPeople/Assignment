<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<link rel="stylesheet" type="text/css" href="css/home.css">
<link rel="stylesheet" type="text/css" href="css/common_ncs.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home_editor.min.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
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
			<span class="date">최종 수정시간 :  ${board.created}</span>
			<span class="date">첨부 파일 :   <a href="/Assignment/boardFileDownload?board_id=${board.board_id}" name="file">${board.fileName}     </a> (${board.fileSize})</span>
			
						
								 

			

		<div class="cont _content translateArea" id="content"><p>
		<span style="font-family: 나눔고딕, NanumGothic, sans-serif;">
		<font class="NaverTrans-Parent"><font class="NaverTrans-Child"> ${board.content}
		</font>
		</font></span></p>
		</div>


		<div class="btn_box _btn_area _no_print">
			<form action="/Assignment/boardUpdate" method="post">
			<input name="board_id" type="text" value="${board.board_id}" style="display:none;">
            <input name="subject" type="text" value="${board.subject}" style="display:none;">
            <input name="created" type="text" value="${board.created}" style="display:none;">
            <input name="content" type="text" value="${board.content}" style="display:none;">
<%--             <input name="fileup" type="file" value="${board.attachment}" style="display:none;"> --%>
            <input name="fileName" type="text" value="${board.fileName}" style="display:none;">
            <button class="_edit_atc" type="submit">수정</button>
            <button class="_delete_atc" type="button" onclick="location.href='/Assignment/boardDelete?board_id=${board.board_id}'">삭제</button>
        </form>
			
		</div>
		
	</div>
</div>
</body>
</html>