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
	var replacedChar = '';
	var r = leastRepeatCnt;
	var whiteCharRegularExp = '(\t{' + r + ',}|\n{' + r + ',}|\r{' + r + ',}|\ {' + r + ',})';
	var re = new RegExp(whiteCharRegularExp, 'g');
	var rtn = this.preFilter(re, replacedChar);
	
	this.whiteSpaceText1Match = rtn[0];
	this.whiteSpaceText2Match = rtn[1];
}

DiffMatchCustom.prototype.cleanupRegularExp = function(regularExp, regularExpOpt) {
	var replacedChar = '\0';
	var re = new RegExp(regularExp, regularExpOpt);
	var rtn = this.preFilter(re, replacedChar);
	
	this.text1Match = rtn[0];
	this.text2Match = rtn[1];
}

/**
 * 중복되지 않는 랜덤한 텍스트를 반환합니다
 * @param {String} text1 중복되지 않을 텍스트의 대상
 * @param {String} text2 중복되지 않을 텍스트의 대상
 * @return {Character} 중복되지 않는 랜덤한 문자
 */
DiffMatchCustom.prototype.getRandomReplaceChar = function(text1, text2) {
	const MAX_UTF_16_CODE = 65535;
	while(true) {
		var char = String.fromCharCode(Math.floor(Math.random() * MAX_UTF_16_CODE)));
		if(	text1.indexOf(char) == -1 &&
			text2.indexOf(char) == -1) {
			return char;
		}
	}
	throw "charCode할당 실패";
}

/**
 * 주어진 정규식에 해당되는 문자들을 같은 문자로 치환합니다.
 * 치환하여 text1Match, text2Match에 보관합니다.
 * @param {RegExp} 정규식 객체가 들어옵니다.
 */
DiffMatchCustom.prototype.preFilter = function(regularExp, replacedChar) {
	const MAX_REGULAR_EXP_CNT = 10000;
	var cnt = 0;
	text1Match = [];
	text2Match = [];

	var match, matchRight;
	var leftRegularExp = regularExp;
	while(( match = leftRegularExp.exec(this.text1)) !== null) {
		text1Match.push(match);
		this.text1 = this.text1.substr(0, match.index) + replacedChar + this.text1.substr(match.index + match[0].length);
		leftRegularExp.lastIndex -= match[0].length;
		cnt++;
		if(cnt > MAX_REGULAR_EXP_CNT) {
			alert("정규식에 해당되는것이 너무 많습니다.\n정규식을 확인바랍니다.");
			return;
		}
	}
	var rightRegularExp = regularExp;
	while(( matchRight = rightRegularExp.exec(this.text2)) !== null) {
		text2Match.push(matchRight);
		this.text2 = this.text2.substr(0, matchRight.index) + replacedChar + this.text2.substr(matchRight.index + matchRight[0].length);
		rightRegularExp.lastIndex -= matchRight[0].length;
		cnt++;
		if(cnt > MAX_REGULAR_EXP_CNT) {
			alert("정규식에 해당되는것이 너무 많습니다.\n정규식을 확인바랍니다.");
			return;
		}
	}
	
	return [text1Match, text2Match];
}

DiffMatchCustom.prototype.restoreWhiteSpace = function(diffs) {
	var replacedChar = '';
	return this.restoreWhiteSpaceFilter(diffs, this.whiteSpaceText1Match, this.whiteSpaceText2Match, replacedChar);
}

DiffMatchCustom.prototype.restoreRegularExp = function(diffs) {
	var replacedChar = '\0';	
	return this.restoreRegExpFilter(diffs, this.text1Match, this.text2Match, replacedChar);
}

/**
 * 변경할 것이 현재 위치에 있는가?
 * idx가 배열 크기를 벗어나지 않으면서, diff 블럭의 시작 위치 <= 변경점 < 블럭 끝나는 위치 인 경우
 * @param {Number} idx 검사할 배열의 인덱스 (배열 범위 초과를 검사 및 현재 배열 값 확인)
 * @param {!Array.<RegularExpMatchResult>} matchArr 정규식 결과가 담긴 배열 입니다.
 * @param {Number} startPos diff 블럭의 시작 위치
 * @param {String} diffStr diff 블럭의 변경 내용 (블럭 끝나는 위치를 알기 위함)
 * @return {Boolean}
 */
DiffMatchCustom.prototype.isHaveChangePos = function(idx, matchArr, startPos, diffStr) {
	if(idx < matchArr.length &&
		(startPos <= matchArr[idx].index &&
		matchArr[idx].index < startPos + diffStr.length)) {
		return true;
	} else {
		return false;
	}
}

/**
 * 정규식으로 치환된 diffs가 들어옵니다.
 * 정규식 치환한 것을 다시 복원하는 작업을 합니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples
 * @param {!Array.<RegularExpMatchResult>} text1Match 치환되기 전의 문자열이 저장되어 있는 배열입니다.
 * @param {!Array.<RegularExpMatchResult>} text2Match 치환되기 전의 문자열이 저장되어 있는 배열입니다.
 * @param {String} replacedChar 치환된 문자입니다. (길이만 사용합니다. 문자 비교는 하지 않습니다.)
 * @return {!Array.<!diff_match_patch.Diff>} 복원된 diff입니다.
 */
DiffMatchCustom.prototype.restoreRegExpFilter = function(diffs, text1Match, text2Match, replacedChar) {
	var curTextLengths = [0, 0];	// 현재 문서의 위치
	var text1MatchIdx = 0;
	var text2MatchIdx = 0;
	
	var leftMatchIdx = 0;
	var rightMatchIdx = 0;
	
	if(this.isPreFilterOn !== true){
		return diffs;
	}
	
	for (var i = 0; i < diffs.length; i++) {
		var diffStatus = diffs[i][0];
		var diffStr = diffs[i][1];
		
		if(	text1MatchIdx >= text1Match.length &&
			text2MatchIdx >= text2Match.length) {
			break;
		}
		
		if(diffStatus == window.DIFF_EQUAL){ 
			if(	!(this.isHaveChangePos(text1MatchIdx, text1Match, curTextLengths[0], diffStr) ||
				this.isHaveChangePos(text2MatchIdx, text2Match, curTextLengths[1], diffStr) )) {
				curTextLengths[0] += diffStr.length;
				curTextLengths[1] += diffStr.length;
				continue;
			} else {
				// 한개의 블럭에 대한 것이므로 블럭을 다시 만듦.
				diffs.splice(i, 1);
			
				// 왼쪽 , 오른쪽 블럭이 같은 것을 치환하는 것이라는 가정입니다.
				// 먼저 왼쪽 것이 대상입니다.
				if(text1MatchIdx >= text1Match.length) {
					throw "left Match Idx out of length";
				}
				var replacePos = text1Match[text1MatchIdx].index - curTextLengths[0];
				if(replacePos !== 0) {
					var beforeReplaceStr = diffStr.substr(0, replacePos);
					diffs.splice(i++, 0, [ diffStatus, beforeReplaceStr ]);
				}
				
				diffs.splice(i++, 0, [ window.DIFF_DELETE * 2, text1Match[text1MatchIdx][0] ]);
				text1MatchIdx++;
				
				// 오른쪽 거를 치환합시다.
				if(text2MatchIdx >= text2Match.length) {
					throw "오른쪽을 치환 할 차례인데 더이상 배열에 없네요.";
				}
				
				diffs.splice(i++, 0, [ window.DIFF_INSERT * 2, text2Match[text2MatchIdx][0] ]);
				text2MatchIdx++;
				
				var lastReplaceBeginPos = replacePos + replacedChar.length;
				if(lastReplaceBeginPos !== diffStr.length) {
					var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
					diffs.splice(i, 0, [ diffStatus, afterReplacedStr ]);
				}
				
				curTextLengths[0] += lastReplaceBeginPos;
				curTextLengths[1] += text2Match[text2MatchIdx - 1].index - curTextLengths[1] + replacedChar.length;

				i--; // 이전 블록을 꺼낸다.
			}
		}
		else if(diffStatus == window.DIFF_INSERT ||
				diffStatus == window.DIFF_INSERT * 2) {	// right 에 속하면
			if(!this.isHaveChangePos(text2MatchIdx, text2Match, curTextLengths[1], diffStr)){
				curTextLengths[1] += diffStr.length;
				continue;
			} else {
				diffs.splice(i, 1);
				var matchBlock = text2Match[text2MatchIdx];
				text2MatchIdx++;
				var replacePos = matchBlock.index - curTextLengths[1];
				if(replacePos !== 0) {
					var beforeReplaceStr = diffStr.substr(0, replacePos);
					diffs.splice(i++, 0, [ diffStatus, beforeReplaceStr ]);
				}
				
				diffs.splice(i++, 0, [ diffStatus, matchBlock[0] ]);
				
				var lastReplaceBeginPos = replacePos + replacedChar.length;
				if(lastReplaceBeginPos !== diffStr.length) {
					var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
					diffs.splice(i, 0, [ diffStatus, afterReplacedStr ]);
				}
				
				curTextLengths[1] += lastReplaceBeginPos;

				i--; // 이전 블록을 꺼낸다.
			}
		} else if(diffStatus == window.DIFF_DELETE ||
					diffStatus == window.DIFF_DELETE * 2) {	// left 에 속하면
			if(!this.isHaveChangePos(text1MatchIdx, text1Match, curTextLengths[0], diffStr)) {
				curTextLengths[0] += diffStr.length;
				continue;
			} else {
				diffs.splice(i, 1);
				var matchBlock = text1Match[text1MatchIdx];
				text1MatchIdx++;
				var replacePos = matchBlock.index - curTextLengths[0];
				if(replacePos !== 0) {
					var beforeReplaceStr = diffStr.substr(0, replacePos);
					diffs.splice(i++, 0, [ diffStatus, beforeReplaceStr ]);
				}
				
				diffs.splice(i++, 0, [ diffStatus, matchBlock[0] ]);
				
				var lastReplaceBeginPos = replacePos + replacedChar.length;
				if(lastReplaceBeginPos !== diffStr.length) {
					var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
					diffs.splice(i, 0, [ diffStatus, afterReplacedStr ]);
				}

				curTextLengths[0] += lastReplaceBeginPos;

				i--; // 이전 블록을 꺼낸다.
			}
		}
	}
	return diffs;
}

/**
 * 이 함수 뒤에 matchIdx 를 1증가 시켜야 합니다.
 * curTextLength += return val;
 * diffs는 변경됩니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs 치환된 diff 결과들의 배열입니다.
 * @param {Number} diffsIdx diffs의 idx 입니다.
 * @param {!Array.<RegularExpMatchResult>} matchArr 치환하기 전의 값들이 들어 있는 배열입니다.
 * @param {Number} matchIdx matchArr의 idx 입니다.
 * @param {String} replacedChar 치환된 문자열입니다. (길이만 사용됩니다.)
 * @param {Number} curTextLength 현재 치환 진행중인 위치를 나타냅니다.
 * @param {Number} insertDiffStatus 비교 변환 결과 값이 들어 옵니다. (같다, 삭제, 삽입)
 * @return {!Array.{Number, Number}} 증가된 diffsIdx와 현재 블럭에서 치환이 진행된 위치를 반환합니다. 
 */
DiffMatchCustom.prototype.restore = function(diffs, diffsIdx, matchArr, matchIdx, replacedChar, curTextLength, insertDiffStatus) {
	var diffStatus = diffs[diffsIdx][0];
	var diffStr = diffs[diffsIdx][1];
	
	diffs.splice(diffsIdx, 1);
	var matchBlock = matchArr[matchIdx];
	var replacePos = matchBlock.index - curTextLength;
	if(replacePos !== 0) {
		var beforeReplaceStr = diffStr.substr(0, replacePos);
		diffs.splice(diffsIdx++, 0, [ diffStatus, beforeReplaceStr ]);
	}
	
	if(Math.abs(insertDiffStatus) == 1) {
		insertDiffStatus *= 2;
	}

	diffs.splice(diffsIdx++, 0, [ insertDiffStatus, matchBlock[0] ]);
	
	var lastReplaceBeginPos = replacePos + replacedChar.length;
	if(lastReplaceBeginPos !== diffStr.length) {
		var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
		diffs.splice(diffsIdx, 0, [ diffStatus, afterReplacedStr ]);
	}
	
	return [diffsIdx, lastReplaceBeginPos];
}

/**
 * 정규식 치환된 diffs가 들어오고, this.text1Match, this.text2Match가 사용됩니다.
 * 정규식에 해당되는 것을 다시 복원하는 작업을 합니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples
 * @return {!Array.<!diff_match_patch.Diff>} 복원된 diff입니다.
 */
DiffMatchCustom.prototype.restoreWhiteSpaceFilter = function(diffs, text1Match, text2Match, replacedChar) {
	var curTextLengths = [0, 0];	// 현재 문서의 위치
	var text1MatchIdx = 0;
	var text2MatchIdx = 0;
	
	var leftMatchIdx = 0;
	var rightMatchIdx = 0;
	
	if(this.isPreFilterOn !== true){
		return diffs;
	}
	
	for (var i = 0; i < diffs.length; i++) {
		var diffStatus = diffs[i][0];
		var diffStr = diffs[i][1];
		
		if(	text1MatchIdx >= text1Match.length &&
			text2MatchIdx >= text2Match.length) {
				break;
		}

		if(diffStatus == window.DIFF_EQUAL){ 
			if(	!(this.isHaveChangePos(text1MatchIdx, text1Match, curTextLengths[0], diffStr) ||
				this.isHaveChangePos(text2MatchIdx, text2Match, curTextLengths[1], diffStr)) ) {
				curTextLengths[0] += diffStr.length;
				curTextLengths[1] += diffStr.length;
			} else {
				// 범위 벗어나지 않았고, 왼쪽이 포함이면 일단 왼쪽
				// 범위 벗어나지 않았고, 오른쪽이 포함이면 일단 오른쪽
				// 단 둘다 포함이면 가까운 쪽부터 하자.
				var isLeftInside = this.isHaveChangePos(text1MatchIdx, text1Match, curTextLengths[0], diffStr);
				var isRightInside = this.isHaveChangePos(text2MatchIdx, text2Match, curTextLengths[1], diffStr);
				var isLeftTurn = false;
				if(isLeftInside && isRightInside) {
					isLeftTurn = 	text1Match[text1MatchIdx].index - curTextLengths[0] <
									text2Match[text2MatchIdx].index - curTextLengths[1];
				} else if(isLeftInside) {
					isLeftTurn = true;
				} else if(isRightInside) {
					isLeftTurn = false;
				}
				if(isLeftTurn){
					var diffLen = this.restore(diffs, i, text1Match, text1MatchIdx, replacedChar, curTextLengths[0], window.DIFF_DELETE * 2);
					curTextLengths[0] += diffLen[1];
					curTextLengths[1] += diffLen[1];
					i = diffLen[0];
					i--;		// 이전 블럭을 꺼낸다.
					text1MatchIdx++;
				} else {
					var diffLen = this.restore(diffs, i, text2Match, text2MatchIdx, replacedChar, curTextLengths[1], window.DIFF_INSERT * 2);
					curTextLengths[0] += diffLen[1];
					curTextLengths[1] += diffLen[1];
					i = diffLen[0];
					i--;
					text2MatchIdx++;
				}
			}
		} else if(diffStatus == window.DIFF_INSERT ||
				diffStatus == window.DIFF_INSERT * 2) {	// right 에 속하면
			if(!this.isHaveChangePos(text2MatchIdx, text2Match, curTextLengths[1], diffStr)){
				curTextLengths[1] += diffStr.length;
			} else {
				var rtn = this.restore(diffs, i, text2Match, text2MatchIdx, replacedChar, curTextLengths[1], diffStatus);
				curTextLengths[1] += rtn[1];
				i = rtn[0];
				i--;
				text2MatchIdx++;
			}
		} else if(diffStatus == window.DIFF_DELETE ||
					diffStatus == window.DIFF_DELETE * 2) {	// left 에 속하면
			if(!this.isHaveChangePos(text1MatchIdx, text1Match, curTextLengths[0], diffStr)) {
				curTextLengths[0] += diffStr.length;
			} else {
				var rtn = this.restore(diffs, i, text1Match, text1MatchIdx, replacedChar, curTextLengths[0], diffStatus);
				curTextLengths[0] += rtn[1];
				i = rtn[0];
				i--;
				text1MatchIdx++;
			}
		}
	}
	return diffs;
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