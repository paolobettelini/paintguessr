// DOM elements
var countdown = document.getElementById('timeLeft');
var canvas = document.getElementById('canvas');
var widthInput = document.getElementById('rangeWidth');
var colorInput = document.getElementById('changeColor');
var dot = document.getElementById('circle');
var ctx = canvas.getContext('2d');
var width = canvas.width;
var height = canvas.height;

// History
var lineHistory = [];
var currentLine = [];

// Game and drawing variables
var dragging = false;
var drawing = false;
var leaderboard;
var playersWhoWonTheTurn = [];
var timerTask;
var secondsLeft = 0;
var currentTurn = 0;
var currentRound = 0;
var creator = false;
var players = 0;

// Set round brush lines
ctx.lineCap = 'round';
ctx.lineJoin = 'round';

// Disable brush settings
colorInput.disabled = true;
widthInput.disabled = true;

 /**
  * Switch to game over leaderboard view
  */
function gameOver() {
	clearInterval(timerTask);
	showGameOver(leaderboard);
}

/**
 * Changes the color and sends a packet to the server to
 * notify it.
 * 
 * @param {string} v the color.
 */
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

/**
 * Changes the width of the brush.
 * 
 * @param {number} v the new width.
 * @param {boolean} send notify the server.
 */
function setWidth(v, send) {
	ctx.lineWidth = v;
	widthInput.value = v;
	dot.setAttribute('r', v / 2);
	
	if (drawing && send) {
		var packet = new ArrayBuffer(2);
		var view = new Uint8Array(packet);
		view[0] = SET_WIDTH;
		view[1] = v;
		sendToServer(packet);
	}
}

/**
 * How much data each packet contains
 */
const BLOCK_SIZE = 10;
let counter = 0;

var buffer = new ArrayBuffer(1 + (BLOCK_SIZE << 2));
var byteBuffer = new Uint8Array(buffer);
byteBuffer[0] = DRAW_BUFFER;

/**
 * Fires when the user moves the mouse over the object.
 * 
 * @param {Ev} e the mouse event.
 */
canvas.onmousemove = e => {
	var x = e.offsetX;
	var y = e.offsetY;
	if (drawing && dragging && y >= 0 && x >= 0 && x < width && y < height) {
		if (counter % BLOCK_SIZE == 0 && counter != 0) { // packet complete
			sendToServer(byteBuffer);
		}

		// draw locally
		if (initLine) {
			initLine = false;
			ctx.moveTo(x, y);
		} else {
			ctx.lineTo(x, y);
			ctx.stroke();
		}

		// store line
		currentLine.push({
			x: x,
			y: y
		});
		
		// add data to packet
		var w = 65535 * (x / width) | 0;
		var h = 65535 * (y / height) | 0;
		byteBuffer[((counter % BLOCK_SIZE) << 2) + 1] = w >>> 8;
		byteBuffer[((counter % BLOCK_SIZE) << 2) + 2] = w & 0xFF;
		byteBuffer[((counter % BLOCK_SIZE) << 2) + 3] = h >>> 8;
		byteBuffer[((counter % BLOCK_SIZE) << 2) + 4] = h & 0xFF;

		++counter;
	}
}

/**
 * Fires when the user clicks the object with either mouse button.
 * 
 * @param {ev} e the mouse event. 
 */
canvas.onmousedown = e => {
	// starting drawing
	dragging = true;
	x = e.offsetX
	y = e.offsetY
	ctx.beginPath();
}

var x0 = -1, y0 = -1;
var initLine = true;

// Undo last line
function undo(send) {
	if (lineHistory.length != 0) {
		lineHistory.pop()
		
		var oldColor = ctx.strokeStyle;
		var oldLineWidth = ctx.lineWidth;

		redraw();

		ctx.fillStyle = oldColor;
		setWidth(oldLineWidth, false);
	
		if (drawing && send) { // Notify the server
			var packet = new ArrayBuffer(1);
			var view = new Uint8Array(packet);
			view[0] = UNDO;
			sendToServer(packet);
		}
	}
}

canvas.onmouseup = _e => {
	if (drawing) {
		if (counter != 0 && --counter % BLOCK_SIZE != 0) { // flush remaining buffer data
			sendToServer(byteBuffer.slice(0, 1 + ((counter % BLOCK_SIZE) << 2)))
		}
		
		mouseUp();
		
		var packet = new ArrayBuffer(1);
		var view = new Uint8Array(packet);
		view[0] = MOUSE_UP;
		sendToServer(packet);
	}
}

/**
 * Removes the current scheduler task and starts a new one
 * for this turn.
 */
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

/**
 * Called when the player ends drawing a line.
 */
function mouseUp() {
	if (dragging) {
		pushLine();
	}

	dragging = false;
	initLine = true;
	counter = 0;
}

/**
 * Pushes a line into history.
 */
function pushLine() {
	lineHistory.push({
		line: currentLine,
		thickness: ctx.lineWidth,
		color: ctx.strokeStyle
	});
	currentLine = [];
}

/**
 * Clears the canvas.
 */
function clearCanvas() {
	ctx.fillStyle = "white";
	ctx.fillRect(0, 0, width, height);
}

/**
 * Draws a line buffer to the canvas.
 * 
 * @param {ArrayBuffer} buf the buffer 
 */
function drawLineBuf(buf) {
	ctx.beginPath();
	
	if (initLine) {
		initLine = false;
		
		x0 = (buf[0] << 8 | buf[1]) / 65535 * width;
		y0 = (buf[2] << 8 | buf[3]) / 65535 * height;

		currentLine.push({
			x: x0,
			y: y0
		});
	}

	ctx.moveTo(x0, y0);
	for (var i = 4; i < buf.length; i += 4) {
		var x1 = (buf[i] << 8 | buf[i + 1]) / 65535 * width;
		var y1 = (buf[i + 2] << 8 | buf[i + 3]) / 65535 * height;
		
		ctx.lineTo(x1, y1);
		
		currentLine.push({
			x: x1,
			y: y1
		});

		x0 = x1;
		y0 = y1;
	}
	
	ctx.stroke();
}

/**
 * Redraws the history to the canvas.
 */
function redraw() {
	clearCanvas();

	for (var i = 0; i < lineHistory.length; i++) {
		ctx.beginPath()
		
		var line = lineHistory[i].line;
		ctx.strokeStyle = lineHistory[i].color;
		ctx.lineWidth = lineHistory[i].thickness;

		for (var j = 0; j < line.length; j++) {
			var point = line[j];
			if (j == 0) {
				ctx.moveTo(point.x, point.y);
			} else {
				ctx.lineTo(point.x, point.y)
			}
		}
		ctx.stroke();
	}
}
