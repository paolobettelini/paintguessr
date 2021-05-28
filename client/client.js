const TOKEN_SERVED	= 0;	// token
const JOIN_GAME		= 1;	// token, username
const CREATE_GAME	= 2;	// public, rounds, duration, max_players, username
const START_GAME	= 3;	// -

const DRAW_BUFFER	= 20;	// point...
const END_DRAWING	= 21;	// -

const JOIN_ERROR	= 201;	// reason
const CREATE_ERROR	= 202;	// reason
const START_ERROR	= 203;	// reason

const server = new WebSocket('ws://127.0.0.1:3333');
server.binaryType = "arraybuffer";
  
server.onopen = function(e) {
	console.log("Connection established");
};

server.onmessage = function(e) {
	var data = new Uint8Array(e.data);
	console.log(data[0])
	drawLineBuf(data)
};

server.onclose = function(e) {
	console.log("Connection closed", e);
};

server.onerror = function(e) {
	console.log("WebSocket Error: ", e);
};

function sendToServer(data) {
	server.send(data);
}

function create() {
	server.send("create Paolo");
}

function join(token) {
	server.send("join " + token + " username");
}

/*
var buffer = new ArrayBuffer(10);
var bytes = new Uint8Array(buffer);
for (var i=0; i<bytes.length; i++) {
 bytes[i] = i;
}
server.send(buffer);
*/