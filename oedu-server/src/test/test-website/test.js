let socket = new WebSocket("ws://localhost:8080/websocket");
let message = null;
socket.onopen = function (e) {
    console.log("connected");
}

socket.onmessage = function (e) {
    message = JSON.parse(e.data)
    console.log(JSON.parse(e.data));

    document.getElementById("answer").value = JSON.stringify(message, null, 4);
}

socket.onclose = function (e) {
    console.log("closed");
}

socket.onerror = function(e) {
    console.log(e);
}

function sendRequest(req) {
    console.log(document.getElementById('request').value)
    try {
        req = JSON.parse(req);
        console.log(req)
    } catch (e) {
        console.error(e)
        document.getElementById("answer").value = "ERROR IN JSON-SYNTAX"
        return;
    }
    document.getElementById("request").value = JSON.stringify(req, null,4);
    socket.send(JSON.stringify(req));
    message = null;

}

function sendFile() {
    let file = document.getElementById('filename').files[0];
    console.log(file)
    let reader = new FileReader();
    let rawData = new ArrayBuffer();
    //alert(file.name);

    reader.loadend = function() {

    }
    reader.onload = function(e) {
        rawData = e.target.result;
        socket.send(rawData);
        alert("the File has been transferred.")
    }

    reader.readAsArrayBuffer(file);

}
