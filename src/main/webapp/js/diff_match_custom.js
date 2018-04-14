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
function DiffMatchCustom(Diff_Timeout, Diff_EditCost, Diff_IgnoreCase, leastRepeatCnt) {
	this.leastRepeatCnt = leastRepeatCnt;
	var r = this.leastRepeatCnt;
	this.regularExp = '(\t{' + r + ',}|\n{' + r + ',}|\r{' + r + ',}|\0{' + r + ',}|\ {' + r + ',})';
	this.dmp = new diff_match_patch();	
	this.dmp.Diff_Timeout = parseFloat(Diff_Timeout);
	this.dmp.Diff_EditCost = parseFloat(Diff_EditCost);
	this.dmp.Diff_Sensitive = Diff_IgnoreCase;
	
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
DiffMatchCustom.prototype.diff_cleanupIgnoreCase = function diff_cleanupIgnoreCase(diffs, curIdx, originalStatus, diffStr) {
}
/**
 * 탭, 개행(\r\n), EOF, space을 같지 않다고 취급합니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples
 */
DiffMatchCustom.prototype.diff_cleanupCustom = function diff_cleanupCustom(diffs) {
	for (var i = 0; i < diffs.length; i++) {
		if (diffs[i][0] == window.DIFF_EQUAL) {
			continue;
		}

		var diffStr = diffs[i][1];
		var matchResult = diffStr.match(this.regularExp);
		var originalStatus = diffs[i][0];
		diffs.splice(i, 1);
		var rtn = this.addDiffsSubstrRegularExp(diffs, i, originalStatus, diffStr, matchResult);
		i = rtn[0] - 1;
		diffs = rtn[1];
	}
	return diffs;
}

/***
 * @param text1 비교할 텍스트 1입니다.(보통 왼쪽)
 * @param text2 비교할 텍스트 2입니다.(보통 오른쪽)
 * @param cleanupOption diff 알고리즘의 옵션입니다. cleanupOpt의 sementic, efficiency, no 중에 설정 가능합니다.
 * @return {!Array.<String>} 비교한 후의 html 입니다. 0이 왼쪽, 1이 오른쪽입니다.
 */
DiffMatchCustom.prototype.start = function(text1, text2, cleanupOption) {
	var ms_start = (new Date()).getTime();
	var d1 = this.dmp.diff_main(text1, text2);
	var d2 = this.dmp.diff_main(text2, text1);
	
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
	console.log(text1.length); //text1 길이
	console.log(text2.length); //text2 길이
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

	var diffMatch = new DiffMatchCustom(2, 4, 1, 2);
	var ds = diffMatch.start(text1, text2, cleanupOpt.efficiencyCleanup);

	document.getElementById('outputdivLeft').innerHTML = ds[0];
	document.getElementById('outputdivRight').innerHTML = ds[1];
}