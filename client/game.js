var canvas = document.getElementById('canvas');
var ctx = canvas.getContext('2d');
var width = canvas.width;
var height = canvas.height;

var drawing = false;

ctx.lineWidth = 2;
ctx.strokeStyle = 'black';

let counter = 0;
const BLOCK_SIZE = 7;
var buffer = new ArrayBuffer(BLOCK_SIZE * 4);

canvas.onmousemove = e => {
	if (drawing) {
		if (counter++ % BLOCK_SIZE == 0 && counter != 0) {
			sendToServer(new Blob([buffer])); // Arriva tutto 0 al server
			//drawLineBuf(buffer)
		}
		
		var w = 65535 * (e.offsetX / width) | 0;
		var h = 65535 * (e.offsetY / height) | 0;
		buffer[((counter % BLOCK_SIZE) << 2) + 0] = w >>8;
		buffer[((counter % BLOCK_SIZE) << 2) + 1] = w & 0xFF;
		buffer[((counter % BLOCK_SIZE) << 2) + 2] = h >> 8;
		buffer[((counter % BLOCK_SIZE) << 2) + 3] = h & 0xFF;
	}
}
canvas.onmousedown = e => {
	drawing = true;
	x = e.offsetX
	y = e.offsetY
}

var x0 = -1, y0 = -1;

canvas.onmouseup = e => { // Mandare ai giocatori questo evento
	drawing = false;
	x0 = y0 = -1;
}

function drawLineBuf(buf) {
	ctx.beginPath();

	if (x0 == -1) {
		x0 = (buf[0] << 8 | buf[1]) / 65535 * width;
		y0 = (buf[2] << 8 | buf[3]) / 65535 * height;
	}
	
	ctx.moveTo(x0, y0);
	
	for (var i = 4; i < BLOCK_SIZE << 2; i += 4) {
		var x1 = (buf[i] << 8 | buf[i + 1]) / 65535 * width;
		var y1 = (buf[i + 2] << 8 | buf[i + 3]) / 65535 * height;
		console.log(x0 + " " + y0)
		
		ctx.lineTo(x1, y1);
		
		x0 = x1;
		y0 = y1;
	}
	
	ctx.stroke();
}