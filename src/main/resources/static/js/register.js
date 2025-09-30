function hasJustLoggedOut() {
    return document.cookie.split(';').some(c => c.trim().startsWith('JUST_LOGGED_OUT=1'))
        || new URLSearchParams(location.search).has('lo');
}

window.addEventListener('pageshow', function (e) {
    const nav = (performance.getEntriesByType && performance.getEntriesByType('navigation')[0]) || null;
    const backFwd = e.persisted || (nav && nav.type === 'back_forward');

    if (backFwd || hasJustLoggedOut()) {
        window.location.replace('/');
        return;
    }

    const form = document.getElementById('registerForm');
    if (form) { form.reset(); form.querySelectorAll('input').forEach(el => el.value=''); }
});