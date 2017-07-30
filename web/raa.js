var raa1Url = "http://raa.media:8000/raa1.ogg";
var raa1StatusUrl = 'http://www.raa.media:8000/status-json.xsl';

var PlaybackManager = function(onRadioProgramBeginCallback, onRadioProgramEndCallback) {

  this.radioHasProgram = false;
  this.cyclesInSameStatus = 0;
  this.currentProgram = '';
  this.isPausedByUser = false;

  this.loop = function(self, initialized) {
    // default value in older browsersxxxx
    initialized = typeof initialized  === 'undefined' ? true : initialized;
    
    self.readServerStatus(initialized);
  }

  this.readServerStatus = function(initialized) {
    self = this;
    $.get(raa1StatusUrl, function(data) {
      title = data.icestats.source.title;
      if (title && title != "" && title != "BLANK") {
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
      onRadioProgramBeginCallback();

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
  // don't waste resources! If player is already started continue
  if (!$('#player')[0].paused) {
    return;
  }

  // invalidate any previous players
  if (!playbackManager.isPausedByUser) {
    $('#player')[0].src = raa1Url;
    $('#player')[0].play(); // Note that this line does not have any effect in mobile browsers
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

},function () {
  $('#player-bar').html('<div class="text-center h4" style="padding-top: 10px">' +
  'الان برنامه نداریم!</div>');

  $('#player')[0].src = "";
});

playbackManager.init();
