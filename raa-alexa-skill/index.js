'use strict';

const Alexa = require('alexa-sdk');

const handlers = {
    'LaunchRequest': function () {
        this.response.audioPlayerPlay("REPLACE_ALL", "https://api.raa.media/linkgenerator/live.mp3?src=aHR0cHM6Ly9zdHJlYW0ucmFhLm1lZGlhL3JhYTEub2dn", 112233, null, 0);
        console.log(JSON.stringify(this.response));
        this.emit(':responseReady');
    },
    'AMAZON.StopIntent': function () {
//        this.response.audioPlayerStop().audioPlayerClearQueue('CLEAR_ALL').speak('Bessalaamat!');
        this.response.audioPlayerStop().speak('Bessalaamat!');
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
