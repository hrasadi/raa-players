var radioStream = null;
var radioPowerSwitch = false;
var currentProgram = '';
var raa1Url = "http://raa.media:8000/raa1.ogg";

var raa1StatusUrl = 'http://www.raa.media:8000/status-json.xsl';

var playbackManager = null;

var PlaybackManager = function(onRadioProgramBeginCallback, onRadioProgramEndCallback) {

  this.radioHasProgram = false;
  this.title = null;
  this.cyclesInSameStatus = 0;
  this.isPausedByUser = false;

  this.loop = function(self, initialized) {
    // default value in older browsers
    initialized = typeof initialized  === 'undefined' ? true : initialized;
    
    self.readServerStatus(initialized);
  }

  this.readServerStatus = function(initialized) {
    self = this;
    $.get(raa1StatusUrl, function(data) {
      self.title = data.icestats.source.title;
      if (self.title && self.title != "" && self.title != "BLANK") {
        /* We have program. Determine if the status has changed */
        // If radio didn't have program before
        if (!self.radioHasProgram) {
          self.radioHasProgram = true;
          self.cyclesInSameStatus = 1;
        } else {
          self.cyclesInSameStatus++;
        }
      } else {
        // We do not have program. Determine if the status has changed
        if (self.radioHasProgram) {
          self.radioHasProgram = false;
          self.cyclesInSameStatus = 1;
        } else {
          self.cyclesInSameStatus++;
        }
      }
      self.decideRadioStatus(initialized);
    });
  }

  this.decideRadioStatus = function(initialized) {
    if (this.radioHasProgram) {
      onRadioProgramBeginCallback(this.title);

      // Do not shutdown the stream immediately when the radio program ends,
      // give some time for playback the rest
      // If this is the first time we are here (uninitialized status) we
      // should go ahead and set the status no matter what
    } else if ((!this.radioHasProgram && this.cyclesInSameStatus > 2) || !initialized) {
      onRadioProgramEndCallback();
    }
  }
}

PlaybackManager.prototype.init = function() {
  // start
  self = this;
  this.loop(this, false);
  setInterval(function() {
    self.loop(self);
  }, 10000);
}


var flipRadioPowerStatus = function() {
  radioPowerSwitch = !radioPowerSwitch;

  if (playbackManager) {
    playbackManager.decideRadioStatus();
  }
}

chrome.runtime.onMessage.addListener(
    function(request, sender, sendResponse) {
        if (!sender.tab) {
            if (request.hasOwnProperty("setRadioPowerSwitch")) {
                if (radioPowerSwitch != request.setRadioPowerSwitch) {
                  flipRadioPowerStatus();
                }
            } else if (request.hasOwnProperty("getRadioPowerSwitch")) {
                sendResponse({radioPowerSwitch: radioPowerSwitch});
            
            } else if (request.hasOwnProperty("getCurrentProgram")) {
                sendResponse({currentProgram: currentProgram});
            }
        } 
    }
);

document.addEventListener('DOMContentLoaded', function() {
    playbackManager = new PlaybackManager(function(title) {
      currentProgram = 'در حال پخش: ' + title;

      if (!radioPowerSwitch) {
        if (radioStream) {
          radioStream.src = "";
        }
      } else {
        // don't waste resources! If player is already started continue
        if (radioStream && !radioStream.paused) {
            return;
        }
        // invalidate any previous players
        if (radioStream) {
            radioStream.src = "";
        }
        radioStream = new Audio(raa1Url);
        radioStream.play();          
      }        
    },function () {
        currentProgram = 'الان برنامه نداریم!';

        if (radioStream) {
            radioStream.src = "";
            radioStream = null;
        }
    });

    playbackManager.init();
});
