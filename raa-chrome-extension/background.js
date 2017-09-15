var radioStream = null;
var radioPowerSwitch = false;
var raa1Url = "http://raa.media:8000/raa1.ogg";

var raa1StatusUrl = 'http://raa.media/lineups/status.json';

var currentNotificationId = null;

var playbackManager = null;

var PlaybackManager = function(onRadioProgramBeginCallback, onRadioProgramEndCallback) {

  this.radioHasProgram = false;
  this.title = null;
  this.currentBox = null;
  this.currentProgram = null;
  this.currentClip = null;
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
      self.currentProgram = data.currentProgram; 
      if (data.isCurrentlyPlaying) {
        self.currentBox = data.currentBox;
        self.currentClip = data.currentClip;
        /* We have program. Determine if the status has changed */
        // If radio didn't have program before
        if (!self.radioHasProgram) {
          self.radioHasProgram = true;
          self.cyclesInSameStatus = 1;
        } else {
          self.cyclesInSameStatus++;
        }
      } else {
        //self.nextBoxId = data.nextBoxId;
        //nextBoxStartTime = moment(data.nextBoxStartTime);
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
      onRadioProgramBeginCallback(this);

      // Do not shutdown the stream immediately when the radio program ends,
      // give some time for playback the rest
      // If this is the first time we are here (uninitialized status) we
      // should go ahead and set the status no matter what
    } else if ((!this.radioHasProgram && this.cyclesInSameStatus > 2) || !initialized) {
      onRadioProgramEndCallback(this);
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

// listeners for popup page 
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
                sendResponse({currentProgram: playbackManager.title});
            }
        } 
    }
);

// register for the notification buttons
chrome.notifications.onButtonClicked.addListener(function(notificationId, buttonIdx) {
  if (buttonIdx == 0) { // turn on radio if it is off
    if (!radioPowerSwitch) {
      flipRadioPowerStatus();
    }
  }
}); 

// register for the notification buttons
chrome.notifications.onClicked.addListener(function(notificationId) {
  if (!radioPowerSwitch) {
    flipRadioPowerStatus();
  }
});

document.addEventListener('DOMContentLoaded', function() {
    playbackManager = new PlaybackManager(function(playbackManager) {
      playbackManager.title = 'در حال پخش: ' + playbackManager.currentProgram;

      if (!radioPowerSwitch) {
        if (radioStream) {
          radioStream.src = "";
        }

        options = {
          type: 'basic',
          message: 'اکنون در رادیو اتو-اسعد',
          iconUrl: 'icon_128.png',
          buttons: [ { title: 'گوش می‌دهم' } ]
        }
        options["title"] = playbackManager.title;

        now = new Date();
        today = now.getFullYear() + "-" +  (now.getMonth() + 1) + "-" + now.getDate();
        newNotificationId = today + "-" + playbackManager.currentBox + "-" + playbackManager.currentProgram;

        // Make sure we do not show any notification more than once
        if (newNotificationId != currentNotificationId) {
          currentNotificationId = newNotificationId;
          chrome.notifications.create(currentNotificationId, options);
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
    }, function (playbackManager) {
        playbackManager.title = 'الان برنامه نداریم!';

        if (radioStream) {
            radioStream.src = "";
            radioStream = null;
        }
    });

    playbackManager.init();
});
