(function () {
    // При връщане от историята (bfcache) правим проверка за сесия и ако я няма → към /login
    window.addEventListener('pageshow', function (e) {
        var nav = (performance.getEntriesByType && performance.getEntriesByType('navigation')[0]) || null;
        var fromBF = e.persisted || (nav && nav.type === 'back_forward');
        var hasSession = document.cookie.split(';').some(function (c) { return c.trim().startsWith('SESSIONID='); });
        if (fromBF && !hasSession) {
            location.replace('/login');
        }
    });
})();
