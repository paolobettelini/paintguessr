const GAME_SERVED	= 0;	// token, public, max_players, rounds, turn_duration
const JOIN_GAME		= 1;	// token, username
const CREATE_GAME	= 2;	// public, rounds, turn_duration, max_players, username
const JOIN_RND		= 3		// username
const START			= 4;	// -
const PLAYER_JOIN	= 5;	// username
const PLAYER_LEFT	= 6;	// username
const NEXT_TURN		= 7;	// drawing, word

const DRAW_BUFFER	= 20;	// point...
const MOUSE_UP		= 21;	// -
const SET_COLOR		= 22;	// r, g, b
const SET_WIDTH		= 23;	// line width

const MSG			= 30;	// Chat message
const ADD_SCORE		= 31;	// amount, username

const JOIN_ERROR	= 201;	// reason

const server = new WebSocket('ws://83.79.53.229:4242');
//const server = new WebSocket('ws://localhost:3333');

server.binaryType = "arraybuffer";
  
server.onopen = function(e) {
	console.log("Connection established");
};

server.onmessage = function(e) {
	var data = new Uint8Array(e.data);
	var cmd = data[0];

	if (cmd == GAME_SERVED) {
		var token = "";
		for (var i = 0; i < 5; i++) {
			token += String.fromCharCode(data[i + 1]);
		}

		//public = data[5 + 1] != 0;
		maxPlayers = data[5 + 2] & 0xFF
		rounds = data[5 + 3] & 0xFF;
		turnDuration = data[5 + 4] & 0xFF;
		
		console.log("rounds: " + rounds);
		console.log("turnDuration: " + turnDuration);

		document.getElementById('token').innerHTML = "Token: " + token + "";

		if (creator) {
			document.getElementById('startButton').style.display = 'block';
			document.getElementById('startButton').disabled = true;
		}

		// Toggle to game view
		showGameSection();

		leaderboard = {};
	} else if (cmd == PLAYER_JOIN) {
		var name = "";
		for (var i = 0; i < data.length - 1; i++) {
			name += String.fromCharCode(data[i + 1]);
		}

		if (++players > 1) {
			document.getElementById('startButton').disabled = false;
		}

		setStatus("Waiting... (" + players + "/" + maxPlayers + ")");

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
	} else if (cmd == NEXT_TURN) {
		++currentTurn;
		drawing = data[1] != 0;
		var word = "";
		for (var i = 0; i < data.length - 2; i++) {
			word += String.fromCharCode(data[i + 2]);
		}

		// clear canvas
		ctx.fillStyle = "white";
		ctx.fillRect(0, 0, width, height);

		document.getElementById('word').innerHTML = "Word: " + word;
		setStatus("Playing... " + (currentTurn / maxPlayers | 0));

		mouseUp();

		if (drawing) {
			setTimeout(() => {
				// enable features
				drawing = true;
				
				setTimeout(() => {
					drawing = false;
				}, turnDuration * 1000);
			}, 3000);
		}
	} else if (cmd == JOIN_ERROR) {
		var msg = "";
		for (var i = 0; i < data.length - 1; i++) {
			msg += String.fromCharCode(data[i + 1]);
		}

		alert(msg);
	} else if (cmd == ADD_SCORE) {
		var amount = data[1] & 0xFF;
		var name = "";
		for (var i = 0; i < data.length - 2; i++) {
			name += String.fromCharCode(data[i + 2]);
		}

		leaderboard[name] += amount;
		displayLeaderboard();
	}
};

server.onclose = function(e) {

};

server.onerror = function(e) {
	//alert('Connection error');
};

function sendToServer(data) {
	server.send(data);
}

function joinGame(token) {
	if (username.length == 0) {
		alert("Username cannot be empty")
		return;
	}

	creator = false;

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
	if (username.length == 0) {
		alert("Username cannot be empty")
		return;
	}

	creator = false;

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
		alert("Username cannot be empty")
		return;
	}

	creator = true;

	var packet = new ArrayBuffer(5 + username.length);
	var view = new Uint8Array(packet);
	view[0] = CREATE_GAME;
	view[1] = public ? ~ 0 : 0;
	view[2] = maxPlayers;
	view[3] = rounds;
	view[4] = turnDuration;
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
	var packet = new ArrayBuffer(1 + msg.length);
	var view = new Uint8Array(packet);
	view[0] = MSG;
	for (var i = 0; i < msg.length; i++) {
		view[i + 1] = msg.charCodeAt(i);
	}
	sendToServer(packet);
}

var textarea = document.getElementById('textarea');

function start() {
	var packet = new ArrayBuffer(1);
	var view = new Uint8Array(packet);
	view[0] = START
	sendToServer(view);
}

function displayMessage(msg) {
	textarea.append(msg + '\n');
}

function setStatus(status) {
	document.getElementById('status').innerHTML = status;
}