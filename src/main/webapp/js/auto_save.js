$(document).ready(function(){

(function () {
  var count =0; // db 접근 횟수
  var autoSave = new Object();
  (function (obj) {
    obj.configuration = {
      interval: 1 // second(s)
    };
    obj.bindTimer = function() {
      var textEle = document.querySelector('#content');
      var textVal = textEle.value;
      var ref1, ref2, ref3; // Newer -&gt; Older
      

      // Save to localStorage
      var encodedTextVal = btoa(unescape(encodeURIComponent(textVal)));
      ref1 = window.localStorage.getItem('textval-01');
      ref2 = window.localStorage.getItem('textval-02');

      if ((window.localStorage) && (encodedTextVal != ref1)){
        window.localStorage.setItem('textval-01', encodedTextVal);
        window.localStorage.setItem('textval-02', ref1);
        window.localStorage.setItem('textval-03', ref2);
        var originalContent = document.getElementById('content').defaultValue;
        var tempContent = textVal;
        
    	var dmp = new diff_match_patch();
    	function launch() {
    		dmp.Diff_Timeout = parseFloat(2);
			dmp.Diff_EditCost = parseFloat(4);
			dmp.Diff_Sensitive = 0;
			
			var diff = dmp.diff_main(originalContent,tempContent);
			dmp.diff_cleanupSemantic(diff);
			console.log(diff);
			var diffLength = diff.length;
			var diffCount = 0;
			var nextVal =0;
			for(var i = 0 ; i < diff.length ; i++){
				if(diff[i][0] != 0){
					diffCount++;
					
					while(diff[i][1].search('\n')!= -1){
						nextVal++;
						diff[i][1] = diff[i][1].substring(diff[i][1].search('\n')+2);
						
					}
				}
			}
			console.log(diffLength); // 배열사이즈
			console.log(diffCount); //-1 ,1 개수
			console.log(nextVal); // 개행 개수
			
			if (diffLength >=10 || diffCount >= 20 || nextVal >= 10) {
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
						async : false,
						success : function(result) {
							if (result.result == "success") {
								$('#board_id').val(result.updatedBoard.board_id);
								$('#version').val(result.updatedBoard.version);
								document.getElementById('content').defaultValue = document.getElementById('content').value;
								
								//여기서 템프 아티클을 하나 만들어 줘야한다.. 흠 ..
							} else {
								alert(result.result);
							}
						},
						error : function(xhr, status, error) {
							alert(error);
						}
					});

			}
			
    	}
        launch();
       
        
        
        var formData = new FormData($("#fileForm")[0]);
       
        formData = new FormData($("#fileForm")[0]);
		$.ajax({
			type : "POST",
			contentType : "application/json; charset=UTF-8",
			data : formData,
			processData : false,
			contentType : false,
			url : "/assignment/boards/autosavewithoutfile",
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
		 count++;
	     console.log(count);
      }
     
      else if (!window.localStorage) {
        console.log('Error' + ': Your browser not support')
        return false;
      }
    };

    obj.start = function() {
      obj.bindTimer();
      setTimeout(function() {
        obj.start();
      }, obj.configuration.interval * 1000);
    };
    obj.start();
  })(autoSave);
})();

});