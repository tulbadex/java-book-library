// Remove flash messages after 10 seconds
setTimeout(function() {
    var flashMessages = document.getElementById('flash-messages');
    if (flashMessages) {
        flashMessages.style.display = 'none';
    }
}, 10000);
