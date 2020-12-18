
//webkitURL is deprecated but nevertheless
URL = window.URL || window.webkitURL;

var gumStream; 						//stream from getUserMedia()
var rec; 							//Recorder.js object
var input; 							//MediaStreamAudioSourceNode we'll be recording

var AudioContext = window.AudioContext || window.webkitAudioContext;
var audioContext //audio context to help us record

function createDownloadLink(blob) {

	var url = URL.createObjectURL(blob);
	var au = document.createElement('audio');
	var li = document.createElement('li');
	var link = document.createElement('a');
	var filename = new Date().toISOString();
	au.controls = true;
	au.src = url;
	au.autoplay = true;

	link.href = url;
	link.download = filename+".wav"; //download forces the browser to donwload the file using the  filename
	link.innerHTML = "Save to disk";

	li.appendChild(au);

	li.appendChild(document.createTextNode(filename+".wav "))

	li.appendChild(link);

	var upload = document.createElement('a');
	upload.href="#";
	upload.innerHTML = "Upload";
	uploadData(blob);

	li.appendChild(document.createTextNode(" "))//add a space in between
	li.appendChild(upload)//add the upload link to li

	recordingsList.appendChild(li);
}

function speechToText(blob){
    showLoader();
    var fd = new FormData();
    fd.append("audio_data", blob, "somefile");

    $.ajax({
           type: "POST",
           enctype: 'multipart/form-data',
           url: "/speech-to-text",
           data: fd,
           contentType: 'application/json; charset=utf-8',
           processData: false,
           contentType: false,
           cache: false,
           timeout: 600000,
           success: function (text) {
               showUserChat(text);
               processChat(text);
               hideLoader();
           },
           error: function (e) {
               console.log("ERROR : ", e);
               hideLoader();
           }
     });
}

function processChat(intent){
    showLoader();
    $.ajax({
            type: "POST",
            url: "/chat",
            data: JSON.stringify({"intent":intent}),
            contentType: 'application/json; charset=utf-8',
            timeout: 600000,
            success: function (data) {
               textToSpeech(data);
               hideLoader();
            },
            error: function (e) {
              console.log("ERROR : ", e);
              hideLoader();
            }
     });
}


function textToSpeech(text){
    showLoader();
    $.ajax({
           type: "POST",
           url: "/text-to-speech",
           data: JSON.stringify({"text":text}),
           contentType: 'application/json; charset=utf-8',
           timeout: 600000,
           success: function (data) {
                showSystemChat(text);
                playAudio(data);
                hideLoader();
           },
           error: function (e) {
               console.log("ERROR : ", e);
               hideLoader();
           }
     });
}

function playAudio(data){
    var snd = new Audio("data:audio/wav;base64,"+data);
    snd.play();
}

function showSystemChat(text){
    text = text.replace('\.', '\. \n');
    var systemChat = '<div class="media media-chat"><img class="avatar" src="https://img.icons8.com/color/36/000000/administrator-male.png" alt="..."> <div class="media-body"> <p>'+text+'</p> </div> </div>';
    $("#content").append(systemChat);
    scrollDown();
}

function showUserChat(text){
    var userChat = '<div class="media media-chat media-chat-reverse"> <div class="media-body"><p>'+text+'</p></div></div>';
    $("#content").append(userChat);
    scrollDown();
}

function startRecording() {
    var constraints = { audio: true, video:false }


    navigator.mediaDevices.getUserMedia(constraints).then(function(stream) {
        audioContext = new AudioContext();
        gumStream = stream;
        input = audioContext.createMediaStreamSource(stream);
        rec = new Recorder(input,{numChannels:1})
        rec.record()
        console.log("Started listening");
        setTimeout(stopRecording, 6 * 1000)
    }).catch(function(err) {
//        welcome();
          console.log("Error ",err);
    });
}

function stopRecording() {
	console.log("Stopped listening");
	rec.stop();
	gumStream.getAudioTracks()[0].stop();
	rec.exportWAV(speechToText);
}

function showLoader(){
    $('#loader').show();
}

function hideLoader(){
    $('#loader').hide();
}

function scrollDown() {
    document.getElementById('chat-content').scrollTop =  document.getElementById('chat-content').scrollHeight;
}

function welcome(){
    $("#content").empty();
    var welcomeText = 'Hello User. I am your virtual Assistant. Ask me about your Investments, Statements or Loans';
    textToSpeech(welcomeText);
}

$(document).ready(function(){
    hideLoader();
    $("#welcome").click(welcome);
    $("#mic").click(startRecording);
});





