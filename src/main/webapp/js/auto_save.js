/**
 * 2018-04-19 10:12 류원상 현재 자동 저장 형태 .
 * 
 * 삭제 된 것은 생각하지 않고 추가만 수정만 고려하여 버전업을 합니다.
 * 
 * 단, 자동 저장시에는 textarea에 적혀 있는 내용 그대로 저장됩니다.
 * 
 * 사용자가 게시글의 일정부분을 지우고 저장하지 않고 종료했다면
 * 
 * 자동 저장 게시글에는 일정 부분이 삭제된 내용을 확인 할 수 있고,
 * 
 * 리프 노드에서는 삭제 되기 전 내용을 확인 할 수 있습니다
 */
//기본 유의미함 판단 조건
var addCountCondition = 6;
var newLineCountCondition = 6;
var stringSizeDifferenceCondition = -0.8;
var addLengthCondition = 200;
	
$(function() {
	$("#autoSaveChkBox").change(function() {
		var autoSaveChkBox = $('#autoSaveChkBox').is(':checked');
		// 체크되어 있지 않으면 보이지 않음
		if (!autoSaveChkBox) {
			document.getElementById('conditionList').innerHTML='';
			return;
		} else {
			optionAutoSave();
		}
	});
});

function optionAutoSave() {

	
	//자동 저장 옵션 체크시 판단 조건이 보인다.
	var span = document.createElement('span');
	span.innerHTML = document.getElementById('pre_set').innerHTML;
	document.getElementById('conditionList').appendChild(span);
	$('#addCountCondition').val(addCountCondition);
	$('#newLineCountCondition').val(newLineCountCondition);
	$('#stringSizeDifferenceCondition').val(stringSizeDifferenceCondition);
	$('#addLengthCondition').val(addLengthCondition);
	
	var autoSave = new Object();
	(function(obj) {
		obj.configuration = {
			interval : 10
		// 시간 간격
		};
		obj.bindTimer = function() {
			var autoSaveChkBox = $('#autoSaveChkBox').is(':checked');
			if (!autoSaveChkBox) {
				return;
			}
			oEditors.getById["content"].exec("UPDATE_CONTENTS_FIELD", []);
			var textEle = document.querySelector('#content');
			var textVal = textEle.value;
			var ref1, ref2 // 전 데이터, 전전 데이터

			// localStorage에 저장
			var encodedTextVal = btoa(unescape(encodeURIComponent(textVal)));
			ref1 = window.localStorage.getItem('textval-01'); // 전
			ref2 = window.localStorage.getItem('textval-02'); // 전전

			if ((window.localStorage) && (encodedTextVal != ref1)) {
				window.localStorage.setItem('textval-01', encodedTextVal);
				window.localStorage.setItem('textval-02', ref1);
				window.localStorage.setItem('textval-03', ref2);

				var tempContent = textVal;

				var dmp = new diff_match_patch();
				function launch() {
					var originalContent = document.getElementById('content').defaultValue; // 원본
					// 내용
					console.log(originalContent);
					dmp.Diff_Timeout = parseFloat(2);
					dmp.Diff_EditCost = parseFloat(4);
					dmp.Diff_Sensitive = false;
					var diff = dmp.diff_main(originalContent, tempContent);
					/**
					 * diff 구조는 길이가 2인 배열을 인덱스로 갖는 배열이다.
					 * [Array(2),Array(2),Array(2),Array(2)...]
					 * 
					 * Array(2)의 0번째 인덱스는 -1, 0, 1의 값을 갖는데 -1은 삭제, 0은 일치, 1은 추가를
					 * 뜻한다. 1번째 인덱스에는 0번째 인덱스의 값에 따른 text가 들어간다.
					 */

					dmp.diff_cleanupSemantic(diff);

					var addCount = 0; // 추가된 배열 카운트. 단, 배열 길이가 3이상인 것만 카운트한다. 즉, 사소한 추가는 제외.
					var addLength = 0; // 추가된 내용의 길이
					var minorCount = 0; // 사소한 변경 카운트
					var newLineCount = 0; // 개행 개수 카운트
					var originalContentLength = originalContent.length; // 원본 문자열 길이
					
					// 원본 문자열 길이가 너무 짧으면 40이라고 생각한다. 너무 잦은 버전업을 막기 위해.
					if (originalContentLength <= 40) {
						originalContentLength = 40;
					}

					var tempContentLength = tempContent.length;
					var stringSizeDifference = (originalContentLength - tempContentLength)/ originalContentLength; // 문자열 크기
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
								diff[i][1] = diff[i][1].substring(diff[i][1]
										.search('\n') + 2);

							}
						}
					}
					console.log(diff);
					console.log(' 1 개수 : ' + addCount + ' , '+addCountCondition+'까지'); // addCount 새롭게 추가된 부분(수정된 부분)을 카운트 한다.
					console.log('개행 수 : ' + newLineCount + ' , '+newLineCountCondition+'까지'); // newLineCount 개행이 추가된 부분을 카운트 한다.
					console.log('o-t/o : ' + stringSizeDifference
							+ ' , '+stringSizeDifferenceCondition+'까지'); // stringSizeDifference (원본 내용 - 현재 내용) / 원본내용 으로 원본내용이 적을 수록 수치가 올라간다. 문자열 크기에 반비례하도록 적용
					console.log('사소한 변경 개수 : ' + minorCount + ' ');
					console.log('추가된 문자열 길이 : ' + addLength + ' , '+addLengthCondition+'이상'); // 추가된 문자열의 길이를 뜻한다. diff 알고리즘 return 값에서 1의 길이의 합을 뜻한다.
					
					testFunction(addCount, newLineCount, stringSizeDifference,
							addLength, addCountCondition, newLineCountCondition, stringSizeDifferenceCondition, addLengthCondition, 1);
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
}
function testFunction(addCount, newLineCount, stringSizeDifference, addLength,
		data1, data2, data3, data4, urlData) {
	if (addCount >= data1 || newLineCount >= data2
			|| stringSizeDifference <= data3 || addLength >= data4) {
		var file_name = '<%=request.getParameter("file_name")%>';
		if (file_name != '') {
			file_name = '<%=request.getParameter("file_name")%>';
		}
		var formData = new FormData($("#fileForm")[0]);
		if (urlData == 1) {
			formData.set("board_id", $('#board_id').val());
			formData.set("version", $('#version').val());
		}

		var urlStr;
		// 원본 게시물에 파일이 있지만 수정하지 않았을 때
		if (file_name != null && $("#fileUp").val() == null) {
			urlStr = "/assignment/boards/updatemaintainattachment";
		} else {
			urlStr = "/assignment/boards/updatewithoutattachment";
		}
		$
				.ajax({
					type : "POST",
					contentType : "application/json; charset=UTF-8",
					data : formData,
					processData : false,
					contentType : false,
					url : urlStr,
					async : false,
					success : function(result) {
						if (result.result == "success") {
							if (urlData == 1) {
								$('#board_id')
										.val(result.updatedBoard.board_id);
								$('#version').val(result.updatedBoard.version);
								document.getElementById('content').defaultValue = document
										.getElementById('content').value;
								document.getElementById('currentBoard').innerText = 'board_id : '
										+ $('#board_id').val()
										+ ' version : '
										+ $('#version').val();
								document.getElementById('notice').innerText = new Date()
										.toGMTString()
										+ " 버전업 완료.";

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
		if (urlData == 1) {
			formData.set("board_id", $('#board_id').val());
			formData.set("version", $('#version').val());
			$
					.ajax({
						type : "POST",
						contentType : "application/json; charset=UTF-8",
						data : formData,
						processData : false,
						contentType : false,
						url : "/assignment/autos/autosavewithoutfile",
						success : function(result) {
							if (result.result == 'success') {
								document.getElementById('notice').innerText = new Date()
										.toGMTString()
										+ " 자동 저장 완료.";
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

}

function applyChange(){
	if(($('#addCountCondition').val() >= 1 && $('#addCountCondition').val() <= 100) &&
		($('#newLineCountCondition').val() >= 1 && $('#newLineCountCondition').val() <= 50) &&
		($('#stringSizeDifferenceCondition').val() >= -2 && $('#stringSizeDifferenceCondition').val() <= -0.2) &&
		($('#addLengthCondition').val() >= 100 && $('#addLengthCondition').val() <= 1000) ){
		
		addCountCondition = Math.floor($('#addCountCondition').val());
		newLineCountCondition =  Math.floor($('#newLineCountCondition').val());
		stringSizeDifferenceCondition = $('#stringSizeDifferenceCondition').val();
		addLengthCondition =  Math.floor($('#addLengthCondition').val());
		
	} else {
		alert('값을 확인해주세요');
	}
	$('#addCountCondition').val(addCountCondition);
	$('#newLineCountCondition').val(newLineCountCondition);
	$('#stringSizeDifferenceCondition').val(stringSizeDifferenceCondition);
	$('#addLengthCondition').val(addLengthCondition);
}
