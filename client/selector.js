function el(id) {
	return document.getElementById(id);
}

var homeSection = el('home');
var createSection = el('create');
var gameSection = el('game');
var gameOverSection = el('gameOver')

var createSectionUsername = el('createSectionUsername');
var homeSectionUsername = el('homeSectionUsername');
var gameOverLeaderboard = el('gameOverLeaderboard');

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
	colors.push('#FFD700'); //gold
	colors.push('#C0C0C0'); //bronze
	colors.push('#cd7f32'); //silver
	
	for (var i = 0; i < keys.length; i++) {
		html += '<tr' + (i < 3 ? ' style="background-color: ' + colors[i] + '"' : '') + '>';
		html += '<td>' + (i + 1) + '.';
		html += '<td>' + keys[i];
		html += '<td>' + leaderboard[keys[i]];
		html += '</tr>';
	}

	gameOverLeaderboard.innerHTML = html;
}