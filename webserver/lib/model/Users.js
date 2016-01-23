var mongoose = require('mongoose');
var config = require('.././Config');
var _ = require('lodash');

// Init
var User;

var initialize = _.once(function () {
    var db = mongoose.connect(config.get('mongodbUrl'));
	console.log('PushAssoc-Line10: ' + config.get('mongodbUrl'));
    mongoose.connection.on('error', errorHandler);

    var usersSchema = new db.Schema({
	_id: 'Number',        
	user: {
            type: 'String',
            required: true
        },
        pass: {
            type: 'String',
            required: true
        },
	team: 'String',
	token: {
            type: 'String',
            required: false
        }
    });

    // I must ensure uniqueness of user
    usersSchema.index({ user: 1}, { unique: true });

    User = db.model('User', usersSchema);

    return module.exports;
});

//Add a new user to schema
var add = function (id, user, pass) {
    var userItem = new User({_id: id, user: user, pass: pass});
    userItem.save();
};

//sets a token for user: gets called from client app
var setTokenForUser = function (id, token){
	User.findOneAndUpdate({_id: id}, {token: token}, function (err) {
            if (err) console.error(err);
        });
};

//sets a token for user: gets called from client app
var setTeamForUser = function (id, team){
	User.findOneAndUpdate({_id: id}, {team: team}, function (err) {
            if (err) console.error(err);
        });
};

//get tokens for all queried teams
var getTokenForTeams = function (teams, callback) {
    var wrappedCallback = outputFilterWrapper(callback,'token');

    User.where('team')
        .in(teams)
        .exec(wrappedCallback);
};

var getAll = function (callback) {
    console.log("getting all");
    var wrappedCallback = outputFilterWrapper(callback, 'all');

    User.find(wrappedCallback);
};
var getAllTeams = function (callback) {
    console.log("getting all temas");
    var wrappedCallback = outputFilterWrapper(callback, 'team');

    User.distinct('team', wrappedCallback);

    /*User.distinct('team')
	.where('team')
	.ne(null)
	.exec(wrappedCallback);*/
};

/*
var updateTokens = function (fromToArray) {
    fromToArray.forEach(function (tokenUpdate) {
        PushAssociation.findOneAndUpdate({token: tokenUpdate.from}, {token: tokenUpdate.to}, function (err) {
            if (err) console.error(err);
        });
    });
};*/

//find tokens for a team
/*
var getTokensForTeam = function (team, callback){
	var wrappedCallback = outputFilterWrapper(callback,'token');	
	User.find({'team':team},wrappedCallback);
}*/


var getPassForUser = function (id, callback) {
    var wrappedCallback = outputFilterWrapper(callback,'pass');

    User.find({_id: id}, wrappedCallback);
};

var getTokenForUser = function (id, callback) {
    var wrappedCallback = outputFilterWrapper(callback,'token');

    User.find({_id: id}, wrappedCallback);
};



//Set token null on unsubscribe
var removeDevice = function (token) {
    Users.update({token: token},{token: null}, function (err) {
        if (err) console.log(err);
    });
};

var removeDevices = function (tokens) {
	    
	Users.update({token: {$in: tokens}},{token: null}, function (err) {
        if (err) console.log(err);
    });
};

var outputFilterWrapper = function (callback, detail) {
    return function (err, pushItems) {
        if (err) return callback(err, null);
	if(detail === 'pass'){
		//Return user and pass
		var items = _.map(pushItems, function (pushItem) {
            		return _.pick(pushItem, ['id', 'pass'])
        	});
	}else if(detail === 'token'){
		//Return user and token		
		var items = _.map(pushItems, function (pushItem) {
            		return _.pick(pushItem, ['id', 'token'])
        	});
	}else if(detail === 'team'){
		//Return user and token		
		var items = _.map(pushItems, function (pushItem) {
            		return pushItem;
        	});
	}else{
		//Return user and token		
		var items = _.map(pushItems, function (pushItem) {
            		return _.pick(pushItem, ['id', 'user', 'pass', 'token','team'])
        	});
	}

        return callback(null, items);
    }
};

var initWrapper = function (object) {
    return _.transform(object, function (newObject, func, funcName) {
        if(!_.isFunction(func)) return newObject[funcName] = func;

        newObject[funcName] = function () {
            if (_.isUndefined(User)) {
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
    setTokenForUser: setTokenForUser,
    setTeamForUser: setTeamForUser,
    getTokenForTeams: getTokenForTeams,
    getPassForUser: getPassForUser,
    getAll: getAll,
    getAllTeams: getAllTeams
});
