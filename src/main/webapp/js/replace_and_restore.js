/**
 * 문자열을 변경하거나 복구하는 역할
 * @author khh
 */

/**
 * 치환을 담당하는 클래스 입니다.
 * @param {Array<RegularExp>} regularExpression 치환할 정규식 객체의 리스트
 * @param {Boolean} isHaveReplaceChar 치환을 문자로 할지 정합니다.(안할시 치환되지 않고 삭제됨)
 * @param {String} text1 비교할 텍스트1 입니다.
 * @param {String} text2 비교할 텍스트2 입니다.
 */
function Replace(regularExpressionArr, isHaveReplaceChar, text1, text2) {
	this._regularExpArr = regularExpressionArr;
	this._replacedChar = isHaveReplaceChar;
	this._replaceChar;
	this.taskQueue = new Array();
	this._text = [text1, text2];
	this._replacedText = ['',''];
	this._textMatch = [[], []];
}

Replace.prototype.getTextMatchArr = function() {
	return this._textMatch;
}

Replace.prototype.getTextArr = function() {
	return this._replacedText;
}

Replace.prototype.getReplacedChar = function() {
	return this._replaceChar;
}

/**
 * 중복되지 않는 랜덤한 텍스트를 반환합니다
 * @param {Array<String>} redundancyCheckList 중복 검사를 할 텍스트 리스트
 * @return {Character} 중복되지 않는 랜덤한 문자
 */
Replace.prototype.getRandomReplaceChar = function(redundancyCheckList, redundancyRegExpList) {
	const MAX_UTF_16_CODE = 65535;
	while(true) {
		var char = String.fromCharCode(Math.floor(Math.random() * MAX_UTF_16_CODE));
		if(char == '\r' || char == '\n') {
			continue;
		}
		var i = 0;
		for(i = 0; i < redundancyCheckList.length; i++) {
			const ele = redundancyCheckList[i];
			if(ele.indexOf(char) != -1) {
				continue;
			}
		}
		for(i = 0; i < redundancyRegExpList.length; i++) {
			const regExp = redundancyCheckList[i];
			var found = char.match(regExp);
			if(found != null && found.length != 0) {
				continue;
			}
		}
		return char;
	}
	throw "charCode할당 실패";
}

Replace.prototype.doQueueTask = function(thisPtr) {
	window.queueCnt = 1;	// 다른 곳의 taskQueue를 멈춥니다. (상호배제)
	if(thisPtr.taskQueue.length > 0) {
		if(window.queueCnt == 1) {
			var task = thisPtr.taskQueue.shift();
			task(thisPtr);
		}
		window.setTimeout(thisPtr.doQueueTask.bind(null, thisPtr), 10);
	} else {
		window.queueCnt = 0;
	}
}

Replace.prototype._isRegExpAllDone = function(matchRtnArr) {
	for(var i = 0; i < matchRtnArr.length; i++) {
		if(matchRtnArr[i] !== null) {
			return false;
		}
	}
	return true;
}

/**
 * 
 * @param {Replace} thisPtr
 * @param {Array<RegExpMachRtn>} matchArr nullable
 * @param {Number} replaceBeginPos 치환을 시작할 위치
 * @param {Number} replaceDiffLength 치환하면서 원본과 어긋난 길이의 값
 * @param {Number} textIdx 왼쪽이면 0, 오른쪽으면 1
 */
Replace.prototype._doReplaceTask = function(thisPtr, matchArr, replaceBeginPos, replaceDiffLength, textIdx) {
	window.queueCnt = 2;
	const MAX_LOOP_CNT = 100;
	var loopCnt = 0;
	
	const curAreaText = thisPtr._text[textIdx];
	
	var RegularExpArr = thisPtr._regularExpArr;
	for(var i = 0; i < RegularExpArr.length; i++) {
		var regExp = RegularExpArr[i];
		matchArr[i] = regExp.exec(curAreaText);
	}
	
	if(!thisPtr._isRegExpAllDone(matchArr)) {
		while(true) {
			var minMatchIdx = 0;
			for(var i = 1; i < RegularExpArr.length; i++) {
				const minReg = matchArr[minMatchIdx];
				if( !minReg ||
					(matchArr[i] && minReg.index > matchArr[i].index) ){
					minMatchIdx = i;
				}
			}
			
			const minReg = matchArr[minMatchIdx];
			
			const appendStr = curAreaText.substr(replaceBeginPos, minReg.index - replaceBeginPos);
			thisPtr._replacedText[textIdx] += appendStr;
			thisPtr._replacedText[textIdx] += thisPtr._replaceChar;
			
			replaceBeginPos = minReg.index + minReg[0].length;
			
			// 복원을 위한 위치 보정
			minReg.index -= replaceDiffLength;
			replaceDiffLength += (minReg[0].length - thisPtr._replaceChar.length);
			thisPtr._textMatch[textIdx].push(minReg);
			// 복원을 위한 위치 보정 끝
			
			matchArr[minMatchIdx] = RegularExpArr[minMatchIdx].exec(curAreaText);
			
			if(!matchArr[minMatchIdx]) {
				if(thisPtr._isRegExpAllDone(matchArr)) {
					break;
				}
			}
			
			loopCnt++;
			if(loopCnt > MAX_LOOP_CNT) {
				window.setTimeout(thisPtr._doReplaceTask(thisPtr, matchArr, replaceBeginPos, replaceDiffLength, textIdx), 10);
				return;
			}
		}
	}
	
	// 치환하고 남은 텍스트를 넣습니다.
	var remainText = curAreaText.substr(replaceBeginPos);
	thisPtr._replacedText[textIdx] += remainText;
	
	window.queueCnt = 1;
}

/*
 * 치환하는 작업의 함수를 queue에 넣어서 실행합니다.
 */
Replace.prototype.doReplaceAsync = function() {
	if(this._replacedChar) {
		this._replaceChar = this.getRandomReplaceChar(this._text, this._regularExpArr);
	} else {
		this._replaceChar = '';
	}
	
	const LEFT_TEXT_AREA_NUM = 0;
	const RIGHT_TEXT_AREA_NUM = 1;
	
	const makeFunc = function getReplaceTaskFunc(matchArr, textIdx) {
		return function replaceOneSection(thisPtr) {
			thisPtr._doReplaceTask(thisPtr, matchArr, 0, 0, textIdx);
		}
	}
	this.taskQueue.push(makeFunc([], LEFT_TEXT_AREA_NUM));
	
	this.taskQueue.push(makeFunc([], RIGHT_TEXT_AREA_NUM));
	
	this.doQueueTask(this);
}

/**
 * 주어진 정규식에 해당되는 문자들을 같은 문자로 치환합니다.
 * 치환하여 text1Match, text2Match에 보관합니다.
 * @param {String} text1 텍스트 내용입니다.
 * @return {!Array.<String>} 치환된 내용들입니다.
 */
Replace.prototype.doReplace = function(text1, text2) {
	if(this._replacedChar) {
		this._replaceChar = this.getRandomReplaceChar([text1, text2]);
	} else {
		this._replaceChar = '';
	}
	
	var replaceDiffLength = 0;	// 치환하면서 어긋난 위치 보정값

	var match, matchRight;
	var leftRegularExp = this._regularExp;
	while(( match = leftRegularExp.exec(text1)) !== null) {
		match.index -= replaceDiffLength;
		replaceDiffLength += (match[0].length - this._replaceChar.length);
		this._text1Match.push(match);
	}
	text1 = text1.replace(this._regularExp, this._replaceChar);
	
	var rightRegularExp = this._regularExp;
	replaceDiffLength = 0;
	while(( matchRight = rightRegularExp.exec(text2)) !== null) {
		matchRight.index -= replaceDiffLength;
		replaceDiffLength += matchRight[0].length - this._replaceChar.length;
		this._text2Match.push(matchRight);
	}
	text2 = text2.replace(this._regularExp, this._replaceChar);
	
	return [text1, text2];
}

function Restore(text1Match, text2Match, diffs, replacedChar) {
	this._text1Match = text1Match;
	this._text2Match = text2Match;
	this._diffs = diffs;
	this._diffsIdx;
	this._replacedChar = replacedChar;
}

Restore.prototype.doRestore = function() {
	if(this._replacedChar.length == 0) {
		return this._resotreWithoutReplaceChar();
	} else {
		return this._restoreWithReplaceChar();
	}
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
Restore.prototype._isHaveChangePos = function(idx, matchArr, startPos, diffStr) {
	if(idx < matchArr.length &&
		(startPos <= matchArr[idx].index &&
		matchArr[idx].index < startPos + diffStr.length)) {
		return true;
	} else {
		return false;
	}
}

/**
 * 이 함수 뒤에 matchIdx 를 1증가 시켜야 합니다.
 * curTextLength += return val;
 * diffs는 변경됩니다.
 * @param {!Array.<RegularExpMatchResult>} matchArr 치환하기 전의 값들이 들어 있는 배열입니다.
 * @param {Number} matchIdx matchArr의 idx 입니다.
 * @param {Number} curTextLength 현재 치환 진행중인 위치를 나타냅니다.
 * @param {Number} insertDiffStatus 비교 변환 결과 값이 들어 옵니다. (같다, 삭제, 삽입)
 * @return {Number} 현재 블럭에서 치환이 진행된 위치를 반환합니다. 
 */
Restore.prototype._restoreWithReplaceCharHelper = function(matchArr, matchIdx, curTextLength) {
	var diffStatus = this._diffs[this._diffsIdx][0];
	var diffStr = this._diffs[this._diffsIdx][1];
	
	this._diffs.splice(this._diffsIdx, 1);
	var matchBlock = matchArr[matchIdx];
	var replacePos = matchBlock.index - curTextLength;
	if(replacePos !== 0) {
		var beforeReplaceStr = diffStr.substr(0, replacePos);
		this._diffs.splice(this._diffsIdx++, 0, [ diffStatus, beforeReplaceStr ]);
	}
	
	this._diffs.splice(this._diffsIdx++, 0, [ diffStatus, matchBlock[0] ]);
	
	var lastReplaceBeginPos = replacePos + this._replacedChar.length;
	if(lastReplaceBeginPos !== diffStr.length) {
		var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
		this._diffs.splice(this._diffsIdx, 0, [ diffStatus, afterReplacedStr ]);
	}
	
	return lastReplaceBeginPos;
}

/**
 * 정규식으로 치환된 diffs가 들어옵니다.
 * 정규식 치환한 것을 다시 복원하는 작업을 합니다.
 * @return {!Array.<!diff_match_patch.Diff>} 복원된 diff입니다.
 */
Restore.prototype._restoreWithReplaceChar = function() {
	var curTextLengths = [0, 0];	// 현재 문서의 위치
	var text1MatchIdx = 0;
	var text2MatchIdx = 0;
	
	var leftMatchIdx = 0;
	var rightMatchIdx = 0;
	
	for (this._diffsIdx = 0; this._diffsIdx < this._diffs.length; this._diffsIdx++) {
		var diffStatus = this._diffs[this._diffsIdx][0];
		var diffStr = this._diffs[this._diffsIdx][1];
		
		if(	text1MatchIdx >= this._text1Match.length &&
			text2MatchIdx >= this._text2Match.length) {
			break;
		}
		
		if(diffStatus == window.DIFF_EQUAL){ 
			if(	!(this._isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr) ||
				this._isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr) )) {
				curTextLengths[0] += diffStr.length;
				curTextLengths[1] += diffStr.length;
				continue;
			} else {
				// 한개의 블럭에 대한 것이므로 블럭을 다시 만듦.
				this._diffs.splice(this._diffsIdx, 1);
			
				// 왼쪽 , 오른쪽 블럭이 같은 것을 치환하는 것이라는 가정입니다.
				// 먼저 왼쪽 것이 대상입니다.
				if(text1MatchIdx >= this._text1Match.length) {
					throw "left Match Idx out of length";
				}
				var replacePos = this._text1Match[text1MatchIdx].index - curTextLengths[0];
				if(replacePos !== 0) {
					var beforeReplaceStr = diffStr.substr(0, replacePos);
					this._diffs.splice(this._diffsIdx++, 0, [ diffStatus, beforeReplaceStr ]);
				}
				
				this._diffs.splice(this._diffsIdx++, 0, [ window.DIFF_DELETE * 2, this._text1Match[text1MatchIdx][0] ]);
				text1MatchIdx++;
				
				// 오른쪽 거를 치환합시다.
				if(text2MatchIdx >= this._text2Match.length) {
					throw "오른쪽을 치환 할 차례인데 더이상 배열에 없네요.";
				}
				
				this._diffs.splice(this._diffsIdx++, 0, [ window.DIFF_INSERT * 2, this._text2Match[text2MatchIdx][0] ]);
				text2MatchIdx++;
				
				var lastReplaceBeginPos = replacePos + this._replacedChar.length;
				if(lastReplaceBeginPos !== diffStr.length) {
					var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
					this._diffs.splice(this._diffsIdx, 0, [ diffStatus, afterReplacedStr ]);
				}
				
				curTextLengths[0] += lastReplaceBeginPos;
				curTextLengths[1] += this._text2Match[text2MatchIdx - 1].index - curTextLengths[1] + this._replacedChar.length;

				this._diffsIdx--; // 이전 블록을 꺼낸다.
			}
		}
		else if(diffStatus == window.DIFF_INSERT ||
				diffStatus == window.DIFF_INSERT * 2) {	// right 에 속하면
			if(!this._isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr)){
				curTextLengths[1] += diffStr.length;
				continue;
			} else {
				var length = this._restoreWithReplaceCharHelper(this._text2Match, text2MatchIdx, curTextLengths[1]);
				curTextLengths[1] += length;
				this._diffsIdx--;
				text2MatchIdx++;
			}
		} else if(diffStatus == window.DIFF_DELETE ||
					diffStatus == window.DIFF_DELETE * 2) {	// left 에 속하면
			if(!this._isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr)) {
				curTextLengths[0] += diffStr.length;
				continue;
			} else {
				var length = this._restoreWithReplaceCharHelper(this._text1Match, text1MatchIdx, curTextLengths[0]);
				curTextLengths[0] += length;
				this._diffsIdx--;
				text1MatchIdx++;
			}
		}
	}
	return this._diffs;
}

/**
 * 이 함수 뒤에 matchIdx 를 1증가 시켜야 합니다.
 * curTextLength += return val;
 * diffs는 변경됩니다.
 * @param {!Array.<RegularExpMatchResult>} matchArr 치환하기 전의 값들이 들어 있는 배열입니다.
 * @param {Number} matchIdx matchArr의 idx 입니다.
 * @param {Number} curTextLength 현재 치환 진행중인 위치를 나타냅니다.
 * @param {Number} insertDiffStatus 비교 변환 결과 값이 들어 옵니다. (같다, 삭제, 삽입)
 * @return {Number} 현재 블럭에서 치환이 진행된 위치를 반환합니다. 
 */
Restore.prototype._restoreWithoutReplaceCharHelper = function(matchArr, matchIdx, curTextLength, insertDiffStatus) {
	var diffStatus = this._diffs[this._diffsIdx][0];
	var diffStr = this._diffs[this._diffsIdx][1];
	
	this._diffs.splice(this._diffsIdx, 1);
	var matchBlock = matchArr[matchIdx];
	var replacePos = matchBlock.index - curTextLength;
	if(replacePos !== 0) {
		var beforeReplaceStr = diffStr.substr(0, replacePos);
		this._diffs.splice(this._diffsIdx++, 0, [ diffStatus, beforeReplaceStr ]);
	}
	
	if(Math.abs(insertDiffStatus) == 1) {
		insertDiffStatus *= 2;
	}

	this._diffs.splice(this._diffsIdx++, 0, [ insertDiffStatus, matchBlock[0] ]);
	
	var lastReplaceBeginPos = replacePos + this._replacedChar.length;
	if(lastReplaceBeginPos !== diffStr.length) {
		var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
		this._diffs.splice(this._diffsIdx, 0, [ diffStatus, afterReplacedStr ]);
	}
	
	return lastReplaceBeginPos;
}

/**
 * 정규식 치환된 diffs가 들어오고, this.text1Match, this.text2Match가 사용됩니다.
 * 정규식에 해당되는 것을 다시 복원하는 작업을 합니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples
 * @return {!Array.<!diff_match_patch.Diff>} 복원된 diff입니다.
 */
Restore.prototype._resotreWithoutReplaceChar = function() {
	var curTextLengths = [0, 0];	// 현재 문서의 위치
	var text1MatchIdx = 0;
	var text2MatchIdx = 0;
	
	var leftMatchIdx = 0;
	var rightMatchIdx = 0;
	
	for (this._diffsIdx = 0; this._diffsIdx < this._diffs.length; this._diffsIdx++) {
		var diffStatus = this._diffs[this._diffsIdx][0];
		var diffStr = this._diffs[this._diffsIdx][1];
		
		if(	text1MatchIdx >= this._text1Match.length &&
			text2MatchIdx >= this._text2Match.length) {
				break;
		}

		if(diffStatus == window.DIFF_EQUAL){ 
			if(	!(this._isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr) ||
				this._isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr)) ) {
				curTextLengths[0] += diffStr.length;
				curTextLengths[1] += diffStr.length;
			} else {
				// 범위 벗어나지 않았고, 왼쪽이 포함이면 일단 왼쪽
				// 범위 벗어나지 않았고, 오른쪽이 포함이면 일단 오른쪽
				// 단 둘다 포함이면 가까운 쪽부터 하자.
				var isLeftInside = this._isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr);
				var isRightInside = this._isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr);
				var isLeftTurn = false;
				if(isLeftInside && isRightInside) {
					isLeftTurn = 	this._text1Match[text1MatchIdx].index - curTextLengths[0] <
									this._text2Match[text2MatchIdx].index - curTextLengths[1];
				} else if(isLeftInside) {
					isLeftTurn = true;
				} else if(isRightInside) {
					isLeftTurn = false;
				}
				if(isLeftTurn){
					var length = this._restoreWithoutReplaceCharHelper(this._text1Match, text1MatchIdx, curTextLengths[0], window.DIFF_DELETE * 2);
					curTextLengths[0] += length;
					curTextLengths[1] += length;
					this._diffsIdx--;		// 이전 블럭을 꺼낸다.
					text1MatchIdx++;
				} else {
					var length = this._restoreWithoutReplaceCharHelper(this._text2Match, text2MatchIdx, curTextLengths[1], window.DIFF_INSERT * 2);
					curTextLengths[0] += length;
					curTextLengths[1] += length;
					this._diffsIdx--;
					text2MatchIdx++;
				}
			}
		} else if(diffStatus == window.DIFF_INSERT ||
				diffStatus == window.DIFF_INSERT * 2) {	// right 에 속하면
			if(!this._isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr)){
				curTextLengths[1] += diffStr.length;
			} else {
				var length = this._restoreWithoutReplaceCharHelper(this._text2Match, text2MatchIdx, curTextLengths[1], diffStatus);
				curTextLengths[1] += length;
				this._diffsIdx--;
				text2MatchIdx++;
			}
		} else if(diffStatus == window.DIFF_DELETE ||
					diffStatus == window.DIFF_DELETE * 2) {	// left 에 속하면
			if(!this._isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr)) {
				curTextLengths[0] += diffStr.length;
			} else {
				var length = this._restoreWithoutReplaceCharHelper(this._text1Match, text1MatchIdx, curTextLengths[0], diffStatus);
				curTextLengths[0] += length;
				this._diffsIdx--;
				text1MatchIdx++;
			}
		}
	}
	return this._diffs;
}