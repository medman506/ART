/* mit licenses*/

var _ = require('lodash'),
    users = require('./model/Users'),
    gcmPusher = require('./GCMPusher');


var send = function (users, androidPayload) {
    var androidTokens = _(users).map('token').value();
    if (androidPayload && androidTokens.length > 0) {
        var gcmPayload = gcmPusher.buildPayload(androidPayload);
        console.log("PushController.js-line 17 - gcmPayload: " + JSON.stringify(gcmPayload));
        gcmPusher.push(androidTokens, gcmPayload);
    }

};

var sendTeams = function (teams, payload) {
    users.getTokenForTeams(teams, function (err, foundUsers) {
        if (err) return;
        send(foundUsers, payload);
    });
};

var subscribe = function (deviceInfo) {
	console.log("\nSUBSCRIPTION: "+decodeURIComponent(deviceInfo.token));
	users.setTokenForUser(deviceInfo.user, decodeURIComponent(deviceInfo.token)) 
};

var unsubscribeDevice = function (deviceToken) {
    users.removeDevice(deviceToken);
};

var unsubscribeUser = function (userID) {
    users.setTokenForUser(userID,null);
};

module.exports = {
    send: send,
    sendTeams: sendTeams,
    subscribe: subscribe,
    unsubscribeDevice: unsubscribeDevice,
    unsubscribeUser: unsubscribeUser
};
