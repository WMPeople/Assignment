/**
 * progress 진행바 클래스 입니다.
 * @author khh
 */

function ProgressBar() {
	this.holder = document.getElementById('progress_holder');
	this.elem = document.getElementById("progressBar");
	
	this.totalCnt;
	this.curCnt = 0;
	
	this.holder.style.display = 'block';
}

ProgressBar.prototype.increseProgress = function (increseCnt, curTaskInfo) {
	this.curCnt += increseCnt;
	var percent = this.curCnt / this.totalCnt * 100;

	if (percent >= 100) {
		this.holder.style.display = 'none';
	} else {
		this.elem.style.width = percent + '%'; 
		this.elem.innerHTML = percent.toFixed(2) + '% ' + this.curCnt + ' / ' + this.totalCnt;
		if(curTaskInfo !== undefined) {
			this.elem.innerHTML += '  ' + curTaskInfo;
		}
	}
}

ProgressBar.prototype.setTotalCnt = function (totalCnt) {
	this.totalCnt = totalCnt;
}