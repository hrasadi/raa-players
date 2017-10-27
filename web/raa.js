var raa1Url = "https://api.raa.media/linkgenerator/live.mp3?src=aHR0cHM6Ly9zdHJlYW0ucmFhLm1lZGlhL3JhYTEub2dn";
var raa1StatusUrl = 'https://raa.media/lineups/status.json';

var PlaybackManager = function(onRadioProgramBeginCallback, onRadioProgramEndCallback) {

  this.radioHasProgram = false;
  this.cyclesInSameStatus = 0;
  this.isPausedByUser = false;

  this.loop = function(self, initialized) {
    // default value in older browsersxxxx
    initialized = typeof initialized  === 'undefined' ? true : initialized;
    
    self.readServerStatus(initialized);
  }

  this.readServerStatus = function(initialized) {
    self = this;
    $.get(raa1StatusUrl, function(data) {
     title = data.currentProgram;	
     if (data.isCurrentlyPlaying) {
      /* We have program. Determine if the status has changed */
        // If radio didn't have program before
        if (!self.radioHasProgram) {
          self.radioHasProgram = true;
          self.cyclesInSameStatus = 1;
        } else {
          self.cyclesInSameStatus++;
        }
      } else {
        nextBoxId = data.nextBoxId;
        nextBoxStartTime = moment(data.nextBoxStartTime);
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
      onRadioProgramBeginCallback();

      // Do not shutdown the stream immediately when the radio program ends,
      // give some time for playback the rest
      // If this is the first time we are here (uninitialized status) we
      // should go ahead and set the status no matter what
    } else if ((!this.radioHasProgram && this.cyclesInSameStatus > 1) || !initialized) {
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

var generatePlayerButton = function() {
  if ($('#player')[0].paused) {
    return '<img src="img/play-button.png" style="max-height: 50px">';
  } else {
    return '<img src="img/pause-button.png" style="max-height: 50px">';
  }
}

var flipPlaybackStatus = function() {
  if ($('#player')[0].paused) {
    $('#player')[0].play();
    playbackManager.isPausedByUser = false;
  } else {
    $('#player')[0].pause();
    playbackManager.isPausedByUser = true;
  }
  $("#player-button").html(generatePlayerButton());
}

var playbackManager = new PlaybackManager(function() {
  // invalidate any previous players
  if ($('#player')[0].paused && !playbackManager.isPausedByUser) {
    $('#player')[0].src = raa1Url;
    $('#player')[0].play(); // Note that this line does not have any effect in mobile browsers
  }

  // invalidate the counter
  downCounterMoment = null;
  if (typeof downCounterIntervalId != 'undefined' && downCounterIntervalId > 0) {
    clearInterval(downCounterIntervalId);
    downCounterIntervalId = -1;
 }
 
 $('#player-bar').html(
  '<div class="col-xs-11 h5">' +
  '<div class="row" style="padding-right:20px">' +
  '<div id="equalizer" class="col-xs-4"/>' +
  '<div class="col-xs-8" style="padding-top: 10px"> در حال پخش: ' + title + '</div>' +
  '</div>' +
  '</div>' +
  '<div id="player-button" class="col-xs-1" dir="ltr" style="padding:0px">' +
  '   <script type="text/javascript">' +
  '     $("#player-button").html(generatePlayerButton());' +
  '     $("#player-button").on("click", function() {' +
  '       flipPlaybackStatus();' +
  '     });' +
  '   </script>' +
  '</div>');
 makeSpectrum("equalizer", 20, 20, 3, 0);

}, function () {
  
  if (typeof downCounterMoment === 'undefined' || downCounterMoment == null) {
   
   downCounterMoment = moment.duration(nextBoxStartTime.diff(moment()));

   downCounterIntervalId = setInterval(function() {
     downCounterMoment.subtract(1, 'second');

     if (downCounterMoment.asMilliseconds() < 0) {
       if (typeof nextBoxId !== 'undefined' && nextBoxId != null) {
          $('#player-bar').html('<div class="text-center h4" style="padding-top: 10px">' +
            'به زودی: ' + nextBoxId + '</div>');        
       } else { // no more programs tonight!
          $('#player-bar').html('<div class="text-center h4" style="padding-top: 10px">' +
            'شب بخیر! ادامه‌ی برنامه‌های رادیو از نیمه شب...');
       }
     } else {
       var playerBarHtml = '<div class="text-center h4" style="padding-top: 10px">' + nextBoxId + ' در ';

       if (downCounterMoment.hours() != 0) {
        playerBarHtml = playerBarHtml + downCounterMoment.hours() + ' ساعت و ';
      }
      if (downCounterMoment.minutes() != 0) {
        playerBarHtml = playerBarHtml + downCounterMoment.minutes() + ' دقیقه و ';
      }

      playerBarHtml = playerBarHtml + downCounterMoment.seconds() + ' ثانیه ' + '</div>';

      $('#player-bar').html(playerBarHtml);
    }
  }, 1000);
 } 
});

playbackManager.init();
