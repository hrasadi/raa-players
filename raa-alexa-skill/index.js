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
    'AMAZON.ResumeIntent': function () {
        this.emit('LaunchRequest');
    },
    'AMAZON.LoopOffIntent': function () {
        this.emit('Unsupported');
    },
    'AMAZON.LoopOnIntent': function () {
        this.emit('Unsupported');
    },
    'AMAZON.NextIntent': function () {
        this.emit('Unsupported');
    },
    'AMAZON.PreviousIntent': function () {
        this.emit('Unsupported');
    },
    'AMAZON.RepeatIntent': function () {
        this.emit('Unsupported');
    },
    'AMAZON.ShuffleOffIntent': function () {
        this.emit('Unsupported');
    },
    'AMAZON.ShuffleOnIntent': function () {
        this.emit('Unsupported');
    },
    'AMAZON.StartOverIntent': function () {
        this.emit('Unsupported');
    },
    'MusicControlIntent': function () {
        this.emit('Unsupported');
    },    
    'SessionEndedRequest': function() {
        console.log("ended");
    },
    'Unsupported': function() {
        this.emit(':tell', 'I cannot do that.');
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
