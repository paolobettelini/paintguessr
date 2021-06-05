const TOKEN_SERVED	= 0;	// token
const JOIN_GAME		= 1;	// token, username
const CREATE_GAME	= 2;	// public, rounds, duration, max_players, username
const JOIN_RND		= 3		// username
const START_GAME	= 4;	// -
const PLAYER_JOIN	= 5;	// -

const DRAW_BUFFER	= 20;	// point...
const MOUSE_UP		= 21;	// -
const SET_COLOR		= 22;	// r, g, b
const SET_WIDTH		= 23;	// line width
// DRAW_POINT
// BUCKET

const MSG			= 30;	// Chat message

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

		leaderboard = {};
	} else if (cmd == PLAYER_JOIN) {
		var name = "";
		for (var i = 0; i < data.length - 1; i++) {
			name += String.fromCharCode(data[i + 1]);
		}
		
		leaderboard[name] = 0;
		displayLeaderboard();
	} else if (cmd == DRAW_BUFFER) {
		drawLineBuf(data.slice(1, data.length));
	} else if (cmd == MOUSE_UP) {
		mouseUp();
	} else if (cmd == MSG) {
		var msg = "";
		for (var i = 0; i < data.length - 1; i++) {
			msg += String.fromCharCode(data[i + 1]);
		}
		displayMessage(msg);
	} else if (cmd == SET_COLOR) {
		ctx.strokeStyle = 'rgb(' + data[1] + ',' + data[2] + ',' + data[3] + ')';
	} else if (cmd == SET_WIDTH) {
		ctx.lineWidth = data[1];
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

function joinRandom() {
	var packet = new ArrayBuffer(1 + username.length);
	var view = new Uint8Array(packet);
	view[0] = JOIN_RND;
	for (var i = 0; i < username.length; i++) {
		view[i + 1] = username.charCodeAt(i);
	}
	sendToServer(packet);
}

function createGame() {
	if (username == '') {
		console.log("username null XXXX")
		return;
	}

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

var textinput = document.getElementById('textinput');

function sendMessage() {
	var msg = textinput.value;
	if (msg == '') {
		return;
	}
	textinput.value = '';
	var value = username + ": " + msg;
	var packet = new ArrayBuffer(1 + value.length);
	var view = new Uint8Array(packet);
	view[0] = MSG;
	for (var i = 0; i < value.length; i++) {
		view[i + 1] = value.charCodeAt(i);
	}
	sendToServer(packet);
}

var textarea = document.getElementById('textarea');

function displayMessage(msg) {
	textarea.append(msg + '\n');
}