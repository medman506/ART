/**
 * Created with JetBrains WebStorm.
 * User: Vincent Lemeunier
 * Date: 06/06/13
 * Time: 15:41
 */

var _ = require('lodash'),
    pushAssociations = require('./PushAssociations'),
    gcmPusher = require('./GCMPusher');


var send = function (pushAssociations, androidPayload) {
    var androidTokens = _(pushAssociations).map('token').value();
    if (androidPayload && androidTokens.length > 0) {
        var gcmPayload = gcmPusher.buildPayload(androidPayload);
        console.log("PushController.js-line 17 - gcmPayload: " + JSON.stringify(gcmPayload));
        gcmPusher.push(androidTokens, gcmPayload);
    }

};

var sendUsers = function (users, payload) {
    pushAssociations.getForUsers(users, function (err, pushAss) {
        if (err) return;
        send(pushAss, payload);
    });
};

var subscribe = function (deviceInfo) {
	console.log("\nSUBSCRIPTION: "+decodeURIComponent(deviceInfo.token)); 
    pushAssociations.add(deviceInfo.user, decodeURIComponent(deviceInfo.token));
};

var unsubscribeDevice = function (deviceToken) {
    pushAssociations.removeDevice(deviceToken);
};

var unsubscribeUser = function (user) {
    pushAssociations.removeForUser(user);
};

module.exports = {
    send: send,
    sendUsers: sendUsers,
    subscribe: subscribe,
    unsubscribeDevice: unsubscribeDevice,
    unsubscribeUser: unsubscribeUser
};
