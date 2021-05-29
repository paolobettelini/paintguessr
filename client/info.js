var maxPlayersDisplay = document.getElementById('maxPlayersDisplay');
var publicRadio = document.getElementById('public');
var roundsDisplay = document.getElementById('roundsDisplay');
var roundDurationDisplay = document.getElementById('roundDurationDisplay');

var public = true;
var maxPlayers = 5;
var username = "";
var rounds = 3;
var roundDuration = 30;

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

function setRoundDuration(v) {
    roundDuration = v;
    roundDurationDisplay.innerHTML = "Round Duration (" + v + "s)";
}