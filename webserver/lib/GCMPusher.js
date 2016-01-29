/*
 * node-pushserver
 * Copyright(c) 2013 Smile SA <https://github.com/Smile-SA/node-pushserver>
 * MIT Licensed
 */

var config = require('./Config');
var _ = require('lodash');
var gcm = require('node-gcm');
var users = require('./model/Users');


/*
Sends the message to the GCM servers
*/
var push = function (tokens, message) {
    var sender = new gcm.Sender(config.get('gcm').apiKey);       
    sender.send(message, { registrationTokens: tokens }, 4, function (err, res) {
        if(err) console.log(err);
        else console.log(res);

        if (res) {
            var mappedResults = _.map(_.zip(tokens, res.results), function (arr) {
                return _.merge({token: arr[0]}, arr[1]);
            });

            handleResults(mappedResults);
        }
    })
};

/*
Handles the response from gcm
checks if tokens are invalid or need to be updated
*/
var handleResults = function (results) {
    var idsToUpdate = [],
        idsToDelete = [];

    results.forEach(function (result) {
        if (!!result.registration_id) {
            idsToUpdate.push({from: result.token, to: result.registration_id});

        } else if (result.error === 'InvalidRegistration' || result.error === 'NotRegistered') {
            idsToDelete.push(result.token);
        }
    });

    if (idsToUpdate.length > 0) users.updateTokens(idsToUpdate);
    if (idsToDelete.length > 0) users.removeDevices(idsToDelete);
};


/*
Adds the payload to the gcm message
*/
var buildPayload = function (options) {
    var message = new gcm.Message();
    message.addData(options);
    return message;
};

module.exports = {
    push: push,
    buildPayload:buildPayload
}
