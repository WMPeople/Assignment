function getUrl(category, crawling_text, startCnt, pageNo) {
	var url;
	const crawling_api = 'search';
	if(pageNo == undefined) {
		pageNo = 1;
	}
	switch(category) {
	case 'book':
	case 'movie':
	case 'news':
	case 'shop':
		url = "/assignment/api/naver/" + crawling_api + "/"
		+ category + "/" + crawling_text + "/" + startCnt;
		break;
	case 'geocode':
		url = "/assignment/api/kakao/" +crawling_api+"/" + category + "/"
		+ crawling_text + "/" + pageNo;
		break;
	case 'dictionary':
		url = "/assignment/api/browser/crawling/" + category + "/"
		+ crawling_text + "/" + pageNo;
		break;
	case 'local':
		url = "/assignment/api/naver/" + crawling_api + "/"
		+ category + "/" + crawling_text + "/" + startCnt;
		break;
	default:
		break;
	}
	return url;
}

var category;
var crawling_text;
var curIndex = 0;
var url_category_text = [];
pageNo = 1;

$(function() {
	// textarea 내용 복사
	var copyContent = $('#content3').val();
	var keywordList = [];
	var nameList = ["위치", "영화", "책", "도서" , "맛집", "뉴스", "쇼핑", "음식점", "단어" , "영어단어", "구매", "지도"];
	
	//keywordList 리스트에  ["영화" , "그린팩토리"] 형식으로 푸쉬된다.
	for(var i =0; i< nameList.length ; i++){
		var searchIndex = 0;
		while(searchIndex != -1){
			var searchIndex = copyContent.search(nameList[i]);
			var tempSpace = '';
			for(var j =0 ; j< nameList[i].length ; j++){
				tempSpace += ' ';
			}
			copyContent = copyContent.replace(nameList[i],tempSpace);
			if(searchIndex!= -1){
				var spaceIndex = searchIndex + nameList[i].length;
				var firstQuoteIndex = spaceIndex + 1;
				if(copyContent.charAt(spaceIndex) == ' ' && copyContent.charAt(firstQuoteIndex) == "'"){
					var startIndex = firstQuoteIndex + 1;
					var secondQuoteIndex = copyContent.substring(startIndex).search("'") + startIndex + 1;
					var text = copyContent.substring(firstQuoteIndex + 1,secondQuoteIndex - 1);
					copyContent = copyContent.substring(0,searchIndex) + copyContent.substring(secondQuoteIndex);
					if (text == "") {
						continue;
					} else {
						var list = [nameList[i] , text]
						keywordList.push(list);		
					}
					
				}
			}	
		}
	}
	
	//배열 중복 제거, 자원 감소
	function unique(array) {
		  var tempArray = [];
		  var resultArray = [];
		  for(var i = 0; i < array.length; i++) {
		    var item = array[i]
		    if(tempArray.includes(item[0])) {
		      continue;
		    } else {
		      resultArray.push(item);
		      tempArray.push(item[0]);
		    }
		  }
		  return resultArray;
		}
	keywordList = unique(keywordList);
	
	
	//name 별로 다른 url을 호출한다.
	for(var i = 0 ; i < keywordList.length ; i++){
		switch(keywordList[i][0]) {
		case '책': category = 'book';
		case '도서':
			break;
			
		case '영화':
			category = 'movie';
			break;
			
		case '뉴스':
			category = 'news';
			break;
			
		case '쇼핑':
		case '구매':
			category = 'shop';
			break;
			
		case '지도':
		case '위치':
			category = 'geocode';
			break;
			
		case '단어':
		case '영어단어':
			category = 'dictionary';
			break;
			
		case '맛집':
		case '음식점':
			category = 'local';
			break;
			
		default:
			break;
		}
		crawling_text = keywordList[i][1];
		url = getUrl(category, encodeURI(crawling_text), 1);
		var list = [url , category, crawling_text];
		url_category_text.push(list);
		
	}
	dialogFunction(url_category_text);
});

function dialogFunction(url_category_text) {
	
	$("#dialog").dialog({
				open : function() {
					$(this).load(url_category_text[curIndex][0]);
					doWhenDialogLoad();
					 $("#dialog").dialog().parents(".ui-dialog").find(".ui-dialog-titlebar").remove();
				},
				width : 400,
				height : 600,
				resizable : false,
				draggable : true,
				buttons: { "previous" : function() {$('#dialog')[0].innerHTML='';if(curIndex == 0){curIndex = url_category_text.length;}$(this).load(url_category_text[--curIndex][0]); doWhenDialogLoad(); } , "next" : function() {$('#dialog')[0].innerHTML=''; if(curIndex == url_category_text.length - 1){curIndex = -1;}$(this).load(url_category_text[++curIndex][0]); doWhenDialogLoad(); } 
				, "close": function() { $(this).dialog("close"); } } 
			});
}

function doWhenDialogLoad() {
	
	$('.price').each(function (){
		var item = $(this).text();
		var num = Number(item).toLocaleString('kr');
		$(this).text(num);
	});

	$('.clamp3line').each(function(){
		$clamp($(this), {clamp: 3});
	});
	
	$('#dialog').scroll(function() {
		
		if ($(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight) {
			var curCnt = $('#dialog').children('li').length;
			var max = $('#total')[0].innerText;
			const NAVER_API_MAX_CNT = 1000;
			
			if(curCnt >= max || curCnt >= NAVER_API_MAX_CNT) {
				return;
			}
			
			$('#loading').css('display', 'block');
			var loadingHTML = $('#loading').innerHTML;
			$.ajax({
				url: getUrl(url_category_text[curIndex][1], url_category_text[curIndex][2], curCnt + 1, ++pageNo),
				success: function(data) {
					$('#loading').remove();
					$("#dialog").append(data);
					$("#dialog").append(loadingHTML);
					event.preventDefault();
				},
				error: function(request, status, error) {
					$('#loading').remove();
					$('#dialog body').append(loadingHTML);
					alert("code:"+request.status+"\n"+"message:"+request.responseText+"\n"+"error:"+error);
				}
			});
		}
	});
}
