function el(id) {
    return document.getElementById(id);
}

var homeSection = el('home');
var createSection = el('create');

var createSectionUsername = el('createSectionUsername');
var homeSectionUsername = el('homeSectionUsername');

function showHomeSection() {
    homeSection.style.display = 'block';
    createSection.style.display = 'none';
    homeSectionUsername.value = username;
}

function showCreateSection() {
    homeSection.style.display = 'none';
    createSection.style.display = 'block';
    createSectionUsername.value = username;
}