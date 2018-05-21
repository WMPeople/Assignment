$(function(){
	fileUpOnChange();
	
    $("#fileUpdate").click(function(){
        document.getElementById('op_file').innerHTML='<input type="file" id="fileUp" name="fileUp" />'
        formData = new FormData($("#fileForm")[0]);
        $.ajax({
            type : "POST",
            contentType : "application/json; charset=UTF-8",
            data : formData,
            processData : false,
            contentType : false,
            url : "/assignment/autos/autosavewithfile",
            success : function(result) {
                if (result.result == 'success') {
                } else {
                    alert("자동 저장 실패");
                }
            },
            error : function(xhr, status, error) {
            	alert("자동 저장 실패");
            }
        });
        
        fileUpOnChange();
    });
    
});

function fileUpOnChange () {
    $('#fileUp').on('change' , function(){ 
        formData = new FormData($("#fileForm")[0]);
        if (!checkFile(formData.get("fileUp"))){
        	return;
        }
        $.ajax({
            type : "POST",
            contentType : "application/json; charset=UTF-8",
            data : formData,
            processData : false,
            contentType : false,
            url : "/assignment/autos/autosavewithfile",
            success : function(result) {
                if (result.result == 'success') {
                } else {
                	alert("자동 저장 실패");
                }
            },
            error : function(xhr, status, error) {
            	alert("자동 저장 실패");
            }
        });
    });
}

function checkFile(file){
    var maxSize  = 5 * 1024 * 1024    //5MB
    var fileSize = 0;
    var browser=navigator.appName;
    if (browser=="Microsoft Internet Explorer") {
        var oas = new ActiveXObject("Scripting.FileSystemObject");
        fileSize = oas.getFile( file.value ).size;
    } else{
    	if (file == null || file == undefined) {
    		return true;
    	}else {
    		fileSize = file.size;
    	}
    }
    if(fileSize > maxSize) {
        alert("첨부파일 사이즈는 5MB 이내로 등록 가능합니다.    ");
        return false;
    }
    return true;
}