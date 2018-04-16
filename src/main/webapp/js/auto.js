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