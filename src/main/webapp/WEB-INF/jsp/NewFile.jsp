<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>Insert title here</title>
</head>
<body>
<script>

function textToHTML() {
    sHtml = document.getElementById('content').value;

    var sContent = sHtml, aTemp = null;
    
    // applyConverter에서 추가한 sTmpStr를 잠시 제거해준다. sTmpStr도 하나의 string으로 인식하는 경우가 있기 때문.
    aTemp = sContent.match('@[0-9]+@');
    if (aTemp !== null) {
        sContent = sContent.replace(aTemp[0], "");
    }
            
    sContent =  sContent.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/ /g, '&nbsp;');
    sContent = addLineBreaker(sContent);

    if (aTemp !== null) {
        sContent = aTemp[0] + sContent;
    }
    
    document.getElementById('content').value = sContent;
}

function addLineBreaker(sContent){
    var oContent = '';
        aContent = sContent.split('\n'); // \n을 기준으로 블럭을 나눈다.
        aContentLng = aContent.length; 
        sTemp = "";
    
    for (var i = 0; i < aContentLng; i++) {
        //sTemp = jindo.$S(aContent[i]).trim().$value();
        sTemp = aContent[i].trim();
        if (i === aContentLng -1 && sTemp === "") {
            break;
        }
        
        if (sTemp !== null && sTemp !== "") {
            oContent +='<P>';
            oContent += aContent[i];
            oContent += '</P>';
        } else {
            oContent += '<P><BR></P>';
        }
    }
    
    return oContent.toString();
}

</script>


<textarea name="content" id="content"
                    style="margin: 8px; min-height: 500px; min-width: 700px; padding: 0px;"></textarea>
                    
            
            <button id = "temp" onclick="textToHTML()"></button>        
</body>


</html>