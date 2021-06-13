var countdown = document.getElementById('timeLeft');
var canvas = document.getElementById('canvas');
var table = document.getElementById('leaderboard');
var widthInput = document.getElementById('rangeWidth');
var colorInput = document.getElementById('changeColor');
var dot = document.getElementById('circle');
var ctx = canvas.getContext('2d');
var width = canvas.width;
var height = canvas.height;

var dragging = false;
var drawing = false;
var leaderboard;
var playersWhoWonTheTurn = [];
var timerTask;
var secondsLeft = 0;
var currentTurn = 0;
var creator = false;
var players = 0;

ctx.lineCap = 'round';
colorInput.disabled = true;
widthInput.disabled = true;

function gameOver() {
	clearInterval(timerTask);
	showGameOver(leaderboard);
}

function displayLeaderboard() {
	var keys = keys = Object.keys(leaderboard);
	keys = keys.sort((a, b) => leaderboard[a] > leaderboard[b] ? -1 : 1);

	var html = '';
	
	for (var i = 0; i < keys.length; i++) {
		html += '<tr' + (playersWhoWonTheTurn.includes(keys[i]) ? ' class="lightgreen"' : '') + '>';
		html += '<td>' + (i + 1) + '.';
		html += '<td>' + keys[i];
		html += '<td>' + leaderboard[keys[i]];
		html += '</tr>';
	}

	table.innerHTML = html;
}

function setColor(v) {
	if (drawing) {
		ctx.strokeStyle = v;

		var packet = new ArrayBuffer(4);
		var view = new Uint8Array(packet);
		view[0] = SET_COLOR;
	
		view[1] = parseInt(v.substring(1, 3), 16);
		view[2] = parseInt(v.substring(3, 5), 16);
		view[3] = parseInt(v.substring(5, 7), 16);

		sendToServer(packet);
	}
}

function updateDot(v) {
	if (drawing) {
		dot.setAttribute('r', v / 2);
	}
}

function setWidth(v) {
	if (drawing) {
		ctx.lineWidth = v;

		var packet = new ArrayBuffer(2);
		var view = new Uint8Array(packet);
		view[0] = SET_WIDTH;
		view[1] = v;
		sendToServer(packet);
	}
}

let counter = 0;
const BLOCK_SIZE = 7;

var buffer = new ArrayBuffer(1 + BLOCK_SIZE * 4);

var byteBuffer = new Uint8Array(buffer);

byteBuffer[0] = DRAW_BUFFER;

canvas.onmousemove = e => {
	var x = e.offsetX;
	var y = e.offsetY;
	if (drawing && dragging && y >= 0 && x >= 0) {
		if (counter % BLOCK_SIZE == 0 && counter != 0) {
			sendToServer(byteBuffer);
			drawLineBuf(byteBuffer.slice(1, byteBuffer.length));
		}
		
		var w = 65535 * (x / width) | 0;
		var h = 65535 * (y / height) | 0;
		byteBuffer[((counter % BLOCK_SIZE) << 2) + 1] = w >>> 8;
		byteBuffer[((counter % BLOCK_SIZE) << 2) + 2] = w & 0xFF;
		byteBuffer[((counter % BLOCK_SIZE) << 2) + 3] = h >>> 8;
		byteBuffer[((counter % BLOCK_SIZE) << 2) + 4] = h & 0xFF;

		++counter;
	}
}
canvas.onmousedown = e => {
	dragging = true;
	x = e.offsetX
	y = e.offsetY
}

var x0 = -1, y0 = -1;
var initLine = true;

canvas.onmouseup = e => {
	if (drawing) {
		console.log("counter: " + counter);
		if (--counter % BLOCK_SIZE != 0) { // flush remaining buffer data
			//var v = 1 + ((counter % BLOCK_SIZE) << 2);
			//sendToServer(byteBuffer.slice(0, v))
			//drawLineBuf(byteBuffer.slice(1, v))
		}
		
		mouseUp();
		
		var packet = new ArrayBuffer(1);
		var view = new Uint8Array(packet);
		view[0] = MOUSE_UP;
		sendToServer(packet);
	}
}

function nextTurn() {
	if (timerTask != null) {
		clearInterval(timerTask);
		timerTask = null;
	}
	
	mouseUp();
	
	secondsLeft = turnDuration;
	
	countdown.innerHTML = "Time left " + secondsLeft;
	timerTask = setInterval(() => {
		countdown.innerHTML = "Time left " + --secondsLeft;
	}, 1000);
}

function mouseUp() {
	dragging = false;
	initLine = true;
	counter = 0;
}

function drawLineBuf(buf) {
	ctx.beginPath();
	
	if (initLine) {
		initLine = false;
		
		x0 = (buf[0] << 8 | buf[1]) / 65535 * width;
		y0 = (buf[2] << 8 | buf[3]) / 65535 * height;
	}
	
	ctx.moveTo(x0, y0);
	
	for (var i = 4; i < BLOCK_SIZE << 2; i += 4) {
		var x1 = (buf[i] << 8 | buf[i + 1]) / 65535 * width;
		var y1 = (buf[i + 2] << 8 | buf[i + 3]) / 65535 * height;
		
		ctx.lineTo(x1, y1);
		
		x0 = x1;
		y0 = y1;
	}
	
	ctx.stroke();
}