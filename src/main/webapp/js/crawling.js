function getUrl(category, crawling_text, startCnt) {
	var url;
	const crawling_api = 'search';
	
	switch(category) {
	case 'book':
	case 'movie':
	case 'news':
	case 'shop':
		url = "/assignment/api/naver/" + crawling_api + "/"
		+ category + "/" + crawling_text + "/" + startCnt;
		break;
	case 'geocode':
	case 'dictionary':
	case 'place':
		url = "/assignment/api/browser/crawling/" + category + "/"
			+ crawling_text;
		break;
	default:
		break;
	}
	return url;
}

var category;
var crawling_text;

$(function() {
	var lines = $('#content3').val().split("\n");
	var firstLine = lines[0];
	var textList = firstLine.split(' ');
	
	if (textList[0] == '책' || textList[0] == '도서') {
		category = 'book';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
	} else if (textList[0] == '영화') {
		category = 'movie';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
	} else if (textList[0] == '뉴스') {
		category = 'news';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
	} else if (textList[0] == '쇼핑' || textList[0] == '구매') {
		category = 'shop';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
	} //네이버 크롤링 
	else if (textList[0] == '지도' || textList[0] == '위치') {
		category = 'geocode';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
	} //네이버 크롤링
	else if (textList[0] == '단어' || textList[0] == '영어단어') {
		category = 'dictionary';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
	} //네이버 크롤링
	else if (textList[0] == '맛집' || textList[0] == '음식점') {
		category = 'place';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
	}

	var url = getUrl(category, encodeURI(crawling_text), 1);
	dialogFunction(category, url);
});

function dialogFunction(crawling_category, url) {
	$("#dialog").dialog({
				open : function() {
					$(this).load(url);
					doWhenDialogLoad(this, category, crawling_text);
				},
				width : 400,
				height : 600,
				resizable : false,
				draggable : true
			});
}

function doWhenDialogLoad(thisPtr, category, crawling_text) {
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
				url: getUrl(category, crawling_text, curCnt + 1),
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
