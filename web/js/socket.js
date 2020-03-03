/**
 * Created by iawom on 2017/8/31.
 */
var socket;
if (!window.WebSocket) {
    window.WebSocket = window.MozWebSocket;
}
if (window.WebSocket) {
    var uri = "ws://" + window.location.host + "/ws";
    socket = new WebSocket(uri);
} else {
    alert("Your browser does not support Web Socket.");
}

function send(message) {
    if (!window.WebSocket) {
        return;
    }
    if (socket.readyState == WebSocket.OPEN) {
        socket.send(message);
    } else {
        alert("The socket is not open . " + socket.readyState);
    }
}