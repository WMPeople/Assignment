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
 * 
 * @param {number} leastRepeatCnt 연속되는 문자의 최소 개수
 */
function DiffMatchCustom(Diff_Timeout, Diff_EditCost, Diff_IgnoreCase, leastRepeatCnt, text1, text2) {
	this.leastRepeatCnt = leastRepeatCnt;
	var r = this.leastRepeatCnt;
	this.regularExp = '(\t{' + r + ',}|\n{' + r + ',}|\r{' + r + ',}|\0{' + r + ',}|\ {' + r + ',})';
	this.dmp = new diff_match_patch();	
	this.dmp.Diff_Timeout = parseFloat(Diff_Timeout);
	this.dmp.Diff_EditCost = parseFloat(Diff_EditCost);
	this.dmp.Diff_Sensitive = Diff_IgnoreCase;
	this.preMatchList = [];	// [ [ text1MatchObj, text1Flag ], [text2MatchObj , text2Flag ] ] sorted by index
	this.text1Match = [];	// [ text1MatchObj]
	this.text2Match = [];	// [ text2MatchObj]
	this.text1 = text1;
	this.text2 = text2;
	this.isPreFilterOn = true;
	

	this.replacedChar = 'a';	// TODO : 만약 text에 'a'가 있다면 겹칠 수 있으므로 다른 문자로 변환할것.
	this.replacedLength = this.replacedChar.length;
}

/**
 * diff_cleanupCustom의 helper 메소드입니다.
 * 정규식에 해당되는 문자를 같은 문자열로 취급하게 합니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples
 * @param {Number} curIdx diffs배열에서 동작하고 있는 idx
 * @param {Number} orignalStatus diff[][0]의 값
 * @param {String} diffStr diff[][1]의 값
 * @param {!Array} matchResult String.match의 리턴값이 들어와야 합니다.
 * @return {!Array.<Number, !diff_match_patch.Diff>} Array of diff tuples.
 */
DiffMatchCustom.prototype.addDiffsSubstrRegularExp = function(diffs, curIdx, originalStatus, diffStr, matchResult) {
	while (matchResult != null) {

		var firstSubstr;
		if (matchResult.index !== 0) {
			firstSubstr = diffStr.substr(0, matchResult.index);
			diffs.splice(curIdx++, 0, [ originalStatus, firstSubstr ]);
		}

		var middleSubstr = diffStr.substr(matchResult.index, matchResult[0].length);
		diffs.splice(curIdx++, 0, [ originalStatus * 2, middleSubstr ]);

		var endSubstr;
		var endSubstrIndex = matchResult.index + matchResult[0].length;
		if (endSubstrIndex !== diffStr.length) {
			endSubstr = diffStr.substr(endSubstrIndex, diffStr.length - endSubstrIndex);
			diffStr = endSubstr;
			matchResult = diffStr.match(this.regularExp);
		} else {
			diffStr = "";
			break;
		}
	}
	if (diffStr.length != 0) {
		diffs.splice(curIdx++, 0, [ originalStatus, diffStr ]);
	}
	return [ curIdx, diffs ];
}	

/**
 * 대소문자를 구분하지 않고 같다고 합시다.
 * -1, 1에 대문자로 치환했을때 공통된 부분이 있으면
 * 이를 따로 분리하여야 합니다.
 * 스페이스 탭, 등이 같다고 처리되어야 하면, 그 결과를 가지고 진행하면 시간상의 이득이 존재할것으로 생각됩니다.
 */
DiffMatchCustom.prototype.diff_cleanupIgnoreCase = function(diffs, curIdx, originalStatus, diffStr) {
}
/**
 * 탭, 개행(\r\n), EOF, space을 같지 않다고 취급합니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples
 */
DiffMatchCustom.prototype.diff_cleanupCustom = function(diffs) {
	var curTextLengths = [0, 0];	// 현재 문서의 위치
	var textDiffLengths = [0, 0];	// 정규식으로 인하여 달라진 문서의 길이의 총합
	var preMatchIdx = 0; // preMatchList의 idx
	var text1MatchIdx = 0;
	var text2MatchIdx = 0;
	var curMatchNum = 0;	// text1인지 text2인지 판단
	
	var leftMatchIdx = 0;
	var rightMatchIdx = 0;
	
	for (var i = 0; i < diffs.length; i++) {
		var diffStatus = diffs[i][0];
		var diffStr = diffs[i][1];
		
		if(this.isPreFilterOn !== true){
			break;
		}
		
		var matchBlock;
		// text1이 가까운지, text2가 가까운지 비교..
		if((text2MatchIdx >= this.text2Match.length) ||	// index out of range 방지
			(text1MatchIdx < this.text1Match.length &&
			(this.text1Match[text1MatchIdx].index < this.text2Match[text2MatchIdx].index))) {
			curMatchNum = window.DIFF_DELETE;
			matchBlock = this.text1Match[text1MatchIdx];
		} else {
			curMatchNum = window.DIFF_INSERT;
			matchBlock = this.text2Match[text2MatchIdx];
		}
		
		//var matchBlock = this.preMatchList[preMatchIdx];	// [ matchResult, textFlag ]
		if(		// 변경이 없으면 둘다 검사, 삭제 된것이면 왼쪽만 검사, 추가된것이면 오른쪽만 검사. 하여 포함 되지 않으면.
				// 왼쪽 텍스트에 속하고, 변경 위치가 왼쪽 텍스트 위치에 속하지 않고
				// 오른쪽 텍스트에 속하고, 변경 위치가 오른쪽 텍스트 위치에 속하지 않을때.
			!(
				(diffStatus == window.DIFF_EQUAL &&
					(
						( curMatchNum == window.DIFF_DELETE &&
						(curTextLengths[0] <= matchBlock.index &&
						matchBlock.index < curTextLengths[0] + diffStr.length)
						)
							||
						( curMatchNum == window.DIFF_INSERT &&
						(curTextLengths[1] <= matchBlock.index &&
						matchBlock.index < curTextLengths[1] + diffStr.length)
						)
					)
				)
					||
				(
				diffStatus == window.DIFF_DELETE &&
					(curTextLengths[0] <= matchBlock.index &&
					matchBlock.index < curTextLengths[0] + diffStr.length)
				)
					||
				(
				diffStatus == window.DIFF_INSERT &&
					(curTextLengths[1] <= matchBlock.index &&
					matchBlock.index < curTextLengths[1] + diffStr.length) 
				)
			)
		  ) {
			if(diffStatus == window.DIFF_EQUAL) {
				curTextLengths[0] += diffStr.length;
				curTextLengths[1] += diffStr.length;
			} else if(diffStatus == window.DIFF_INSERT) {
				curTextLengths[1] += diffStr.length;
			} else if(diffStatus == window.DIFF_DELETE) {
				curTextLengths[0] += diffStr.length;
			}
			continue;
		}
		
		// 한개의 블럭에 대한 것이므로 블럭을 다시 만듦.
		diffs.splice(i, 1);
											// 같은 경우에는 2번 치환 되지 않을 수 있음..(같은 것을 변환 하였기에)
		if(diffStatus == window.DIFF_EQUAL) { // 같은 경우에는 2번 치환 됨을 명심할것. this.preMatchList은 -1, 1 와 같이 번갈아 나올것이라는 예측
			var textNum = curMatchNum == window.DIFF_INSERT ? 1 : 0;	// 1 오른쪽, 0 왼쪽	 순서를 정렬 했으면, 이것이 필요 없음.
			var oppositTextNum = curMatchNum == window.DIFF_INSERT ? 0 : 1;
			
			var replacePos = matchBlock.index - curTextLengths[textNum];
			if(replacePos !== 0) {
				var beforeReplaceStr = diffStr.substr(0, replacePos);
				diffs.splice(i++, 0, [ diffStatus, beforeReplaceStr ]);
			}
			
			{
				diffs.splice(i++, 0, [ curMatchNum * 2, matchBlock[0] ]);
				if(curMatchNum == window.DIFF_DELETE) {
					text1MatchIdx++;
				} else {
					text2MatchIdx++;
				}

				if(	text1MatchIdx >= this.text1Match.length &&
					text2MatchIdx >= this.text2Match.length) {
					throw new Exception("이게 실행되나?");
					break;
				}
				
				var nextMatchBlock;
				if((text2MatchIdx >= this.text2Match.length) ||	// index out of range 방지
					(text1MatchIdx < this.text1Match.length &&
					(this.text1Match[text1MatchIdx].index < this.text2Match[text2MatchIdx].index))) {
					curMatchNum = window.DIFF_DELETE;
					nextMatchBlock = this.text1Match[text1MatchIdx];		// 이건 마지막에서 증가 시켜줌. curMatchNum에 종속적.
				} else {
					curMatchNum = window.DIFF_INSERT;
					nextMatchBlock = this.text2Match[text2MatchIdx];
				}
				
				diffs.splice(i++, 0, [ curMatchNum * 2, nextMatchBlock[0] ]);
			}
			
			var lastReplaceBeginPos= replacePos + this.replacedLength;
			if(lastReplaceBeginPos !== diffStr.length) {
				var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
				diffs.splice(i, 0, [ diffStatus, afterReplacedStr ]);
			}
			
			curTextLengths[textNum] += lastReplaceBeginPos;
			curTextLengths[oppositTextNum] += matchBlock.index - curTextLengths[oppositTextNum] + this.replacedLength;
		}
		else {
			if(curMatchNum == window.DIFF_INSERT) {	// right 에 속하면
				var replacePos = matchBlock.index - curTextLengths[1];
				if(replacePos !== 0) {
					var beforeReplaceStr = diffStr.substr(0, replacePos);
					diffs.splice(i++, 0, [ diffStatus, beforeReplaceStr ]);
				}
				
				if(diffStatus == window.DIFF_EQUAL) {
					curTextLengths[0] += replacePos;
					diffs.splice(i++, 0, [ window.DIFF_INSERT_EQUAL , matchBlock[0] ]);
				} else {
					diffs.splice(i++, 0, [ diffStatus, matchBlock[0] ]);
				}
				
				var lastReplaceBeginPos= replacePos + this.replacedLength;
				if(lastReplaceBeginPos !== diffStr.length) {
					var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
					diffs.splice(i, 0, [ diffStatus, afterReplacedStr ]);
				}
				
				curTextLengths[1] += lastReplaceBeginPos
			}
			
			else if(curMatchNum == window.DIFF_DELETE) {	// left 에 속하면
				var replacePos = matchBlock.index - curTextLengths[0];
				if(replacePos !== 0) {
					var beforeReplaceStr = diffStr.substr(0, replacePos);
					diffs.splice(i++, 0, [ diffStatus, beforeReplaceStr ]);
				}
				
				if(diffStatus == window.DIFF_EQUAL) {	// 서로 같은 것에 속하는 것. 즉, 연속적으로 나올 것임.
					curTextLengths[1] += replacePos;
					diffs.splice(i++, 0, [ window.DIFF_DELETE_EQUAL , matchBlock[0] ]);
				} else {
					diffs.splice(i++, 0, [ diffStatus, matchBlock[0] ]);
				}
				
				var lastReplaceBeginPos = replacePos + this.replacedLength;
				if(lastReplaceBeginPos !== diffStr.length) {
					var afterReplacedStr = diffStr.substr(lastReplaceBeginPos);
					diffs.splice(i, 0, [ diffStatus, afterReplacedStr ]);
				}

				curTextLengths[0] += lastReplaceBeginPos
			}
		}
		i--; // 이전 블록을 꺼낸다.
		//preMatchIdx++;	// 다음 매치 블럭을 찾자.
		if(curMatchNum == window.DIFF_DELETE) {
			text1MatchIdx++;
		} else {
			text2MatchIdx++;
		}
		if(	text1MatchIdx >= this.text1Match.length &&
			text2MatchIdx >= this.text2Match.length) {
			break;
		}
		/*
		if(preMatchIdx >= this.preMatchList.length) {
			break;
		}
		*/
	}
	return diffs;
}
/*DiffMatchCustom.prototype.diff_cleanupCustom = function(diffs) {
	var curTextLengths = [0, 0];	// 현재 문서의 위치
	var textDiffLengths = [0, 0];	// 정규식으로 인하여 달라진 문서의 길이의 총합
	var preMatchIdx = 0; // preMatchList의 idx
	
	for (var i = 0; i < diffs.length; i++) {
		var diffStatus = diffs[i][0];
		var diffStr = diffs[i][1];

		if(this.isPreFilterOn !== true){
			break;
		}
		else if(preMatchIdx >= this.preMatchList.length) {
			break;
		}
		var curListEle = this.preMatchList[preMatchIdx];	// [ matchResult, textFlag ]
		var curMatch = curListEle[0];

		// 현재 문서의 위치 <= 변경된 idx < 다음 문서의 위치
		if(curTextLengths[0] <= curMatch.index &&
				curMatch.index < curTextLengths[0] + diffStr.length) {
			
			diffs.splice(i, 1);
			
			while(curTextLengths[0] <= curMatch.index &&
				curMatch.index < curTextLengths[0] + diffStr.length) {	
				var replaceIdx = curMatch.index - curTextLengths[0];
				
				// a를 지워줘야 함.
				
				if(replaceIdx !== 0) {
					var firstSubstr = diffStr.substr(0, replaceIdx);
					diffs.splice(i++, 0, [ diffStatus, firstSubstr ]);
					
					curTextLengths[0] += replaceIdx;
					curTextLengths[1] += replaceIdx;
					
					diffStr = diffStr.substr(replaceIdx);
				}
			
				if(curListEle[1] == window.DIFF_INSERT) {
					if(diffStatus == window.DIFF_EQUAL) {
						diffs.splice(i++, 0, [ window.DIFF_DELETE_EQUAL, curMatch[0] ]);
					} else {
						diffs.splice(i++, 0, [ window.DIFF_DELETE, curMatch[0] ]);
					} 
					textDiffLengths[0] += curMatch[0].length;
				} else if(curListEle[1] == window.DIFF_DELETE) {
					if(diffStatus == window.DIFF_EQUAL) {
						diffs.splice(i++, 0, [ window.DIFF_INSERT_EQUAL, curMatch[0] ]);
					} else {
						diffs.splice(i++, 0, [ window.DIFF_INSERT, curMatch[0] ]);
					}
					textDiffLengths[1] += curMatch[0].length;
				}
				
				preMatchIdx++;
				if(preMatchIdx >= this.preMatchList.length) {
					break;
				}
				curListEle = this.preMatchList[preMatchIdx];
				curMatch = curListEle[0];
			}

			if(diffStatus == window.DIFF_EQUAL) {
				var replaceIdx = curMatch.index - curTextLengths[0];
				var lastSubstr = diffStr.substr(replaceIdx, diffStr.length);
				diffs.splice(i, 0, [ window.DIFF_EQUAL, lastSubstr ]);				
			}
		}
		if(!(curTextLengths[0] <= curMatch.index &&
			curMatch.index < curTextLengths[0] + diffStr.length)){
			curTextLengths[0] += diffStr.length;
			curTextLengths[1] += diffStr.length;
		}
		
		} else if(diffs[i][0] == window.DIFF_DELETE) {
			curTextLengths[0] += diffStr.length;
		} else if(diffs[i][0] == window.DIFF_INSERT) {
			curTextLengths[1] += diffStr.length;
		}
		
		if(0 != 0) {	// ignore this. 
			var diffStr = diffs[i][1];
			var matchResult = diffStr.match(this.regularExp);
			var originalStatus = diffs[i][0];
			diffs.splice(i, 1);
			var rtn = this.addDiffsSubstrRegularExp(diffs, i, originalStatus, diffStr, matchResult);
			i = rtn[0] - 1;
			diffs = rtn[1];
		}
	}
	return diffs;
}*/

DiffMatchCustom.prototype.preFilter = function (regularExp) {
	var match, matchRight;
	var leftRegularExp = regularExp;
	while(( match = leftRegularExp.exec(this.text1)) !== null) {
		//this.preMatchList.push([ match, window.DIFF_DELETE ]);
		this.text1Match.push(match);
		this.text1 = this.text1.substr(0, match.index) + 'a' + this.text1.substr(match.index + match[0].length);
		leftRegularExp.lastIndex -= match[0].length;
	}
	var rightRegularExp = regularExp;
	while(( matchRight = rightRegularExp.exec(this.text2)) !== null) {
		//this.preMatchList.push([ matchRight, window.DIFF_INSERT ]);
		this.text2Match.push(matchRight);
		this.text2 = this.text2.substr(0, matchRight.index) + 'a' + this.text2.substr(matchRight.index + matchRight[0].length);
		rightRegularExp.lastIndex -= matchRight[0].length;
	}
	
	/*
	this.preMatchList.sort(function(lhs, rhs){
		return lhs[0].index > rhs[0].index;
	});
	*/
}

DiffMatchCustom.prototype.doReverse = function () {
	var temp = this.text1Match;
	this.text1Match = this.text2Match;
	this.text2Match = temp;

	// deprecated
	for(var i = 0; i < this.preMatchList.length; i++ ){
		this.preMatchList[i][1] = this.preMatchList[i][1] * -1;
	}
}

/***
 * @param text1 비교할 텍스트 1입니다.(보통 왼쪽)
 * @param text2 비교할 텍스트 2입니다.(보통 오른쪽)
 * @param cleanupOption diff 알고리즘의 옵션입니다. cleanupOpt의 sementic, efficiency, no 중에 설정 가능합니다.
 * @return {!Array.<String>} 비교한 후의 html 입니다. 0이 왼쪽, 1이 오른쪽입니다.
 */
DiffMatchCustom.prototype.start = function(cleanupOption) {
	var ms_start = (new Date()).getTime();
	var d1 = this.dmp.diff_main(this.text1, this.text2);
	var d2 = this.dmp.diff_main(this.text2, this.text1);
	
	switch(cleanupOption) {
	case cleanupOpt.sementicCleanup:
		this.dmp.diff_cleanupSemantic(d1);
		this.dmp.diff_cleanupSemantic(d2);
		break;
		
	case cleanupOpt.efficiencyCleanup:
		this.dmp.diff_cleanupEfficiency(d1);
		this.dmp.diff_cleanupEfficiency(d2);
		break;
	
	case cleanupOpt.noCleanup:
	default:
			break;
	}

	{
		d1 = this.diff_cleanupCustom(d1, 2);
		this.doReverse();
		d2 = this.diff_cleanupCustom(d2, 2);
	}
	var ds = this.dmp.diff_prettyHtml(d1, d2);

	console.log(d1);
	var length = d1.length;
	var count = 0;
	var nextVal = 0;
	for (var i = 0; i < d1.length; i++) {
		if (d1[i][0] != 0) {
			count++;

			while (d1[i][1].search('\n') != -1) {
				nextVal++;
				console.log(d1[i][0] + ' test ' + d1[i][1]);
				d1[i][1] = d1[i][1].substring(d1[i][1].search('\n') + 2);

			}
		}
	}
	console.log(this.text1.length); //text1 길이
	console.log(this.text2.length); //text2 길이
	//TODO tex2 - text 1 절댓값 구해야함 
	console.log(length); // 배열사이즈
	console.log(count); //-1 ,1 개수
	console.log(nextVal); // 개행 개수


	var ms_end = (new Date()).getTime();
	console.log("전체 소요 시간 : " + (ms_end - ms_start) / 1000);
	
	return ds;
}

function launch() {
	var text1 = document.getElementById('text1').value;
	var text2 = document.getElementById('text2').value;

	var diffMatch = new DiffMatchCustom(2, 4, 1, 2, text1, text2);
	diffMatch.preFilter(/<br>|<\/br>/g);
	var ds = diffMatch.start(cleanupOpt.efficiencyCleanup);

	document.getElementById('outputdivLeft').innerHTML = ds[0];
	document.getElementById('outputdivRight').innerHTML = ds[1];
}