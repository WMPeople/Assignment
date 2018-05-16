/**
 * diff_match_custom 에서 diff_match_patch 를 호출합니다.
 * 커스텀을 위한 어뎁터 클래스로 볼수도 있습니다.
 * 
 * @author khh
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
 * @param {String} 왼쪽 결과를 보여줄 selector입니다.
 * @param {String} 오른쪽 결과를 보여줄 selector입니다.
 */
function DiffMatchCustom(Diff_Timeout, Diff_EditCost, Diff_IgnoreCase, text1, text2, leftOutputSelector, rightOutputSelector) {
	this.dmp = new diff_match_patch();	
	this.dmp.Diff_Timeout = parseFloat(Diff_Timeout);
	this.dmp.Diff_EditCost = parseFloat(Diff_EditCost);
	this.dmp.Diff_Sensitive = Diff_IgnoreCase;
	this.text1 = text1;
	this.text2 = text2;
	this.originText2 = text2;
	this.ms_start;
	this._replaceStack = new Array();
	this.taskQueue = new Array();
	this.diffRtn;
	this.leftOutputSelector = leftOutputSelector;
	this.rightOutputSelector = rightOutputSelector;
	this.replace;
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
DiffMatchCustom.prototype._markToEqualDiff = function(diffs, curIdx, diffStatus, diffStr, matchResult, whiteCharRegularExp) {
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
 * diff 결과에서 공백 문자를 같다고 수정합니다.
 * @param{!Array.<!diff_match_patch.Diff>} diffs diff알고리즘 결과
 * @param {Number} leastRepeatCnt 개행, 공백, 탭 옵션용입니다. 이 갯수 이상부터 같다고 판단합니다.
 * @return{!Array.<!diff_match_patch.Diff>} 공백이 무시된 결과
 */
DiffMatchCustom.prototype._ignoreWhiteCharater = function(diffs, leastRepeatCnt) {
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
			var rtn = this._markToEqualDiff(diffs, i, diffStatus, diffStr, matchResult, whiteCharRegularExp);
			i = rtn[0];
			diffs = rtn[1];
		}
	}
	
	return diffs;
}

/**
 * 공백 문자를 무시할 정규식을 반환합니다.
 * @param {Number} leastRepeatCnt 개행, 공백, 탭 옵션용입니다. 이 개수 이상부터 같다고 판단합니다.
 */
DiffMatchCustom.prototype.getWhiteCharacterRegExp = function(leastRepeatCnt) {
	var r = leastRepeatCnt;
	var whiteCharRegularExp = '(\t{' + r + ',}|\n{' + r + ',}|\r{' + r + ',}|\ {' + r + ',})';
	return new RegExp(whiteCharRegularExp, 'g');
}

/*
 * replace의 doReplaceWithTask는 비동기 실행 함수입니다.
 * 따라서 그 결과를 가져오기 위해서 task를 나누어야 합니다.
 */
/**
 * @param {RegExp} regExp
 * @param {Boolean} withReplaceChar
 * @return {Replace}
 */
DiffMatchCustom.prototype.doReplace = function(regExp, withReplaceChar) {
	this.replace = new Replace(regExp, withReplaceChar, this.text1, this.text2);
	this.replace.doReplaceAsync();

	return this.replace;
}


DiffMatchCustom.prototype.applyTextAndPush = function(replace) {
	var matchRtn = replace.getTextArr();
	this.text1 = matchRtn[0];
	this.text2 = matchRtn[1];
	
	this._replaceStack.push(replace);
}

DiffMatchCustom.prototype.restoreWhiteSpace = function(diffs, text1Match, text2Match) {
	var replacedChar = '';
	var restore = new Restore(text1Match, text2Match, diffs, '');
	return restore.doRestore();
}

DiffMatchCustom.prototype.restoreRegularExp = function(diffs, text1Match, text2Match) {
	var replacedChar = '\0';	
	var restore = new Restore(text1Match, text2Match, diffs, replacedChar);
	return restore.doRestore();
}

/***
 * @param {cleanupOpt} cleanupOption diff 알고리즘의 옵션입니다. cleanupOpt의 sementic, efficiency, no 중에 설정 가능합니다.
 * @param {Number} ignoreWhiteCharCnt 개수 이상으로 반복되는 공백문자를 무시합니다. (0이면 실행하지 않습니다.)
 * @param {Number} isWhitespaceFirst 공백 및 개행 무시 우선이면 true, 정규식 무시 우선이면 false 입니다.
 */
DiffMatchCustom.prototype.startAsync = function(cleanupOption, ignoreWhiteCharCnt, regularExpArr, isWhitespaceFirst) {
	this.ms_start = (new Date()).getTime();
	
	// 변경 무시 옵션에 따른 무시 사전 필터 시작
	if(typeof ignoreWhiteCharCnt != 'undefined' &&
			ignoreWhiteCharCnt > 0 &&
			isWhitespaceFirst) {
		this.taskQueue.push(function whiteChar(thisPtr) {
			var regExp = thisPtr.getWhiteCharacterRegExp(ignoreWhiteCharCnt);
			thisPtr.doReplace(regExp, false);
		});
		this.taskQueue.push(function applyTextAndPush(thisPtr) {
			thisPtr.applyTextAndPush(thisPtr.replace);
		});
	}
	if(typeof regularExpArr != 'undefined') {
		for(var i = 0; i < regularExpArr.length; i++) {
			var pushEle = function (regExp) {
				return function (thisPtr) {
					thisPtr.doReplace(regExp, true);
				}
			}
			this.taskQueue.push(pushEle(regularExpArr[i]));
			this.taskQueue.push(function applyTextAndPush(thisPtr) {
				thisPtr.applyTextAndPush(thisPtr.replace);
			});
		}
	}
	// 변경 무시 옵션에 따른 사전 필터 끝
	
	this.taskQueue.push(function doDiffMain(thisPtr) {
		thisPtr.diffRtn = thisPtr.dmp.diff_main(thisPtr.text1, thisPtr.text2);
	});
	
	this.taskQueue.push(function cleanupTask(thisPtr) {
		switch(cleanupOption) {
		case cleanupOpt.sementicCleanup:
			thisPtr.dmp.diff_cleanupSemantic(thisPtr.diffRtn);
			break;
			
		case cleanupOpt.efficiencyCleanup:
			thisPtr.dmp.diff_cleanupEfficiency(thisPtr.diffRtn);
			break;
		
		case cleanupOpt.noCleanup:
		default:
				break;
		}
	});
	
	// 변경 무시 옵션에 따른 사전 필터 복원 시작
	if(typeof regularExpArr != 'undefined') {
		for(var i = 0; i < regularExpArr.length; i++) {
			this.taskQueue.push(function recoverRegExp(thisPtr) {
				var replace = thisPtr._replaceStack.pop();
				var matchArr = replace.getTextMatchArr();
				thisPtr.diffRtn = thisPtr.restoreRegularExp(thisPtr.diffRtn, matchArr[0], matchArr[1]);
			});
		}
	}
	if(typeof ignoreWhiteCharCnt != 'undefined' &&
			ignoreWhiteCharCnt > 0 &&
			isWhitespaceFirst) {
		this.taskQueue.push(function recoverWhiteSpace(thisPtr) {
			var replace = thisPtr._replaceStack.pop();
			var matchArr = replace.getTextMatchArr();
			thisPtr.diffRtn = thisPtr.restoreWhiteSpace(thisPtr.diffRtn, matchArr[0], matchArr[1]);
		});
	}
	// 사전 필터 복원 끝

	// 공백문자 후처리 필터
	if(typeof ignoreWhiteCharCnt != 'undefined' &&
			ignoreWhiteCharCnt > 0 &&
			!isWhitespaceFirst) {
		this.taskQueue.push(function ignoreWhiteCharacter(thisPtr) {
			thisPtr.diffRtn = thisPtr._ignoreWhiteCharater(thisPtr.diffRtn, ignoreWhiteCharCnt);
		});
	}
	// 공백문자 후처리 필터 끝

	this.taskQueue.push(function prettyAndPrint(thisPtr) {
		var ds = thisPtr.dmp.diff_prettyHtml(thisPtr.diffRtn, '', thisPtr.originText2);
		
		var ms_end = (new Date()).getTime();
		console.log("전체 소요 시간 : " + (ms_end - thisPtr.ms_start) / 1000);
		
		document.getElementById(thisPtr.leftOutputSelector).innerHTML = ds[0];
		document.getElementById(thisPtr.rightOutputSelector).innerHTML = ds[1];
	});
	
	progressBar.setTotalCnt(this.taskQueue.length);
	this.doTask(this);
}

var progressBar;
var queueCnt = 0;	// 다른 곳에서 쓰면 다른 값. 다른 곳은 1, 
DiffMatchCustom.prototype.doTask = function(thisPtr) {
	if(thisPtr.taskQueue.length > 0) {
		if(queueCnt == 0) {
			var task = thisPtr.taskQueue.shift();
			task(thisPtr);
			
			progressBar.increseProgress(1, task.name);
		}
		window.setTimeout(thisPtr.doTask.bind(null, thisPtr), 100);
	}
}

var diffMatchCustom;
function launch() {
	var text1 = document.getElementById('text1').value;
	var text2 = document.getElementById('text2').value;
	var caseSensitive = $('#caseSensitive').is(':checked');
	var regularExpListChildren = $('#regularList').children();
	var ignoreWhiteCharCnt = $("#ignoreWhiteCharCnt").val();
	var whiteCharFirst = $('#whiteCharPriorityOpt').prop("checked");
	var regExpFirst = $('#regularExpPriorityOpt').prop("checked");
	var isWhiteCharFirst;
	if(whiteCharFirst) {
		isWhiteCharFirst = true;
	} else if(regExpFirst) {
		isWhiteCharFirst = false;
	}
	progressBar = new ProgressBar();

	diffMatchCustom = new DiffMatchCustom(2, 4, caseSensitive, text1, text2, 'outputdivLeft', 'outputdivRight');
	var ds;
	if(regularExpListChildren.length != 0){
		var regularExpArr = [];
		for(var i = 0; i < regularExpListChildren.length; i++) {
			var regularExp = regularExpListChildren.find('#regularExp')[i].value;
			var regularExpOpt = regularExpListChildren.find("#regularExpOpt")[i].value;
			var re = new RegExp(regularExp, regularExpOpt);
			regularExpArr.push(re);
		}
		diffMatchCustom.startAsync(cleanupOpt.efficiencyCleanup, ignoreWhiteCharCnt, regularExpArr, isWhiteCharFirst);
	} else {
		diffMatchCustom.startAsync(cleanupOpt.efficiencyCleanup, ignoreWhiteCharCnt, [], false);
	}
}

// ================= 이벤트 핸들러를 위한 함수들 시작 ====================

function chagnePriorityDisabled() {
	var regularList = document.getElementById("regularList");
	var ignoreWhiteSpaceCnt = document.getElementById("ignoreWhiteCharCnt").value;
	
	var priorityOpt = document.getElementById("whiteCharPriorityOpt");
	var priorityOpt2 = document.getElementById("regularExpPriorityOpt");

	if(regularList.children.length != 0 && parseInt(ignoreWhiteSpaceCnt, 10) > 0) {
		priorityOpt.disabled = false;
		priorityOpt2.disabled = false;
	} else {
		priorityOpt.disabled = true;
		priorityOpt2.disabled = true;
	}
}

function add_item() {
	var span = document.createElement('span');
	span.innerHTML = document.getElementById('pre_set').innerHTML;
    document.getElementById('regularList').appendChild(span);
    chagnePriorityDisabled();
}

function remove_item(obj) {
	document.getElementById('regularList').removeChild(obj.parentNode);
	chagnePriorityDisabled();
}

function isNumeric(n) {
	  return !isNaN(parseFloat(n)) && isFinite(n);
}

function isInt(n) {
	   return n % 1 === 0;
}

function onIgnoreWhiteSpaceChagned() {
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

window.onload = function() {
	chagnePriorityDisabled();
}
// ===================== 이벤트 핸들러를 위한 함수들 끝 =======================