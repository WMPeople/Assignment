$(document).ready(function() {
	function textToHTML() {
		// DB에서 가져오는 게시물 내용값을 넣어줍니다.
	    sHtml = document.getElementById('content_detail').value;
	
	    var sContent = sHtml;
	    // & , < , > ,  스페이스바   각각 변경
	    sContent =  sContent.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/ /g, '&nbsp;');
	    // addLineBreaker : <p>태그와 <br>태그를 이용하여 개행을 표현해주는 메쏘드입니다.
	    sContent = addLineBreaker(sContent); 
	    // 영화 '엑스맨'과 같은 정보 제공이 필요한 텍스트를 span 태그로 만들어서 mouseover 이벤트가 작동하도록 합니다.
	    var copyContent = createTag(sContent);
	    //완성된 html문을 div에 넣어줍니다.
	    document.getElementById('contents').innerHTML = copyContent;
	}
	
	//메타 데이터 제공을 위한 태그 생성
	function createTag (sContent){
		 var copyContent = sContent;
		 var realContent = sContent;
		    var nameList = ["위치", "영화", "책", "도서" , "맛집", "뉴스", "쇼핑", "음식점", "영어단어", "구매", "지도"];
		    var englishNameList = ["geocode", "movie", "book", "book" , "local", "news", "shop", "local", "dictionary", "shop", "geocode"];
		    var nbsp = "&nbsp;"
		    console.log(nbsp.length);
		    for(var i =0; i< nameList.length ; i++){
		        var searchIndex = 0;
		        var copyContentArray = copyContent.split(nameList[i]+nbsp+"'");
		        var copyContentArrayLength = copyContentArray.length;
		        if (copyContentArrayLength >= 2) {
		        	for(var j = 0 ; j< copyContentArrayLength - 1 ; j++){
		        		 var searchIndex = copyContent.search(nameList[i]+nbsp+"'");
				            if(searchIndex!= -1){
				                var spaceIndex = searchIndex + nameList[i].length + nbsp.length - 1;
				                var firstQuoteIndex = spaceIndex + 1;
				                console.log(copyContent.charAt(spaceIndex));
				                console.log(copyContent.charAt(firstQuoteIndex));
				                if(copyContent.charAt(spaceIndex) == ';' && copyContent.charAt(firstQuoteIndex) == "'"){
				                    var startIndex = firstQuoteIndex + 1;
				                    console.log(copyContent.charAt(startIndex));
				                    var secondQuoteIndex = '';
				                    var text = '';
				                    if(copyContent.charAt(startIndex) == "'"){
				                    	secondQuoteIndex = copyContent.substring(startIndex).search("'") + startIndex;
				                    } else {
				                    	console.log(copyContent.substring(startIndex).search("'"));
				                    	secondQuoteIndex = copyContent.substring(startIndex).search("'") + startIndex + 1;
				                    	text = copyContent.substring(firstQuoteIndex + 1,secondQuoteIndex - 1);
				                    }
				                    console.log(copyContent.charAt(secondQuoteIndex));
				                    if (text == "" || text == "''" || text == "'") {
				                    	copyContent = copyContent.replace(nameList[i]+nbsp+"'"+text+"'",'<span> '+nameList[i]+" '"+text +"'"+'</span>');
				                    } else {
				                    	copyContent = copyContent.replace(nameList[i]+nbsp+"'"+text+"'",'<b id= '+englishNameList[i]+' class="'+ text +'" onmouseover="m_over(this)">'+nameList[i]+" '"+text +"'"+'</b>');
				                    }
				                }
				            }  
		        		
		        	}
		        }

		    }
		    return copyContent;
	}
	
	//라인을 나눕니다.
	function addLineBreaker(sContent){
	    var oContent = '';
	    arrayContent = sContent.split('\n');
        arrayContentLength = arrayContent.length; 
	    
	    for (var i = 0; i < arrayContentLength; i++) {
	        sTemp = arrayContent[i].trim();
	        if (sTemp === "") {
	            continue;
	        }
	        
	        if (sTemp !== null && sTemp !== "") {
	            oContent +='<P>';
	            oContent += arrayContent[i];
	            oContent += '</P>';
	        } else {
	            oContent += '<P><BR></P>';
	        }
	    }
	    
	    return oContent.toString();
	}
	textToHTML();
}); 

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
pageNo = 1;

function m_over(target){
	category = target.id;
	crawling_text = target.className;
	var url = getUrl(category, encodeURI(crawling_text), 1);
	dialogFunction(this.category, url);
}  

function dialogFunction(crawling_category, url) {
	console.log(category);
	console.log(crawling_text);
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

	var makeScroolFunc = function maker(category, crawling_text) {
		return function (){
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
					url: getUrl(category, crawling_text, curCnt + 1, ++pageNo),
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
		}
	}
	$('#dialog').off('scroll');
	$('#dialog').scroll(makeScroolFunc(category, crawling_text));
	
	$('.price').each(function (){
		var item = $(this).text();
		var num = Number(item).toLocaleString('kr');
		$(this).text(num);
	});

	$('.clamp3line').each(function(){
		$clamp($(this), {clamp: 3});
	});
	
}
