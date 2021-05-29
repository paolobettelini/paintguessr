const TOKEN_SERVED	= 0;	// token
const JOIN_GAME		= 1;	// token, username
const CREATE_GAME	= 2;	// public, rounds, duration, max_players, username
const START_GAME	= 3;	// -
const PLAYER_JOIN	= 4;	// -

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
	var cmd = data[0];

	if (cmd == TOKEN_SERVED) {
		var token = "";
		for (var i = 0; i < 5; i++) {
			token += String.fromCharCode(data[i + 1]);
		}
		console.log("token served: " + token);

		// Toggle to game view
		showGameSection();
	} else if (cmd == PLAYER_JOIN) {
		var name = "";
		for (var i = 0; i < data.length - 1; i++) {
			name += String.fromCharCode(data[i + 1]);
		}
		console.log("player: " + name);
	}
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

function joinGame(token) {
	if (username.length == 0) {
		console.log("invalid username");
		return;
	}

	var packet = new ArrayBuffer(1 + 5 + username.length);
	var view = new Uint8Array(packet);
	view[0] = JOIN_GAME;
	for (var i = 0; i < 5; i++) {
		view[i + 1] = token.charCodeAt(i);
	}
	for (var i = 0; i < username.length; i++) {
		view[i + 6] = username.charCodeAt(i);
	}
	sendToServer(packet);
}

function createGame() {
	var packet = new ArrayBuffer(5 + username.length);
	var view = new Uint8Array(packet);
	view[0] = CREATE_GAME;
	view[1] = public ? ~0 : 0;
	view[2] = maxPlayers;
	view[3] = rounds;
	view[4] = roundDuration;
	for (var i = 0; i < username.length; i++) {
		view[i + 5] = username.charCodeAt(i);
	}
	sendToServer(packet);
}