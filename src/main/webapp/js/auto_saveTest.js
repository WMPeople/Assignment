/**
2018-04-19 10:12 류원상 현재 자동 저장 형태 . 

삭제 된 것은 생각하지 않고 추가만 수정만 고려하여 버전업을 합니다.

단, 자동 저장시에는 textarea에 적혀 있는 내용 그대로 저장됩니다. 

사용자가 게시글의 일정부분을 지우고 저장하지 않고 종료했다면

자동 저장 게시글에는 일정 부분이 삭제된 내용을 확인 할 수 있고, 

리프 노드에서는 삭제 되기 전 내용을 확인 할 수 있습니다
*/
$(function() {
	(function() {
		var autoSave = new Object();
		(function(obj) {
			obj.configuration = {
				interval : 10 // 시간 간격
			};
			obj.bindTimer = function() {
				var textEle = document.querySelector('#content');
				var textVal = textEle.value;
				var ref1,
					ref2 // 전 데이터, 전전 데이터


				// localStorage에 저장
				var encodedTextVal = btoa(unescape(encodeURIComponent(textVal)));
				ref1 = window.localStorage.getItem('textval-01'); // 전
				ref2 = window.localStorage.getItem('textval-02'); // 전전

				if ((window.localStorage) && (encodedTextVal != ref1)) {
					window.localStorage.setItem('textval-01', encodedTextVal);
					window.localStorage.setItem('textval-02', ref1);
					window.localStorage.setItem('textval-03', ref2);

					var tempContent = textVal;
					console.log('ref1 : ' + decodeURIComponent(escape(window.atob(ref1)))); // 전 'content'
					console.log('ref2 : ' + decodeURIComponent(escape(window.atob(ref2)))); // 전전 'content'
					console.log('encodedTextVal : ' + decodeURIComponent(escape(window.atob(encodedTextVal)))); // 현재 'content'

					var dmp = new diff_match_patch();
					function launch() {
						var originalContent = document.getElementById('content').defaultValue; //원본 내용
						console.log(originalContent);
						dmp.Diff_Timeout = parseFloat(2);
						dmp.Diff_EditCost = parseFloat(4);
						dmp.Diff_Sensitive = 0;
						var diff = dmp.diff_main(originalContent, tempContent);
						/**
						 * diff 구조는 길이가 2인 배열을 인덱스로 갖는 배열이다. [Array(2),Array(2),Array(2),Array(2)...]
						 *  
						 * Array(2)의 0번째 인덱스는 -1, 0, 1의 값을 갖는데 -1은 삭제, 0은 일치, 1은 추가를 뜻한다. 1번째 인덱스에는 0번째 인덱스의 값에 따른 text가 들어간다.
						 */

						dmp.diff_cleanupSemantic(diff);

						var addCount = 0; // 추가된 배열 카운트. 단, 배열 길이가 3이상인 것만 카운트한다. 즉, 사소한 추가는 제외.
						var addLength = 0; // 추가된 내용의 길이
						var minorCount = 0; // 사소한 변경 카운트
						var newLineCount = 0; // 개행 개수 카운트
						var originalContentLength = originalContent.length; // 원본 내용
						if (originalContentLength <= 40) {
							originalContentLength = 40;
						}

						var tempContentLength = tempContent.length;
						var stringSizeDifference = (originalContentLength - tempContentLength) / originalContentLength; // 문자열 변화량 / 오리지날 문자열 크기
						for (var i = 0; i < diff.length; i++) {
							if (diff[i][0] == 1) {
								if (diff[i][1].length >= 3) {
									addCount++;
								} else {
									minorCount++;
								}
								addLength += diff[i][1].length;
								while (diff[i][1].search('\n') != -1) {
									newLineCount++;
									diff[i][1] = diff[i][1].substring(diff[i][1].search('\n') + 2);

								}
							}
						}
						
						
						
						
						console.log(diff);
						console.log(' 1 개수 : ' + addCount );
						console.log('개행 수 : ' + newLineCount);
						console.log('o-t/o : ' + stringSizeDifference);
						console.log('사소한 변경 개수 : ' + minorCount);
						console.log('추가된 문자열 길이 : ' + addLength);
						//버전업 조건
						// addCount 새롭게 추가된 부분을 카운트 한다.
						// newLineCount 개행이 추가된 부분을 카운트 한다.
						// stringSizeDifference (원본 내용 - 현재 내용) / 원본내용   으로  원본내용이 적을 수록 수치가 올라간다. 문자열 크기에 반비례하도록  적용
						// addLength 원본 내용보다 추가된 문자열 길이
						
						
						
						//테스트
						testFunction(addCount, newLineCount, stringSizeDifference, addLength, 6, 5, -0.5, 300, 1);
						testFunction(addCount, newLineCount, stringSizeDifference, addLength, 6, 5, -1, 200, 2);
						testFunction(addCount, newLineCount, stringSizeDifference, addLength, 6, 5, -2, 400, 3);
						testFunction(addCount, newLineCount, stringSizeDifference, addLength, 10, 10, -3, 1000, 4);
//						testFunction(addCount, newLineCount, stringSizeDifference, addLength, 10, 7, -0.88, 200, 5);
						//						testFunction(addCount, newLineCount, stringSizeDifference, addLength, 9, 5, -0.5, 100,6);

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

function testFunction(addCount, newLineCount, stringSizeDifference, addLength, data1, data2, data3, data4, urlData) {
	if (addCount >= data1 || newLineCount >= data2 || stringSizeDifference <= data3 || addLength >= data4) {
		var file_name = '<%=request.getParameter("file_name")%>';
		if (file_name != '') {
			file_name = '<%=request.getParameter("file_name")%>';
		}
		var formData = new FormData($("#fileForm")[0]);
		//테스트
		if (urlData == 1 ) {
			formData.set("board_id",  $('#board_id').val());
			formData.set("version",  $('#version').val());
		}
		
		if (urlData == 2 ) {
			formData.set("board_id",  $('#board_id2').val());
			formData.set("version",  $('#version2').val());
		}
		
		if (urlData == 3 ) {
			formData.set("board_id",  $('#board_id3').val());
			formData.set("version",  $('#version3').val());
		}
		
		if (urlData == 4 ) {
			formData.set("board_id",  $('#board_id4').val());
			formData.set("version",  $('#version4').val());
		}
		
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
					//테스트
					if (urlData == 1){
						$('#board_id').val(result.updatedBoard.board_id);
						$('#version').val(result.updatedBoard.version);
						document.getElementById('content').defaultValue = document.getElementById('content').value;
						document.getElementById('currentBoard').innerText = 'board_id : ' + $('#board_id').val() + ' version : ' + $('#version').val();
						document.getElementById('notice').innerText = new Date().toGMTString() + " 버전업 완료.";
						
					}
					
					if (urlData == 2){
						$('#board_id2').val(result.updatedBoard.board_id);
						$('#version2').val(result.updatedBoard.version);
						document.getElementById('content').defaultValue = document.getElementById('content').value;
						document.getElementById('currentBoard').innerText = 'board_id : ' + $('#board_id2').val() + ' version : ' + $('#version2').val();
						document.getElementById('notice').innerText = new Date().toGMTString() + " 버전업 완료.";
						
					}
					
					if (urlData == 3){
						$('#board_id3').val(result.updatedBoard.board_id);
						$('#version3').val(result.updatedBoard.version);
						document.getElementById('content').defaultValue = document.getElementById('content').value;
						document.getElementById('currentBoard').innerText = 'board_id : ' + $('#board_id3').val() + ' version : ' + $('#version3').val();
						document.getElementById('notice').innerText = new Date().toGMTString() + " 버전업 완료.";
						
					}
					
					if (urlData == 4){
						$('#board_id4').val(result.updatedBoard.board_id);
						$('#version4').val(result.updatedBoard.version);
						document.getElementById('content').defaultValue = document.getElementById('content').value;
						document.getElementById('currentBoard').innerText = 'board_id : ' + $('#board_id4').val() + ' version : ' + $('#version4').val();
						document.getElementById('notice').innerText = new Date().toGMTString() + " 버전업 완료.";
						
					}
				} else {
					alert(result.result);
				}
			},
			error : function(xhr, status, error) {
				alert('버전업 실패');
			}
		});

	} else {
		var formData = new FormData($("#fileForm")[0]);

		formData = new FormData($("#fileForm")[0]);
		//테스트
		if (urlData == 1 ) {
			formData.set("board_id",  $('#board_id').val());
			formData.set("version",  $('#version').val());
		}
		
		if (urlData == 2 ) {
			formData.set("board_id",  $('#board_id2').val());
			formData.set("version",  $('#version2').val());
		}
		
		if (urlData == 3 ) {
			formData.set("board_id",  $('#board_id3').val());
			formData.set("version",  $('#version3').val());
		}
		
		if (urlData == 4 ) {
			formData.set("board_id",  $('#board_id4').val());
			formData.set("version",  $('#version4').val());
		}
		$.ajax({
			type : "POST",
			contentType : "application/json; charset=UTF-8",
			data : formData,
			processData : false,
			contentType : false,
			url : "/assignment/autos/autosavewithoutfile",
			success : function(result) {
				if (result.result == 'success') {
					document.getElementById('notice').innerText = new Date().toGMTString() + " 자동 저장 완료.";
				} else {
					alert("자동 저장 실패");
				}
			},
			error : function(xhr, status, error) {
				alert('자동 저장 실패');
			}
		});
	}


}