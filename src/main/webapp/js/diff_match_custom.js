/**
 * diff_match_custom 에서 diff_match_patch 를 호출합니다.
 * 커스텀을 위한 어뎁터 클래스로 볼수도 있습니다.
 * 
 * @author khh
 * @author rws
 */

const cleanupOpt = Object.freeze({
	"noCleanup" : 0,
	"sementicCleanup" : 1,
	"efficiencyCleanup" : 2
});

/***
 * @param {Number} Diff_Timeout diff알고리즘의 timout 시간. 시간초과시 결과가 부정확할 수 있습니다.
 * @param {Number} Diff_EditCost diff알고리즘의 diff_cleanupEfficiency에서 사용될 비용입니다.
 * @param {Boolean} Diff_IgnoreCase 대소문자를 구분할지 여부입니다.
 * @param {String} text1 비교할 왼쪽 문자입니다.
 * @param {String} text2 비교할 오른쪽 문자입니다.
 */
function DiffMatchCustom(Diff_Timeout, Diff_EditCost, Diff_IgnoreCase, text1, text2) {
	this.dmp = new diff_match_patch();	
	this.dmp.Diff_Timeout = parseFloat(Diff_Timeout);
	this.dmp.Diff_EditCost = parseFloat(Diff_EditCost);
	this.dmp.Diff_Sensitive = Diff_IgnoreCase;
	this.text1Match = [];	// [ text1MatchObj]
	this.text2Match = [];	// [ text2MatchObj]
	this.whiteSpaceText1Match = [];
	this.whiteSpaceText2Match = [];
	this.text1 = text1;
	this.text2 = text2;
	this.originText2 = text2;
	this.isPreFilterOn = true;
}

/**
 * 정규식에 해당되는 문자를 같은 문자열로 취급하게 합니다.
 * diff 결과를 수정합니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples
 * @param {Number} curIdx diffs배열에서 동작하고 있는 idx
 * @param {Number} diffStatus diff[][0]의 값
 * @param {String} diffStr diff[][1]의 값
 * @param {!Array} matchResult String.match의 리턴값
 * @param {String} whiteCharRegularExp 정규식 문자열
 * @return {!Array.<Number, !diff_match_patch.Diff>} Array of diff tuples.
 */
DiffMatchCustom.prototype.markToEqualDiff = function(diffs, curIdx, diffStatus, diffStr, matchResult, whiteCharRegularExp) {
	
	while (matchResult != null) {

		var firstSubstr;
		if (matchResult.index !== 0) {
			firstSubstr = diffStr.substr(0, matchResult.index);
			diffs.splice(curIdx++, 0, [ diffStatus, firstSubstr ]);
		}

		var middleSubstr = diffStr.substr(matchResult.index, matchResult[0].length);
		diffs.splice(curIdx++, 0, [ diffStatus * 2, middleSubstr ]);

		var endSubstrIndex = matchResult.index + matchResult[0].length;
		if (endSubstrIndex !== diffStr.length) {
			diffStr = diffStr.substr(endSubstrIndex, diffStr.length - endSubstrIndex);
			matchResult = diffStr.match(whiteCharRegularExp);	// 해당되는 것이 더 있는지 찾습니다.
			
			if(matchResult == null) {
				diffs.splice(curIdx, 0, [ diffStatus, diffStr ]);
				break;
			}
		} else {
			break;
		}
	}
	return [ curIdx, diffs ];
}

/**
 * 공백 문자를 무시합니다. diff 결과를 수정합니다.
 * @param{!Array.<!diff_match_patch.Diff>} diffs diff알고리즘 결과
 * @param {Number} leastRepeatCnt 개행, 공백, 탭 옵션용입니다. 이 갯수 이상부터 같다고 판단합니다.
 * @return{!Array.<!diff_match_patch.Diff>} 공백이 무시된 결과
 */
DiffMatchCustom.prototype.ignoreWhiteCharater = function(diffs, leastRepeatCnt) {
	var text2DiffLength = 0;
	const r = leastRepeatCnt;
	var whiteCharRegularExp = '(\t{' + r + ',}|\n{' + r + ',}|\r{' + r + ',}|\ {' + r + ',})';
	
	for(var i = 0; i < diffs.length; i++) {
		var diffStatus = diffs[i][0];
		var diffStr = diffs[i][1];

		if(	diffStatus == window.DIFF_INSERT ||
			diffStatus == window.DIFF_DELETE ) {

			var matchResult = diffStr.match(whiteCharRegularExp);
			if(matchResult == null) {
				continue;
			}
			diffs.splice(i, 1);
			var rtn = this.markToEqualDiff(diffs, i, diffStatus, diffStr, matchResult, whiteCharRegularExp);
			i = rtn[0];
			diffs = rtn[1];
		}
	}
	
	return diffs;
}

// TODO : 공백 제거 -> 정규식 제거 -> diff 알고리즘 -> 정규식 복구 -> 공백 복구
// TODO : 공백 복구시에는 replaceChar가 ''으로 되어야 하고, replaceLength = 0 이어야 합니다. 
// TODO : 정규식 복구와 공백 복구가 하나의 함수에서 처리가 가능할 것으로 보입니다.
/**
 * 공백 문자를 무시합니다.
 * @param {Number} leastRepeatCnt 개행, 공백, 탭 옵션용입니다. 이 개수 이상부터 같다고 판단합니다.
 */
DiffMatchCustom.prototype.cleanupWhiteCharater = function(leastRepeatCnt) {
	var r = leastRepeatCnt;
	var whiteCharRegularExp = '(\t{' + r + ',}|\n{' + r + ',}|\r{' + r + ',}|\ {' + r + ',})';
	var re = new RegExp(whiteCharRegularExp, 'g');
	var replace = new Replace(this.text1, this.text2, re, false);
	
	var matchRtn = replace.doReplace();
	this.text1 = replace.getText1();
	this.text2 = replace.getText2();
	
	this.whiteSpaceText1Match = matchRtn[0];
	this.whiteSpaceText2Match = matchRtn[1];
}

DiffMatchCustom.prototype.cleanupRegularExp = function(regularExp, regularExpOpt) {
	var re = new RegExp(regularExp, regularExpOpt);
	var replace = new Replace(this.text1, this.text2, re, true);
	
	var matchRtn = replace.doReplace();
	this.text1 = replace.getText1();
	this.text2 = replace.getText2();
	
	this.text1Match = matchRtn[0];
	this.text2Match = matchRtn[1];
}

DiffMatchCustom.prototype.restoreWhiteSpace = function(diffs) {
	var replacedChar = '';
	var restore = new Restore(this.whiteSpaceText1Match, this.whiteSpaceText2Match, diffs, '');
	return restore.doRestore();
}

DiffMatchCustom.prototype.restoreRegularExp = function(diffs) {
	var replacedChar = '\0';	
	var restore = new Restore(this.text1Match, this.text2Match, diffs, replacedChar);
	return restore.doRestore();
}

/***
 * @param text1 비교할 텍스트 1입니다.(보통 왼쪽)
 * @param text2 비교할 텍스트 2입니다.(보통 오른쪽)
 * @param cleanupOption diff 알고리즘의 옵션입니다. cleanupOpt의 sementic, efficiency, no 중에 설정 가능합니다.
 * @param {Number} whiteCharLeastCnt 개수 이상으로 반복되는 공백문자를 무시합니다. (0이면 실행하지 않습니다.)
 * @param {Number} priority 공백 및 개행 무시 우선이면 1, 정규식 무시 우선이면 0 입니다.
 * @return {!Array.<String>} 비교한 후의 html 입니다. 0이 왼쪽, 1이 오른쪽입니다.
 */
DiffMatchCustom.prototype.start = function(cleanupOption, ignoreWhiteCharCnt, regularExp, regularExpOpt, priority) {
	var ms_start = (new Date()).getTime();
	
	var text1Match, text2Match;
	// 변경 무시 옵션에 따른 무시 사전 필터 시작
	if(typeof ignoreWhiteCharCnt != 'undefined' &&
			ignoreWhiteCharCnt > 0 &&
			priority == 1) {
		this.cleanupWhiteCharater(ignoreWhiteCharCnt);
	}
	if(typeof regularExp != 'undefined') {
		this.cleanupRegularExp(regularExp, regularExpOpt);
	}
	// 사전 필터 끝
	
	var d1 = this.dmp.diff_main(this.text1, this.text2);
	
	// 결과를 clean up 합니다.
	switch(cleanupOption) {
	case cleanupOpt.sementicCleanup:
		this.dmp.diff_cleanupSemantic(d1);
		break;
		
	case cleanupOpt.efficiencyCleanup:
		this.dmp.diff_cleanupEfficiency(d1);
		break;
	
	case cleanupOpt.noCleanup:
	default:
			break;
	}

	// 변경 무시 옵션에 따른 사전 필터 복원 시작
	if(typeof regularExp != 'undefined') {
		d1 = this.restoreRegularExp(d1);
	}
	if(typeof ignoreWhiteCharCnt != 'undefined' &&
			ignoreWhiteCharCnt > 0 &&
			priority == 1) {
		d1 = this.restoreWhiteSpace(d1);
	}
	// 사전 필터 복원 끝

	// 공백문자 후처리 필터
	if(typeof ignoreWhiteCharCnt != 'undefined' &&
			ignoreWhiteCharCnt > 0 &&
			priority == 0) {
		d1 = this.ignoreWhiteCharater(d1, ignoreWhiteCharCnt);
	}

	var ds = this.dmp.diff_prettyHtml(d1, '', this.originText2);

	console.log(d1);

	var ms_end = (new Date()).getTime();
	console.log("전체 소요 시간 : " + (ms_end - ms_start) / 1000);
	
	return ds;
}

function launch() {
	var text1 = document.getElementById('text1').value;
	var text2 = document.getElementById('text2').value;
	var caseSensitive = $('#caseSensitive').is(':checked');
	var regularExpChkBox = $('#regularExpChkBox').is(':checked');
	var regularExp = $('#regularExp').val();
	var regularExpOpt = $("#regularExpOpt").val();
	var ignoreWhiteCharCnt = $("#ignoreWhiteCharCnt").val();
	var whiteCharPri = $('#whiteCharPriorityOpt').prop("checked");
	var regExpPri = $('#regularExpPriorityOpt').prop("checked");
	var pri;
	if(whiteCharPri) {
		pri = 1;
	} else if(regExpPri) {
		pri = 0;
	}

	var diffMatch = new DiffMatchCustom(2, 4, caseSensitive, text1, text2);
	var ds;
	if(regularExpChkBox){
		ds = diffMatch.start(cleanupOpt.efficiencyCleanup, ignoreWhiteCharCnt, regularExp, regularExpOpt, pri);
	} else {
		ds = diffMatch.start(cleanupOpt.efficiencyCleanup, ignoreWhiteCharCnt);
	}

	document.getElementById('outputdivLeft').innerHTML = ds[0];
	document.getElementById('outputdivRight').innerHTML = ds[1];
}

function chagnePriorityDisabled() {
	var regularChkBox = document.getElementById("regularExpChkBox");
	var ignoreWhiteSpaceCnt = document.getElementById("ignoreWhiteCharCnt").value;
	
	var priorityOpt = document.getElementById("whiteCharPriorityOpt");
	var priorityOpt2 = document.getElementById("regularExpPriorityOpt");

	if(regularChkBox.checked == true && parseInt(ignoreWhiteSpaceCnt, 10) > 0) {
		priorityOpt.disabled = false;
		priorityOpt2.disabled = false;
	} else {
		priorityOpt.disabled = true;
		priorityOpt2.disabled = true;
	}
}

function regularChkboxChanged(){
	var checkbox = document.getElementById("regularExpChkBox");
	var regularOptSpan = document.getElementById("regularOptSpan");
	
	if(checkbox.checked == true) {
		regularOptSpan.style.display = "block";
	} else {
		regularOptSpan.style.display = "none";
	}
	chagnePriorityDisabled();
}

function isNumeric(n) {
	  return !isNaN(parseFloat(n)) && isFinite(n);
}

function isInt(n) {
	   return n % 1 === 0;
}

function ignoreWhiteSpaceChagned() {
	var ignoreWhiteSpace = document.getElementById("ignoreWhiteCharCnt");
	if(!isNumeric(ignoreWhiteSpace.value)) {
		ignoreWhiteSpace.value = 0;
	} else if(ignoreWhiteSpace.value < 0) {
		ignrowWhiteSpace.value = 0;
	} else if(!(isInt(ignoreWhiteSpace.value))) {
		ignorwWhiteSpace.value = 0;
	}
	
	chagnePriorityDisabled();
}