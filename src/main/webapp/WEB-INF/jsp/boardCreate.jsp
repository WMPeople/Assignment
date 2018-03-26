<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
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


 <form id="fileForm" action="fileUpload" method="post" enctype="multipart/form-data">
<div id="articleEditor" >
	<div class="board_write">
		<h2 class="tit">�۾���</h2>
		<ul class="option _info">
			<li class="opt_sbj">
				<h3 class="tx">����</h3>
				<span class="wrap_sbj">
					<input type="text" name="subject" id="subject" class="itxt subject _title"  value="">
				</span>
			</li>

			<li class="opt_file">
				<h3 class="tx">����÷��</h3>
				<div class="_file_btn" style="width: 500; overflow: hidden; position: relative;">
					<div class="browse-box _article_attach_btn_back" >
						 <input type="file" id="fileUp" name="fileUp"/>
					</div>
				</div>
			</li>
		</ul>
		
		<textarea  name="content" id="content" style="min-height: 500px; min-weight: 500px;" ></textarea>
<!--      	<input type="text" name="content" id="content" class="itxt content _title"> -->


		<div class="btn_area _btn_area">
<!-- 			<p class="next"> -->
<!-- 				<button type="button" class="btn _cancel">�ۼ����</button> -->
<!-- 				<button type="button" class="btn _atc_delete">����</button> -->
<!-- 			</p> -->
<!-- 			<button type="button" class="btn _temp">�ӽ�����</button> -->
<!-- 			<button type="button" class="btn _preview">�̸�����</button> -->

			 <button type="button" id= "btnCreate"class="btn tx_point _save"><strong>Ȯ��</strong></button>
		</div>
	</div>
</div>
</form>
</body>
<script>
$(document).ready(function(){
    
    $("#btnCreate").click(function(){
    	var formData = new FormData($("#fileForm")[0]);
    	console.log(fileUp);
        $.ajax({                
            type: "post",
            contentType: false,
            processData: false,
            url: "/Assignment/boardCreate",
            data: formData, fileUp,
            success: function(){
            	
                alert("���� ���� �Ϸ�");
                location.href = "/Assignment/boardList";
            }
        	,
        	error : function(xhr, status, error) {
        		console.log(error);
        		

        	}
        });
        
        
    });
    


    
});
</script>
</html>