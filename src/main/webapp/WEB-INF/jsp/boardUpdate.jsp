<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet"
	href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<link rel="stylesheet" type="text/css" href="../css/home.css">
<link rel="stylesheet" type="text/css" href="../css/common_ncs.css">
<link rel="stylesheet" type="text/css" href="../css/home_editor.min.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<script src="../js/auto_save.js"></script>
<script src="../js/diff_match_patch.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>게시글 수정</title>
</head>
<body>
     <button class="btn btn-primary" style="float:right; VALUE="HOME" ONCLICK="location.href='${path}/assignment'">홈으로</button>
	<form id="fileForm" action="fileUpload" method='post'
		enctype="multipart/form-data">
		<div id="articleEditor">
			<div class="board_write">
				<h2 class="tit">글수정</h2>
				<ul class="option _info">
					<li class="opt_sbj">
						<h3 class="tx">제목</h3> <span class="wrap_sbj"> <input
							type="text" name="subject" id="subject"
							class="itxt subject _title"
							value="<%=request.getParameter("subject")%>">
					</span>
					</li>
					<li class="opt_file">
						<h3 class="tx">파일첨부</h3> <c:if test="${param.file_name != ''}">
							<span class="date" id="test">첨부 파일 : <a
								href="${path}/assignment/boards/download/${param.file_id}/"
								name="file">${param.file_name} </a> (${param.file_size})
								<button type="button" id="fileUpdate" class="btn tx_point _save">파일
									수정</button>
							</span>
						</c:if> <c:if test="${param.file_name == '' }">
							<input type="file" id="fileUp" name="fileUp" />
						</c:if>
					</li>
				</ul>
				<%
					response.addHeader("X-XSS-Protection","0");
					pageContext.setAttribute("tab", "	"); 
					pageContext.setAttribute("nbsp", "&nbsp;"); 
					pageContext.setAttribute("crcn", "\r\n"); 
		 			pageContext.setAttribute("br", "<br/>"); 
				%>
				<textarea name="content" id="content"
					style="min-height: 500px; min-width: 700px;">${param.content}</textarea>
				<div class="btn_area _btn_area">
					<input type="text" name="board_id" id="board_id"
						style="display: none;"
						value="<%=request.getParameter("board_id")%>"> <input
						type="text" name="version" id="version" style="display: none;"
						value="<%=request.getParameter("version")%>">  <input
						type="text" name="cookie_id" id="cookie_id" style="display: none;"
						value="<%=request.getParameter("cookie_id")%>">
					<button type="button" id="btnUpdate" class="btn tx_point _save">
						<strong>수정</strong>
					</button>
				</div>
			</div>
		</div>
	</form>
</body>
<script>
$(document).ready(function(){
	 var formData = new FormData($("#fileForm")[0]);
	 $('#fileUp').on('change' , function(){ 
         formData = new FormData($("#fileForm")[0]);
         $.ajax({
             type : "POST",
             contentType : "application/json; charset=UTF-8",
             data : formData,
             processData : false,
             contentType : false,
             url : "/assignment/boards/autosavewithfile",
             success : function(result) {
                 if (result.result == 'success') {
                     console.log("자동 저장 성공");
                 } else {
                     alert("자동 저장 실패");
                 }
             },
             error : function(xhr, status, error) {
                 alert(error);
             }
         });
     });
	$("#fileUpdate").click(function(){
		document.getElementById('test').innerHTML='<input type="file" id="fileUp" name="fileUp" />'
		formData = new FormData($("#fileForm")[0]);
		$.ajax({
            type : "POST",
            contentType : "application/json; charset=UTF-8",
            data : formData,
            processData : false,
            contentType : false,
            url : "/assignment/boards/autosavewithfile",
            success : function(result) {
                if (result.result == 'success') {
                    console.log("자동 저장 성공");
                } else {
                    alert("자동 저장 실패");
                }
            },
            error : function(xhr, status, error) {
                alert(error);
            }
        });
		
		$('#fileUp').on('change' , function(){ 
            formData = new FormData($("#fileForm")[0]);
            $.ajax({
                type : "POST",
                contentType : "application/json; charset=UTF-8",
                data : formData,
                processData : false,
                contentType : false,
                url : "/assignment/boards/autosavewithfile",
                success : function(result) {
                    if (result.result == 'success') {
                        console.log("자동 저장 성공");
                    } else {
                        alert("자동 저장 실패");
                    }
                },
                error : function(xhr, status, error) {
                    alert(error);
                }
            });
        });
		
	});
});

$("#btnUpdate").click(function(){
   	var file_name = '<%=request.getParameter("file_name")%>';
   	if(file_name != ''){
   		file_name = '<%=request.getParameter("file_name")%>';
		}
		var formData = new FormData($("#fileForm")[0]);

		var urlStr;
		//원본 게시물에 파일이 있지만 수정하지 않았을 때
		if (file_name != null && $("#fileUp").val() == null) {
			urlStr = "/assignment/boards/updatemaintainattachment";
		} else {
			urlStr = "/assignment/boards/updatewithoutattachment";
		}
		$.ajax({
			type : "POST",
			contentType : "application/json; charset=UTF-8",
			data : formData,
			processData : false,
			contentType : false,
			url : urlStr,
			success : function(result) {
				if (result.result == "success") {
					alert("보드 수정 완료");
					location.href = "/assignment/";
				} else {
					alert(result.result);
				}
			},
			error : function(xhr, status, error) {
				alert(error);
			}
		});
	
	});
</script>
</html>