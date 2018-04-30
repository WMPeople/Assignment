/**
 * 문자열을 변경하거나 복구하는 역할
 * @author khh
 */

function Replace(regularExpression, withReplaceChar, text1, text2) {
	this._text1Match = [];
	this._text2Match = [];
	this._regularExp = regularExpression;
	this._withReplaceChar = withReplaceChar;
	this._replaceChar;
	this.taskQueue = new Array();
	this._text = [text1, text2];
	this._textMatch = [[], []];
}

Replace.prototype.getTextMatchArr = function() {
	return this._textMatch;
}

Replace.prototype.getTextArr = function() {
	return this._text;
}

Replace.prototype.getReplacedChar = function() {
	return this._replaceChar;
}

/**
 * 중복되지 않는 랜덤한 텍스트를 반환합니다
 * @param {String} text1 중복되지 않을 텍스트의 대상
 * @param {String} text2 중복되지 않을 텍스트의 대상
 * @return {Character} 중복되지 않는 랜덤한 문자
 */
Replace.prototype.getRandomReplaceChar = function(text1, text2) {
	const MAX_UTF_16_CODE = 65535;
	while(true) {
		var char = String.fromCharCode(Math.floor(Math.random() * MAX_UTF_16_CODE));
		if(char == '\r' || char == '\n') {
			continue;
		}
		if(	text1.indexOf(char) == -1 &&
			text2.indexOf(char) == -1) {
			return char;
		}
	}
	throw "charCode할당 실패";
}

Replace.prototype.doTask = function(thisPtr) {
	window.queueCnt = 1;
	if(thisPtr.taskQueue.length > 0) {
		if(window.queueCnt == 1) {
			var task = thisPtr.taskQueue.shift();
			task(thisPtr);
		}
		window.setTimeout(thisPtr.doTask.bind(null, thisPtr), 10);
	}
	window.queueCnt = 0;
}

/**
 * 다른 곳에서 setTimeout을 하지 않도록 queueCnt 를 2로 고정합니다.
 * @param {Replace} thisPtr
 * @param {Match} match nullable
 * @param {Number} replaceDiffLength 치환하면서 어긋난 위치 보정값 
 * @param {Number} textIdx 왼쪽이면 0, 오른쪽으면 1
 */
Replace.prototype.doReplaceTask = function(thisPtr, match, replaceDiffLength, textIdx) {
	window.queueCnt = 2;
	var idx = 0;
	
	var RegularExp = thisPtr._regularExp;
	while(( match = RegularExp.exec(thisPtr._text[textIdx])) !== null) {
		match.index -= replaceDiffLength;
		replaceDiffLength += (match[0].length - thisPtr._replaceChar.length);
		thisPtr._textMatch[textIdx].push(match);
		idx++;
		if(idx > 100) {
			window.setTimeout(thisPtr.doReplaceTask(thisPtr, match, replaceDiffLength, textIdx), 10);
			return;
		}
	}
	
	window.queueCnt = 1;
}

Replace.prototype.doReplaceWithTask = function() {
	if(this._withReplaceChar) {
		this._replaceChar = this.getRandomReplaceChar(this._text[0], this._text[1]);
	} else {
		this._replaceChar = '';
	}
	
	var makeFunc = function make(match, replaceDiffLength, textIdx) {
		return function (thisPtr) {
			thisPtr.doReplaceTask(thisPtr, match, replaceDiffLength, textIdx);
		}
	}
	this.taskQueue.push(makeFunc(undefined, 0, 0));
	
	this.taskQueue.push(function replaceLeft(thisPtr) {
		thisPtr._text[0] = thisPtr._text[0].replace(thisPtr._regularExp, thisPtr._replaceChar);
	});
	
	this.taskQueue.push(makeFunc(undefined, 0, 1));
	
	this.taskQueue.push(function replaceRight(thisPtr) {
		thisPtr._text[1] = thisPtr._text[1].replace(thisPtr._regularExp, thisPtr._replaceChar);
	});
	
	this.doTask(this);
}

/**
 * 주어진 정규식에 해당되는 문자들을 같은 문자로 치환합니다.
 * 치환하여 text1Match, text2Match에 보관합니다.
 * @param {String} text1 텍스트 내용입니다.
 * @return {!Array.<String>} 치환된 내용들입니다.
 */
Replace.prototype.doReplace = function(text1, text2) {
	if(this._withReplaceChar) {
		this._replaceChar = this.getRandomReplaceChar(text1, text2);
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
		return this.resotreWithoutReplaceChar();
	} else {
		return this.restoreWithReplaceChar();
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
Restore.prototype.isHaveChangePos = function(idx, matchArr, startPos, diffStr) {
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
Restore.prototype._restoreWithReplaceChar = function(matchArr, matchIdx, curTextLength) {
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
Restore.prototype.restoreWithReplaceChar = function() {
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
			if(	!(this.isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr) ||
				this.isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr) )) {
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
			if(!this.isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr)){
				curTextLengths[1] += diffStr.length;
				continue;
			} else {
				var length = this._restoreWithReplaceChar(this._text2Match, text2MatchIdx, curTextLengths[1]);
				curTextLengths[1] += length;
				this._diffsIdx--;
				text2MatchIdx++;
			}
		} else if(diffStatus == window.DIFF_DELETE ||
					diffStatus == window.DIFF_DELETE * 2) {	// left 에 속하면
			if(!this.isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr)) {
				curTextLengths[0] += diffStr.length;
				continue;
			} else {
				var length = this._restoreWithReplaceChar(this._text1Match, text1MatchIdx, curTextLengths[0]);
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
Restore.prototype._restoreWithoutReplaceChar = function(matchArr, matchIdx, curTextLength, insertDiffStatus) {
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
Restore.prototype.resotreWithoutReplaceChar = function() {
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
			if(	!(this.isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr) ||
				this.isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr)) ) {
				curTextLengths[0] += diffStr.length;
				curTextLengths[1] += diffStr.length;
			} else {
				// 범위 벗어나지 않았고, 왼쪽이 포함이면 일단 왼쪽
				// 범위 벗어나지 않았고, 오른쪽이 포함이면 일단 오른쪽
				// 단 둘다 포함이면 가까운 쪽부터 하자.
				var isLeftInside = this.isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr);
				var isRightInside = this.isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr);
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
					var length = this._restoreWithoutReplaceChar(this._text1Match, text1MatchIdx, curTextLengths[0], window.DIFF_DELETE * 2);
					curTextLengths[0] += length;
					curTextLengths[1] += length;
					this._diffsIdx--;		// 이전 블럭을 꺼낸다.
					text1MatchIdx++;
				} else {
					var length = this._restoreWithoutReplaceChar(this._text2Match, text2MatchIdx, curTextLengths[1], window.DIFF_INSERT * 2);
					curTextLengths[0] += length;
					curTextLengths[1] += length;
					this._diffsIdx--;
					text2MatchIdx++;
				}
			}
		} else if(diffStatus == window.DIFF_INSERT ||
				diffStatus == window.DIFF_INSERT * 2) {	// right 에 속하면
			if(!this.isHaveChangePos(text2MatchIdx, this._text2Match, curTextLengths[1], diffStr)){
				curTextLengths[1] += diffStr.length;
			} else {
				var length = this._restoreWithoutReplaceChar(this._text2Match, text2MatchIdx, curTextLengths[1], diffStatus);
				curTextLengths[1] += length;
				this._diffsIdx--;
				text2MatchIdx++;
			}
		} else if(diffStatus == window.DIFF_DELETE ||
					diffStatus == window.DIFF_DELETE * 2) {	// left 에 속하면
			if(!this.isHaveChangePos(text1MatchIdx, this._text1Match, curTextLengths[0], diffStr)) {
				curTextLengths[0] += diffStr.length;
			} else {
				var length = this._restoreWithoutReplaceChar(this._text1Match, text1MatchIdx, curTextLengths[0], diffStatus);
				curTextLengths[0] += length;
				this._diffsIdx--;
				text1MatchIdx++;
			}
		}
	}
	return this._diffs;
}