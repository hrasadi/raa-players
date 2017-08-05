'use strict';

const Alexa = require('alexa-sdk');

const handlers = {
    'LaunchRequest': function () {
        this.response.audioPlayerPlay("REPLACE_ALL", "https://stream.raa.media/raa1.ogg", 112233, null, 0);
        this.emit(':responseReady');
    },
    'AMAZON.StopIntent': function () {
        this.response.audioPlayerClearQueue('CLEAR_ALL').audioPlayerStop().speak('Bessalaamat!');
        this.emit(':responseReady');
    },
    'AMAZON.CancelIntent': function () {
        this.emit('AMAZON.StopIntent');
    },
    'AMAZON.PauseIntent': function () {
        this.emit('AMAZON.StopIntent');
    },
    'SessionEndedRequest': function() {
        console.log("ended");
    },
    'Unhandled': function() {
        this.emit(':tell', 'Sorry, I didn\'t get that.');
    }
};

exports.handler = function (event, context) {
    const alexa = Alexa.handler(event, context);
    alexa.registerHandlers(handlers);
    alexa.execute();
};
