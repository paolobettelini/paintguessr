function el(id) {
    return document.getElementById(id);
}

var homeSection = el('home');
var createSection = el('create');
var gameSection = el('game');

var createSectionUsername = el('createSectionUsername');
var homeSectionUsername = el('homeSectionUsername');

function showHomeSection() {
    homeSection.style.display = 'block';
    createSection.style.display = 'none';
    gameSection.style.display = 'none';
    homeSectionUsername.value = username;
}

function showCreateSection() {
    homeSection.style.display = 'none';
    createSection.style.display = 'block';
    gameSection.style.display = 'none';
    createSectionUsername.value = username;
}

function showGameSection() {
    homeSection.style.display = 'none';
    createSection.style.display = 'none';
    gameSection.style.display = 'block';
}