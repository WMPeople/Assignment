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
       
        var tempContent = textVal;
        
    	var dmp = new diff_match_patch();
    	function launch() {
    		var originalContent = document.getElementById('content').defaultValue;
    		dmp.Diff_Timeout = parseFloat(2);
			dmp.Diff_EditCost = parseFloat(4);
			dmp.Diff_Sensitive = 0;
			
			var diff = dmp.diff_main(originalContent,tempContent);
			dmp.diff_cleanupSemantic(diff);

			var diffLength = diff.length;
			var diffCount = 0;
			var minorCount = 0;
			var nextVal =0;
			var originalContentLength = originalContent.length;
			
			// 문자열 비교를 위한 크기 값을 오리지널 게시물의 문자열 크기에 반비례 하게 넣어준다.
			if (originalContentLength < 50) {
				originalContentLength = originalContentLength / 1.3;
			} else if (originalContentLength <100 ) {
				originalContentLength = originalContentLength / 1.2;
			} else if (originalContentLength < 200) { 
				originalContentLength = originalContentLength / 1.1 
				}
			
			var tempContentLength = tempContent.length;
			var stringSizeDifference = Math.abs(originalContentLength - tempContentLength) / originalContentLength ;
			for (var i = 0 ; i < diff.length ; i++){
				if (diff[i][0] != 0){
					if (diff[i][1].length >=3) {
						diffCount++;
					} else {
						minorCount++;
					}
					while (diff[i][1].search('\n')!= -1){
						nextVal++;
						diff[i][1] = diff[i][1].substring(diff[i][1].search('\n')+2);
						
					}
				}
			}
			console.log(diff);
			console.log('사소한 변경을 제외한 diff 크기 : '+ (diffLength - minorCount) + ' , 8 까지'); // 배열사이즈
			console.log('-1, 1 개수 : '+ diffCount+ ' , 17 까지'); //-1 ,1 개수. 단 , 사소한 수정은 제외
			console.log('개행 수 : '+ nextVal+ ' , 7까지'); // 개행 개수
			console.log('o-t/o : '+ stringSizeDifference+ ' , 1.5까지'); // 문자열 변화량 / 오리지날 문자열 크기
			console.log('사소한 변경 개수 : '+ minorCount+ ' '); //짧게 변한 것은 제외하기 위한 카운트
			
			//버전업 조건
			if ((diffLength - minorCount) >= 8 || diffCount >= 17 || nextVal >= 7 || stringSizeDifference >= 1.5) {
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
								alert('board_id : ' + result.updatedBoard.board_id + ' version : ' + result.updatedBoard.version + ' 으로 버전업 되었습니다. ');
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