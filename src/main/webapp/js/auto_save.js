$(document).ready(function(){

(function () {
  var count =0; // db 접근 횟수
  var autoSave = new Object();
  (function (obj) {
    obj.configuration = {
      interval: 10 // second(s)
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
			/**
			 * diff 구조는 길이가 2인 배열을 인덱스로 갖는 배열이다. [Array(2),Array(2),Array(2),Array(2)...]
			 *  
			 * Array(2)의 0번째 인덱스는 -1, 0, 1의 값을 갖는데 -1은 삭제, 0은 일치, 1은 추가를 뜻한다. 1번째 인덱스에는 0번째 인덱스의 값에 따른 text가 들어간다.
			 */
			
			dmp.diff_cleanupSemantic(diff);

			var diffLength = diff.length;
			var addAndDeleteCount = 0; // 추가된 배열과 삭제된 배열 카운트. 단, 배열 길이가 3이상인 것만 카운트한다. 즉, 사소한 변경은 제외.
			var minorCount = 0; // 사소한 변경 카운트
			var newLineCount =0; // 개행 개수 카운트
			var originalContentLength = originalContent.length; // 원본 내용
			
			// 문자열 비교를 위한 크기 값을 오리지널 게시물의 문자열 크기에 반비례 하게 넣어준다.
			if (originalContentLength < 50) {
				originalContentLength = originalContentLength / 1.3;
			} else if (originalContentLength <100 ) {
				originalContentLength = originalContentLength / 1.2;
			} else if (originalContentLength < 200) { 
				originalContentLength = originalContentLength / 1.1 
			}
			
			var tempContentLength = tempContent.length;
			var stringSizeDifference = Math.abs(originalContentLength - tempContentLength) / originalContentLength ; // 문자열 변화량 / 오리지날 문자열 크기
			for (var i = 0 ; i < diff.length ; i++){
				if (diff[i][0] != 0){
					if (diff[i][1].length >=3) {
						addAndDeleteCount++;
					} else {
						minorCount++;
					}
					while (diff[i][1].search('\n')!= -1){
						newLineCount++;
						diff[i][1] = diff[i][1].substring(diff[i][1].search('\n')+2);
						
					}
				}
			}
			console.log(diff);
			console.log('사소한 변경(minorCount)을 제외한 diff 크기 : '+ (diffLength - minorCount) + ' , 8 까지'); 
			console.log('-1, 1 개수 : '+ addAndDeleteCount+ ' , 17 까지');
			console.log('개행 수 : '+ newLineCount+ ' , 7까지'); 
			console.log('o-t/o : '+ stringSizeDifference+ ' , 1.5까지'); 
			console.log('사소한 변경 개수 : '+ minorCount+ ' '); 
			
			//버전업 조건
			if ((diffLength - minorCount) >= 8 || addAndDeleteCount >= 17 || newLineCount >= 7 || stringSizeDifference >= 1.5) {
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
								document.getElementById('currentBoard').innerText = 'board_id : ' + $('#board_id').val() + ' version : ' + $('#version').val();
								document.getElementById('notice').innerText =new Date().toGMTString() + " 버전업 완료.";
							} else {
								alert(result.result);
							}
						},
						error : function(xhr, status, error) {
							alert(error);
						}
					});

			} else {
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
								document.getElementById('notice').innerText =new Date().toGMTString() + " 자동 저장 완료.";
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
    	}
        launch();
      } else if (!window.localStorage) {
        console.log('Error' + ': Your browser not support')
        return false;
      }
    };

    obj.start = function() {
      setTimeout(function() {
    	obj.bindTimer();
        obj.start();
      }, obj.configuration.interval * 1000);
    };
    obj.start();
  })(autoSave);
})();

});