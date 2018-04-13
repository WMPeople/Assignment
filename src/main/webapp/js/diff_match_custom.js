/**
 * diff_match_custom 에서 diff_match_patch 를 호출합니다.
 * 커스텀을 위한 어뎁터 클래스로 볼수도 있습니다.
 * 
 * @author khh
 * @author rws
 */


/**
 * diff_cleanupCustom의 helper 메소드입니다.
 * 정규식에 해당되는 문자를 같은 문자열로 취급하게 합니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples
 * @param {Number} orignalStatus diff[][0]의 값
 * @param {String} diffStr diff[][1]의 값
 * @param {!Array} matchResult String.match의 리턴값이 들어와야 합니다.
 * @return {!Array.<Number, !diff_match_patch.Diff>} Array of diff tuples.
 */
function addDiffsSubstrRegularExp(diffs, index, originalStatus, diffStr, matchResult, regularExp) {
	while (matchResult != null) {

		var firstSubstr;
		if (matchResult.index !== 0) {
			firstSubstr = diffStr.substr(0, matchResult.index);
			diffs.splice(index++, 0, [ originalStatus, firstSubstr ]);
		}

		var middleSubstr = diffStr.substr(matchResult.index, matchResult[0].length);
		diffs.splice(index++, 0, [ originalStatus * 2, middleSubstr ]);

		var endSubstr;
		var endSubstrIndex = matchResult.index + matchResult[0].length;
		if (endSubstrIndex !== diffStr.length) {
			endSubstr = diffStr.substr(endSubstrIndex, diffStr.length - endSubstrIndex);
			diffStr = endSubstr;
			matchResult = diffStr.match(regularExp);
		} else {
			diffStr = "";
			break;
		}
	}
	if (diffStr.length != 0) {
		diffs.splice(index++, 0, [ originalStatus, diffStr ]);
	}
	return [ index, diffs ];
}

/**
 * 대소문자를 구분하지 않고 같다고 합시다.
 * -1, 1에 대문자로 치환했을때 공통된 부분이 있으면
 * 이를 따로 분리하여야 합니다.
 * 스페이스 탭, 등이 같다고 처리되어야 하면, 그 결과를 가지고 진행하면 시간상의 이득이 존재할것으로 생각됩니다.
 */
function diff_cleanupIgnoreCase(diffs, index, originalStatus, diffStr) {
}
/**
 * 탭, 개행(\r\n), EOF, space을 같지 않다고 취급합니다.
 * @param {!Array.<!diff_match_patch.Diff>} diffs Array of diff tuples
 * @param {number} leastRepeatCnt 연속되는 문자의 최소 개수
 */
function diff_cleanupCustom(diffs, leastRepeatCnt) {
	for (var i = 0; i < diffs.length; i++) {
		if (diffs[i][0] == window.DIFF_EQUAL) {
			continue;
		}
		var r = leastRepeatCnt;
		var regularExp = '(\t{' + r + ',}|\n{' + r + ',}|\r{' + r + ',}|\0{' + r + ',}|\ {' + r + ',})';

		var diffStr = diffs[i][1];
		var matchResult = diffStr.match(regularExp);
		var originalStatus = diffs[i][0];
		diffs.splice(i, 1);
		var rtn = addDiffsSubstrRegularExp(diffs, i, originalStatus, diffStr, matchResult, regularExp);
		i = rtn[0] - 1;
		diffs = rtn[1];
	}
	return diffs;
}
var dmp = new diff_match_patch();


function launch() {
	var text1 = document.getElementById('text1').value;
	var text2 = document.getElementById('text2').value;

	dmp.Diff_Timeout = parseFloat(2);
	dmp.Diff_EditCost = parseFloat(4);
	dmp.Diff_Sensitive = 1;

	//체크 박스 만든 후 수정 예정 
	// 			if(대소문자구분없을시) dmp.sensitive = 1;
	// 			else 대소문자구분할시 dmp.sensitive = 0;

	var ms_start = (new Date()).getTime();
	var d1 = dmp.diff_main(text1, text2);
	//dmp.diff_cleanupSemantic(d1);
	dmp.diff_cleanupEfficiency(d1);
	var d2 = dmp.diff_main(text2, text1);
	//dmp.diff_cleanupSemantic(d2);
	dmp.diff_cleanupEfficiency(d2);

	{
		d1 = diff_cleanupCustom(d1, 2);
		d2 = diff_cleanupCustom(d2, 2);
	}
	var ds = dmp.diff_prettyHtml(d1, d2);

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

	document.getElementById('outputdivLeft').innerHTML = ds[0]
	document.getElementById('outputdivRight').innerHTML = ds[1]
}