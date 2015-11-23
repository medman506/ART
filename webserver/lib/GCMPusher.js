/**
 * Created with JetBrains WebStorm.
 * User: smile
 * Date: 13/06/13
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */

var config = require('./Config')
var _ = require('lodash');
var gcm = require('node-gcm');
var pushAssociations = require('./PushAssociations');
var apikey = "AIzaSyCNTPu0BUd-jLiBuNtT8VvWkebycX5yUM0"


var push = function (tokens, message) {
    var sender = new gcm.Sender(apikey);
    console.log("tokens: "+tokens +"\n");
        
    console.log("regIDS: "+regIds +"\n");
    sender.send(message, { registrationTokens: tokens }, 4, function (err, res) {
        if(err) console.log(err);
        else console.log(res);

       /* if (res) {
            var mappedResults = _.map(_.zip(tokens, res.results), function (arr) {
                return _.merge({token: arr[0]}, arr[1]);
            });

            handleResults(mappedResults);
        } */
    })
};

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

    if (idsToUpdate.length > 0) pushAssociations.updateTokens(idsToUpdate);
    if (idsToDelete.length > 0) pushAssociations.removeDevices(idsToDelete);
};

var buildPayload = function (options) {
    var message = new gcm.Message();
    message.addData(options);
    
    //maybe missing notification?
    message.addNotification({
	title: 'Alert!!!',
  	body: 'TEST TEST TEST',
  	icon: 'ic_launcher'
    });
    console.log('GCMPusher.js-line 47: ' + JSON.stringify(message));
    return message;
};

module.exports = {
    push: push,
    buildPayload:buildPayload
}
