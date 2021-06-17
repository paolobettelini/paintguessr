const server = new WebSocket('ws://XXX.XXX.XXX.XXX:PORT'); // Modify this line

// Avoid Blob conversion
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

// Decoder use to decode UTF-8 data
var decoder = new TextDecoder();

// Manage packet reception
server.onmessage = e => {
	var data = new Uint8Array(e.data);

	switch (data[0]) {
		case GAME_SERVED:
			var token = "";
			// Read token from packet
			for (var i = 0; i < 5; i++) {
				token += String.fromCharCode(data[i + 1]);
			}
	
			// Read room settings
			public = data[5 + 1] != 0;
			maxPlayers = data[5 + 2] & 0xFF
			rounds = data[5 + 3] & 0xFF;
			turnDuration = data[5 + 4] & 0xFF;
	
			// Display token
			document.getElementById('token').innerHTML = "Token: " + token + "";
	
			if (creator) {
				// Display start button
				document.getElementById('startButton').style.display = 'block';
				document.getElementById('startButton').disabled = true;
			}
	
			// Toggle to game view
			showGameSection();
	
			leaderboard = {};
			break;
		case PLAYER_JOIN:
			var name = "";
			// Read the name of the player from packet
			for (var i = 0; i < data.length - 1; i++) {
				name += String.fromCharCode(data[i + 1]);
			}

			if (++players > 1) {
				// Enable the start button
				document.getElementById('startButton').disabled = false;
			}

			// Update status
			setStatus("Waiting... (" + players + "/" + maxPlayers + ")");

			// Add the new player to the leaderboard
			leaderboard[name] = 0;
			displayLeaderboard(leaderboard);
			break;
		case DRAW_BUFFER:
			// Draw the buffer data
			drawLineBuf(data.slice(1, data.length));
			break;
		case MOUSE_UP:
			mouseUp();
			pushLine();
			break;
		case MSG:
			// Display message on the chat
			displayMessage(decoder.decode(data.slice(2, data.length)), undefined, data[1] != 0 ? '#37b34e' : undefined);
			break;
		case SET_COLOR:
			// Set the brush color
			ctx.strokeStyle = 'rgb(' + data[1] + ',' + data[2] + ',' + data[3] + ')';
			break;
		case SET_WIDTH:
			// Set the brush width
			setWidth(data[1], false);
			break;
		case NEXT_TURN:
			// Update turn/round variables
			++currentTurn;
			if (currentTurn == players + 1) {
				currentTurn = 1;
			}
			if (currentTurn == 1) {
				++currentRound;
			}
			// Check if you're the next one drawing
			drawing = data[1] != 0;
			var word = "";
			// Read the word from the packet
			for (var i = 0; i < data.length - 2; i++) {
				word += String.fromCharCode(data[i + 2]);
			}
	
			// Clear canvas
			clearCanvas();
	
			// Reset brush settings
			ctx.strokeStyle = "#000000";
			setWidth(5, false);
	
			// Disable/Enable brush control
			colorInput.disabled = !drawing;
			widthInput.disabled = !drawing;
	
			// Reset history
			lineHistory = [];
			currentLine = [];
			historyPos = 0;
	
			// Reset leaderboard winnings
			playersWhoWonTheTurn = [];
			displayLeaderboard(leaderboard);
	
			// Display the word
			document.getElementById('word').innerHTML = word;
			
			// Update the status
			setStatus("Round " + currentRound + "/" + rounds + " Turn " + currentTurn + "/" + players);
			
			if (currentTurn == 1) {
				// Remove the start button
				startButton.style.display = 'none';
			}
	
			// Handle scheduler tasks
			nextTurn();
			break;
		case JOIN_ERROR:
			var msg = "";
			// Read the message from the packet
			for (var i = 0; i < data.length - 1; i++) {
				msg += String.fromCharCode(data[i + 1]);
			}

			alert(msg);
			break;
		case ADD_SCORE:
			var amount = data[1] & 0xFF;
			var name = "";
			// Read the name of the receiver
			for (var i = 0; i < data.length - 2; i++) {
				name += String.fromCharCode(data[i + 2]);
			}
	
			playersWhoWonTheTurn.push(name);
	
			displayMessage(name + ' guessed the word', '#90EE90');
	
			leaderboard[name] += amount;
			
			// Do not update the leaderboard if this is going to cause the next turn to start
			// You'd just see the green for some milliseconds before the new turn reset the leaderboard.
			if (players != playersWhoWonTheTurn.length + 1) {
				displayLeaderboard(leaderboard);
			}
			break;
		case PLAYER_LEFT:
			var name = "";
			// Read the player name
			for (var i = 0; i < data.length - 2; i++) {
				name += String.fromCharCode(data[i + 2]);
			}

			displayMessage(name + ' left the game', '#FA8072');

			--players;

			// If the player was drawing the next turn won't its number
			// eg. (2/5) -> (2/4)
			if (data[1] != 0) {
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

// Send a packet to the websocket
function sendToServer(data) {
	server.send(data);
}

// Send join game request
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

// Send join random request
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

// Send create game request
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

// Send a message packet
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
	for (var i = 0; i < msg.length; i++) {
		view[i + 1] = msg.charCodeAt(i);
	}
	sendToServer(packet);
}

// Send the start packet
function start() {
	var packet = new ArrayBuffer(1);
	var view = new Uint8Array(packet);
	view[0] = START;
	sendToServer(view);
}