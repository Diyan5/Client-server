window.addEventListener('pageshow', function (e) {
    const navEntries = performance.getEntriesByType('navigation');
    const isBF = e.persisted || (navEntries[0] && navEntries[0].type === 'back_forward');
    if (isBF && !document.cookie.includes('SESSIONID=')) {
        location.replace('/login');
    }
});