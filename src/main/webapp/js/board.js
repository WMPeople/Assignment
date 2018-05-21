$(function(){
	// content에 70만자 까지만 들어가도록 하는 메쏘드
	function textCheck(){
		//textarea 70만자 제한
		var textCountLimit = 700000;
		$('textarea[name=content]').keyup(function() {
		    // 텍스트영역의 길이를 체크
		    var textLength = $(this).val().length;
		    // 입력된 텍스트 길이를 #textCount 에 업데이트 해줌
		    $('#textCount').text(textLength);
		    // 제한된 길이보다 입력된 길이가 큰 경우 제한 길이만큼만 자르고 텍스트영역에 넣음
		    if (textLength > textCountLimit) {
		    	alert("Warning : 70만자 제한 ");
		        $(this).val($(this).val().substr(0, textCountLimit));
		    }
		});
	}
	textCheck();
	
	// 5MB 이하 파일만 업로드 할 수 있게 하는 메쏘드
	
	$("#btnCreate").click(function(){
		var availableFile = fileCheck(this.form.fileUp);
		if (!availableFile) {
			return;
		}
		
		if ($('#subject').val() == '' || $('#content').val() == '') {
			 alert("제목과 내용을 입력하세요.");
			return;
		}

		var formData = new FormData($("#fileForm")[0]);
	    $.ajax({
	        type: "post",
	        contentType: false,
	        processData: false,
	        url: "/assignment/boards",
	        data: formData,
	        success: function(result){
	        	if(result.result == "success"){
	        		alertModal("생성 완료",false);
	               	location.href = "/assignment/";
	        	}
	        	else{
	        		alert('글 생성 실패');
	        	}
	        },
	    	error : function(xhr, status, error) {
	    		alert('글 생성 실패');
	    	}
	    });
	});

	$("#btnUpdate").click(function(){
		if ($('#subject').val() == '' || $('#content').val() == '') {
	        alert("제목과 내용을 입력하세요.");
	        return;
	    }
		
		if (this.form.fileUp != null) {
			 var availableFile = fileCheck(this.form.fileUp);
			    if (!availableFile) {
			        return;
			    }
		}
	
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
						alertModal("수정 완료", false);
						location.href = "/assignment/";
					} else {
						alert('글 수정 실패');
					}
				},
				error : function(xhr, status, error) {
					alert('글 수정 실패');
				}
			});
		
		});
	if($('#span_fileSize')[0] != undefined){
		$('#span_fileSize')[0].innerText= changeFileSize($('#span_fileSize')[0].innerText);
	}

});

function btnDelete(board_id,version){
	createConfirmModal("삭제하시겠습니까?");
	$('.ui.large.basic.confirm.modal').modal({
	    onApprove : function() {
	    	 $.ajax({
			        type: "DELETE",
			        url: "/assignment/boards/"+board_id+"/"+version,
			        success: function(result){
			        	if(result.result == 'success'){
			        		location.href = "/assignment/";
			        	}
			        	else{
			        		alert('글 삭제 실패');
			        	}
			        },
			        error : function(xhr, status, error) {
			        	alert('글 삭제 실패');
			    	}
			    })	
	    },
	    closable : false
	  }).modal('show');
};

function fileCheck(file){
    var maxSize  = 5 * 1024 * 1024    //5MB
    var fileSize = 0;

    var browser=navigator.appName;

    // 익스플로러
    if (browser=="Microsoft Internet Explorer") {
        var oas = new ActiveXObject("Scripting.FileSystemObject");
        fileSize = oas.getFile( file.value ).size;
    } else{ // 익스플로러가 아닐경우
    	if (file.files[0] == null || file.files[0] == undefined) {
    		return true;
    	}else {
    		fileSize = file.files[0].size;
    	}

    }

    if(fileSize > maxSize) {
        alert("첨부파일 사이즈는 5MB 이내로 등록 가능합니다.    ");
        return false;
    }
    return true;
}

function goPage(pages, lines) {
	pages = Math.ceil(pages);
    location.href = '?' + "pages=" + pages;
}

function changeFileSize(fileSize) {
	if (fileSize == 0 || fileSize == null || fileSize == undefined) {
		return 0;
	}
    var s = ['bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];
    var e = Math.floor(Math.log(fileSize) / Math.log(1024));
    var transformedFileSize = (fileSize / Math.pow(1024, e)).toFixed(2) + " " + s[e];
    return transformedFileSize;
}

function createConfirmModal (message) {
	
	var cofirmDiv = $('#confirm_modal');
	if (!cofirmDiv.length) {
		cofirmDiv = parent.document.createElement("div");
		cofirmDiv.id = "confirm_modal";
		parent.document.getElementsByTagName("body")[0].appendChild(cofirmDiv);
	}
	
	cofirmDiv.innerHTML = '<div class="ui large basic confirm modal">'
	+'<div class="ui icon header">'
		+'<i class="archive icon"></i>'
		+message
		+'</div>'
		+'<div class="actions">'
		+'<div class="ui green ok inverted button">OK</div>'
		+' <div class="ui red basic cancel inverted button">Cancel</div>'
		+'</div>'	
		+'</div>';
	
	
}

function alertModal(message, avialableCancel, image) {
	if (image == undefined) {
		image = 'heart';}
		var alertDiv = $('#alert_modal');
		if (!alertDiv.length) {
			alertDiv = parent.document.createElement("div");
			alertDiv.id = "alert_modal";
			parent.document.getElementsByTagName("body")[0].appendChild(alertDiv);
		}
	var htmlStr = '';
	htmlStr += '<div id="modalAlert" class="ui large basic alert modal"><div class="ui icon header"><i class="'+image+' icon"></i>' +message +'</div>';
	// avialableCancel가 true면 동작한다. 취소 버튼을 만들어준다.
	if (avialableCancel){
		htmlStr +=  '<div class="actions"><div class="ui red ok inverted button">Cancel</div></div>';
	}
	htmlStr +='</div>';
	$(alertDiv).html(htmlStr);
	$('#modalAlert').modal({
	    closable : false,
	    onApprove : function() {
	    },
	    silent: false
	}).modal('show');
}