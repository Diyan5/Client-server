(function () {
    function hasJustLoggedOut() {
        var fromCookie = document.cookie.split(';').some(function (c) { return c.trim().startsWith('JUST_LOGGED_OUT=1'); });
        var fromQuery  = new URLSearchParams(location.search).has('lo');
        return fromCookie || fromQuery;
    }
    function clearJustLoggedOut() {
        document.cookie = "JUST_LOGGED_OUT=; Max-Age=0; Path=/; SameSite=Lax";
    }
    function isFrontendLoggedIn() {
        return document.cookie.split(';').some(function (c) { return c.trim() === 'LOGGED_IN=1'; });
    }

    window.addEventListener('pageshow', function (e) {
        var nav = (performance.getEntriesByType && performance.getEntriesByType('navigation')[0]) || null;
        var backFwd = e.persisted || (nav && nav.type === 'back_forward');

        // ако се връщаме с Back и сме логнати → към /home
        if (backFwd && isFrontendLoggedIn()) {
            location.replace('/home');
            return;
        }

        // ако току-що сме излезли, остани на /login и изчисти флага
        if (hasJustLoggedOut()) {
            clearJustLoggedOut();
        }

        var form = document.getElementById('loginForm');
        if (form) { form.reset(); form.querySelectorAll('input').forEach(function (el) { el.value = ''; }); }
    });
})();
