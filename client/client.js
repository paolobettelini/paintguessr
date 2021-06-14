const server = new WebSocket('ws://83.79.53.229:3333');

server.binaryType = "arraybuffer";

server.onopen = _e => {
	console.log("Connection established");
};

server.onclose = _e => {
	console.log("Server closed");
};

server.onerror = _e => {
	console.log("Connection error");
	alert('Connection error');
};

var decoder = new TextDecoder();

server.onmessage = e => {
	var data = new Uint8Array(e.data);

	switch (data[0]) {
		case GAME_SERVED:
			var token = "";
			for (var i = 0; i < 5; i++) {
				token += String.fromCharCode(data[i + 1]);
			}
	
			public = data[5 + 1] != 0;
			maxPlayers = data[5 + 2] & 0xFF
			rounds = data[5 + 3] & 0xFF;
			turnDuration = data[5 + 4] & 0xFF;
	
			document.getElementById('token').innerHTML = "Token: " + token + "";
	
			if (creator) {
				document.getElementById('startButton').style.display = 'block';
				document.getElementById('startButton').disabled = true;
			}
	
			// Toggle to game view
			showGameSection();
	
			leaderboard = {};
			break;
		case PLAYER_JOIN:
			var name = "";
			for (var i = 0; i < data.length - 1; i++) {
				name += String.fromCharCode(data[i + 1]);
			}

			if (++players > 1) {
				document.getElementById('startButton').disabled = false;
			}

			setStatus("Waiting... (" + players + "/" + maxPlayers + ")");

			leaderboard[name] = 0;
			displayLeaderboard(leaderboard);
			break;
		case DRAW_BUFFER:
			drawLineBuf(data.slice(1, data.length));
			break;
		case MOUSE_UP:
			mouseUp();
			pushLine();	
			break;
		case MSG:
			var spectator = data[1] != 0;
			var msg = decoder.decode(data.slice(2, data.length));
			displayMessage(msg, undefined, spectator ? '#37b34e' : undefined);
			break;
		case SET_COLOR:
			ctx.strokeStyle = 'rgb(' + data[1] + ',' + data[2] + ',' + data[3] + ')';
			break;
		case SET_WIDTH:
			setWidth(data[1], false);
			break;
		case NEXT_TURN:
			++currentTurn;
			if (currentTurn == players + 1) {
				currentTurn = 1;
			}
			if (currentTurn == 1) {
				++currentRound;
			}
			drawing = data[1] != 0;
			var word = "";
			for (var i = 0; i < data.length - 2; i++) {
				word += String.fromCharCode(data[i + 2]);
			}
	
			// clear canvas
			clearCanvas();
	
			ctx.strokeStyle = "#000000";
			setWidth(5, false);
	
			colorInput.disabled = !drawing;
			widthInput.disabled = !drawing;
	
			lineHistory = [];
			currentLine = [];
			historyPos = 0;
	
			playersWhoWonTheTurn = [];
			displayLeaderboard(leaderboard);
	
			document.getElementById('word').innerHTML = word;
			
			setStatus("Round " + currentRound + "/" + rounds + " Turn " + currentTurn + "/" + players);
			
			if (currentTurn == 1) {
				startButton.style.display = 'none';
			}
	
			nextTurn();
			break;
		case JOIN_ERROR:
			var msg = "";
			for (var i = 0; i < data.length - 1; i++) {
				msg += String.fromCharCode(data[i + 1]);
			}
	
			alert(msg);
			break;
		case ADD_SCORE:
			var amount = data[1] & 0xFF;
			var name = "";
			for (var i = 0; i < data.length - 2; i++) {
				name += String.fromCharCode(data[i + 2]);
			}
	
			playersWhoWonTheTurn.push(name);
	
			displayMessage(name + ' guessed the word', '#90EE90');
	
			leaderboard[name] += amount;
			
			if (players != playersWhoWonTheTurn.length + 1) {
				displayLeaderboard(leaderboard);
			}
			break;
		case PLAYER_LEFT:
			var name = "";
			for (var i = 0; i < data.length - 2; i++) {
				name += String.fromCharCode(data[i + 2]);
			}
			displayMessage(name + ' left the game', '#FA8072');
			--players;
			if (data[1] != 0) { // if the drawing player left
				--currentTurn;
				if (currentTurn == 0) {
					--currentRound;
				}
			} else {
				setStatus("Round " + currentRound + "/" + rounds + " Turn " + currentTurn + "/" + players);
			}
			break;
		case GAME_OVER:
			gameOver();
			break;
		case UNDO:
			undo(false);
			break;
	}
};

function sendToServer(data) {
	server.send(data);
}

function joinGame(token) {
	if (username.length == 0) {
		alert('Username cannot be empty')
		return;
	}

	if (!username.match(/^[a-zA-Z0-9._]{0,20}$/)) {
		alert('Invalid username');
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
		alert('Username cannot be empty');
		return;
	}

	if (!username.match(/^[a-zA-Z0-9._]{0,20}$/)) {
		alert('Invalid username');
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
		alert('Username cannot be empty')
		return;
	}

	if (!username.match(/^[a-zA-Z0-9._]{0,20}$/)) {
		alert('Invalid username');
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

function sendMessage() {
	var msg = textInput.value;
	if (msg == '') {
		return;
	}
	textInput.value = '';
	if (msg.length > 40) {
		alert('Messages can be only 40 characters')
		return;
	}

	var packet = new ArrayBuffer(1 + msg.length);
	var view = new Uint8Array(packet);
	view[0] = MSG;
	//var encoded = new TextEncoder().encode(msg);
	for (var i = 0; i < msg.length; i++) {
		view[i + 1] = msg.charCodeAt(i);
	}
	sendToServer(packet);
}

function start() {
	var packet = new ArrayBuffer(1);
	var view = new Uint8Array(packet);
	view[0] = START
	sendToServer(view);
}