var canvas = document.getElementById('canvas');
var ctx = canvas.getContext('2d');
var width = canvas.width;
var height = canvas.height;

var dragging = false;
var drawing = false;
var leaderboard;

var currentTurn = 0;
var creator = false;
var players = 0;

function displayLeaderboard() {
	var div = document.getElementById('leaderboard');

	var html = "";
	for (var name in leaderboard) {
		html += "<p>" + name + ": " + leaderboard[name] + "</p>";
	}
	div.innerHTML = html;
}

ctx.lineWidth = 5;
ctx.strokeStyle = 'black';
ctx.lineCap = 'round';

function setColor(v) {
	var packet = new ArrayBuffer(4);
	var view = new Uint8Array(packet);
	view[0] = SET_COLOR;
 
	view[1] = parseInt(v.substring(1, 3), 16);
	view[2] = parseInt(v.substring(3, 5), 16);
	view[3] = parseInt(v.substring(5, 7), 16);

	sendToServer(packet);
}

function setWidth(v) {
	var packet = new ArrayBuffer(2);
	var view = new Uint8Array(packet);
	view[0] = SET_WIDTH;
	view[1] = v;
	sendToServer(packet);
}

let counter = 0;
const BLOCK_SIZE = 7;

var buffer = new ArrayBuffer(1 + BLOCK_SIZE * 4);

var byteBuffer = new Uint8Array(buffer);

byteBuffer[0] = DRAW_BUFFER;

canvas.onmousemove = e => {
	if (drawing && dragging) {
		if (counter % BLOCK_SIZE == 0 && counter != 0) {
			sendToServer(byteBuffer);
			//drawLineBuf(byteBuffer);
		}
		
		var w = 65535 * (e.offsetX / width) | 0;
		var h = 65535 * (e.offsetY / height) | 0;
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
	if (counter % BLOCK_SIZE != 0) { // flush remaining data
		// slice it
		//sendToServer(packet)
	}
	
	mouseUp();
	
	var packet = new ArrayBuffer(1);
	var view = new Uint8Array(packet);
	view[0] = MOUSE_UP;
	sendToServer(packet);
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