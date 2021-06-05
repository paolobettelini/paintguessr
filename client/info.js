var maxPlayersDisplay = document.getElementById('maxPlayersDisplay');
var publicRadio = document.getElementById('public');
var roundsDisplay = document.getElementById('roundsDisplay');
var turnDurationDisplay = document.getElementById('turnDurationDisplay');

var public = true;
var maxPlayers = 5;
var username = "";
var rounds = 3;
var turnDuration = 30;

function setUsername(v) {
    username = v;
}

function setMaxPlayers(v) {
    maxPlayersDisplay.innerHTML = 'Max Players: (' + v + ')'
    maxPlayers = v;
}

function updateVisibility() {
    public = publicRadio.checked;
}

function setRounds(v) {
    rounds = v;
    roundsDisplay.innerHTML = "Rounds (" + v + ")";
}

function setTurnDuration(v) {
    turnDuration = v;
    turnDurationDisplay.innerHTML = "Turn Duration (" + v + "s)";
}