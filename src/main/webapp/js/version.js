// 체크 박스 2개만 클릭 가능하도록 하게 만드는 메쏘드입니다.
var cbox = new remain_two_obj('cbox');
function remain_two_obj(prefix) {
	this.old = new Array();
	this.prefix = prefix;
	this.remain_two = function(cur) {
		items = document.getElementsByName(this.prefix + '[]');
		for (i = 0, count = 0; i < items.length; i++)
			if (items[i].checked) {
				count++;
		}
		if (count > 0 && cur.checked == false && this.old[0] == cur.value) {
			this.old[0] = this.old[1];
		}
		if (cur.checked == false) {
			return;
		}
		if (count < 2) {
			this.old[count] = cur.value;
		} else {
			this.old[0] = this.old[1];
			this.old[1] = cur.value;
			items = document.getElementsByName(this.prefix + '[]');
			for (j = 0; j < items.length; j++) {
				if (items[j].value != this.old[0] && items[j].value != this.old[1]) {
					items[j].checked = false;
				}
			}
		}
	}
}

// 비교 버튼입니다 .
function btnDiff() {
	var num = 0;
	var checkArr = [];
	$(":checkbox[name='cbox[]']:checked").each(function(index) {
		num += 1;
		checkArr.push($(this).val());
	});
	if (num == 2) {

		var firstNode = checkArr[0].split('-');
		var secondNode = checkArr[1].split('-');

		$("#board_id1").val(Number(firstNode[0]));
		$("#version1").val(Number(firstNode[1]));

		$("#board_id2").val(Number(secondNode[0]));
		$("#version2").val(Number(secondNode[1]));

		var fm = document.diffForm;
		fm.method = 'get';
		fm.action = '/assignment/boards/diff';
		fm.submit();
	}
}

// 버전 삭제 버튼입니다.
function btnVersionDelete(board_id, version) {
	$.ajax({
		type : "DELETE",
		url : "/assignment/boards/version/" + board_id + "/" + version,
		success : function(result) {
			if (result.result == 'success') {
				alert("삭제완료");
				location.reload();
				makeFakeJson("deleteVersion", new Date().toLocaleString(),board_id , version);
			} else {
				alert("삭제실패");
			}
		},
		error : function(xhr, status, error) {
			alert(error);
		}
	})
}

// 버전 복구 버튼입니다.
function btnRecover(board_id, version) {
	var tableSearch = document.getElementById('table');

	var leafBoard_id = Number(tableSearch.rows[1].cells[1].innerHTML);
	var leafVersion = Number(tableSearch.rows[1].cells[2].innerHTML);
	$.ajax({
		type : "GET",
		url : "/assignment/boards/recover/" + board_id + "/" + version + "/" + leafBoard_id + "/" + leafVersion,
		success : function(result) {
			if (result.result == 'success') {
				alert("복원완료");
				location.href = '/assignment/boards/management/' + result.board_id + '/' + result.version;
				makeFakeJson("restore", new Date().toLocaleString(),board_id , version);
			} else {
				alert(result.result);
			}
		},
		error : function(xhr, status, error) {
			alert(error);
		}
	})
}