var radioStream = null;

var decidePlayback = function(radioHasProgram, radioPowerSwitch) {
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
