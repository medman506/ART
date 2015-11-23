var mongoose = require('mongoose');
var config = require('./Config');
var _ = require('lodash');

// Init
var PushAssociation;

var initialize = _.once(function () {
    var db = mongoose.connect(config.get('mongodbUrl'));
	console.log('PushAssoc-Line10: ' + config.get('mongodbUrl'));
    mongoose.connection.on('error', errorHandler);

    var pushAssociationSchema = new db.Schema({
        user: {
            type: 'String',
            required: true
        },
        token: {
            type: 'String',
            required: true
        },
	longitude: {
	    type: 'Number',
	    required: false
	},
	latitude: {
	    type: 'Number',
	    required: false
	}
    });

    // I must ensure uniqueness of user
    pushAssociationSchema.index({ user: 1}, { unique: true });

    PushAssociation = db.model('PushAssociation', pushAssociationSchema);

    return module.exports;
});

var add = function (user, token) {
    var pushItem = new PushAssociation({user: user, token: token, longitude: 0, latitude: 0});
    pushItem.save();
};

var updateLoc = function (update) {
	PushAssociation.findOneAndUpdate({user: update.user}, 
	{longitude: update.longitude, latitude: update.latitude}, 
	function (err) {
            if (err) console.error(err);
        });
};

var updateTokens = function (fromToArray) {
    fromToArray.forEach(function (tokenUpdate) {
        PushAssociation.findOneAndUpdate({token: tokenUpdate.from}, {token: tokenUpdate.to}, function (err) {
            if (err) console.error(err);
        });
    });
};

var getAll = function (callback) {
    var wrappedCallback = outputFilterWrapper(callback);

    PushAssociation.find(wrappedCallback);
};

var getForUser = function (user, callback) {
    var wrappedCallback = outputFilterWrapper(callback);

    PushAssociation.find({user: user}, wrappedCallback);
};

var getForUsers = function (users, callback) {
    var wrappedCallback = outputFilterWrapper(callback);

    PushAssociation.where('user')
        .in(users)
        .exec(wrappedCallback);
};

var removeForUser = function (user) {
    PushAssociation.remove({user: user}, function (err) {
        if (err) console.dir(err);
    });
};

var removeDevice = function (token) {
    PushAssociation.remove({token: token}, function (err) {
        if (err) console.log(err);
    });
};

var removeDevices = function (tokens) {
    PushAssociation.remove({token: {$in: tokens}}, function (err) {
        if (err) console.log(err);
    });
};

var outputFilterWrapper = function (callback) {
    return function (err, pushItems) {
        if (err) return callback(err, null);

        var items = _.map(pushItems, function (pushItem) {
            return _.pick(pushItem, ['user', 'longitude','latitude', 'token'])
        });
	
        return callback(null, items);
    }
};

var initWrapper = function (object) {
    return _.transform(object, function (newObject, func, funcName) {
        if(!_.isFunction(func)) return newObject[funcName] = func;

        newObject[funcName] = function () {
            if (_.isUndefined(PushAssociation)) {
                initialize();
            }

            return func.apply(null, arguments);
        };
    });
};

var errorHandler = function(error) {
    console.error('ERROR: ' + error);
};

module.exports = initWrapper({
    add: add,
    updateTokens: updateTokens,
    updateLoc: updateLoc,
    getAll: getAll,
    getForUser: getForUser,
    getForUsers: getForUsers,
    removeForUser: removeForUser,
    removeDevice: removeDevice,
    removeDevices: removeDevices
});
