var radioStream = null;
var radioPowerSwitch = false;

var radioHasProgram = false;
var radioProgramCyclesInSameStatus = 0;
var currentProgram = '';

const raa1Url = "http://raa.media:8000/raa1.ogg";
const raa1StatusUrl = 'http://www.raa.media:8000/status-json.xsl';

var decidePlayback = function() {
    if (radioHasProgram && radioPowerSwitch) {
        // invalidate any previous players
        if (radioStream) {
            radioStream.src = "";
        }
        radioStream = new Audio("http://raa.media:8000/raa1.ogg");
        radioStream.play();

    // Do not shutdown the stream immediately when the radio program ends, give some time for playback the rest 
    // In case the radio is turned off manually, we should go ahead and cut the stream
    } else if ((radioPowerSwitch && radioProgramCyclesInSameStatus > 2) ||   
                 !radioPowerSwitch) {
        if (radioStream) {
            radioStream.src = "";
            radioStream = null;
        }
    }
}

chrome.runtime.onMessage.addListener(
    function(request, sender, sendResponse) {
        if (!sender.tab) {
            if (request.hasOwnProperty("setRadioPowerSwitch")) {
                radioPowerSwitch = request.setRadioPowerSwitch;
                decidePlayback();
            
            } else if (request.hasOwnProperty("getRadioPowerSwitch")) {
                sendResponse({radioPowerSwitch: radioPowerSwitch});
            
            } else if (request.hasOwnProperty("getCurrentProgram")) {
                sendResponse({currentProgram: currentProgram});
            }
        } 
    }
);

var readRadioTitle = function(){
    var previousHasProgramStatus = radioHasProgram;
    jQuery.get(raa1StatusUrl, function(data){
        var title = data.icestats.source.title;
        if (title && title != "" && title != "BLANK") {                 
            radioHasProgram = true;
            currentProgram = 'در حال پخش: ' + title;
        } else {
            radioHasProgram = false;
            currentProgram = 'الان برنامه نداریم!';
        }

        if (previousHasProgramStatus != radioHasProgram) {
            radioProgramCyclesInSameStatus = 0;
            decidePlayback();      
        } else {
            radioProgramCyclesInSameStatus++;
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {
    readRadioTitle();
    setInterval(readRadioTitle, 10000);
});
