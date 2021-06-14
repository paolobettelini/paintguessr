function el(id) {
	return document.getElementById(id);
}

var maxPlayersDisplay = el('maxPlayersDisplay');
var publicRadio = el('option-1');
var roundsDisplay = el('roundsDisplay');
var turnDurationDisplay = el('turnDurationDisplay');

var public = true;
var maxPlayers = 5;
var username = "";
var rounds = 3;
var turnDuration = 30;

var homeSection = el('home');
var createSection = el('create');
var gameSection = el('game');
var gameOverSection = el('gameOver')

var createSectionUsername = el('createSectionUsername');
var homeSectionUsername = el('homeSectionUsername');
var gameOverLeaderboard = el('gameOverLeaderboard');

var textInput = el('textInput');
var textarea = el('textarea');

var table = el('leaderboard');

document.addEventListener('keydown', e => {
    if (drawing && e.key == 'z' && e.ctrlKey) {
        undo(true);
    }
});

textInput.addEventListener('keydown', e => {
	if (e.key == "Enter") {
		sendMessage();
	}
});

function displayLeaderboard(leaderboard) {
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

function displayMessage(msg, backColor, foreColor) {
	var element = document.createElement('p');
	element.appendChild(document.createTextNode(msg));
	if (backColor != undefined) {
		element.style.backgroundColor = backColor;
		element.style.fontWeight = 'bold';
	}
	if (foreColor != null) {
		element.style.color = foreColor;
	}
	textarea.appendChild(element)
	textarea.scrollTop = textarea.scrollHeight;
}

function setStatus(status) {
	document.getElementById('status').innerHTML = status;
}

function setUsername(v) {
	username = v;
}

function setMaxPlayers(v) {
	maxPlayersDisplay.innerHTML = 'Max Players: (' + v + ')'
	maxPlayers = v;
}

function updateVisibility() {
	public = publicRadio.checked;
	console.log(public);
}

function setRounds(v) {
	rounds = v;
	roundsDisplay.innerHTML = "Rounds (" + v + ")";
}

function setTurnDuration(v) {
	turnDuration = v;
	turnDurationDisplay.innerHTML = "Turn Duration (" + v + "s)";
}

function showHomeSection() {
	homeSection.style.display = 'block';
	createSection.style.display = 'none';
	gameSection.style.display = 'none';
	gameOverSection.style.display = 'none';
	homeSectionUsername.value = username;
}

function showCreateSection() {
	homeSection.style.display = 'none';
	createSection.style.display = 'block';
	gameSection.style.display = 'none';
	gameOverSection.style.display = 'none';
	createSectionUsername.value = username;
}

function showGameSection() {
	homeSection.style.display = 'none';
	createSection.style.display = 'none';
	gameSection.style.display = 'block';
	gameOverSection.style.display = 'none';
}

function showGameOver(leaderboard) {
	homeSection.style.display = 'none';
	createSection.style.display = 'none';
	gameSection.style.display = 'none';
	gameOverSection.style.display = 'block';
	
	var keys = keys = Object.keys(leaderboard);
	keys = keys.sort((a, b) => leaderboard[a] > leaderboard[b] ? -1 : 1);

	var html = '';
	var colors = [];
	colors.push('#FFD700'); // gold
	colors.push('#C0C0C0'); // bronze
	colors.push('#cd7f32'); // silver
	
	for (var i = 0; i < keys.length; i++) {
		html += '<tr' + (i < 3 ? ' style="background-color: ' + colors[i] + '"' : '') + '>';
		html += '<td>' + (i + 1) + '.';
		html += '<td>' + keys[i];
		html += '<td>' + leaderboard[keys[i]];
		html += '</tr>';
	}

	gameOverLeaderboard.innerHTML = html;
}