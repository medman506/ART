/*
 * node-pushserver
 * Copyright(c) 2013 Smile SA <https://github.com/Smile-SA/node-pushserver>
 * MIT Licensed
 */

var mongoose = require('mongoose');
var config = require('.././Config');
var _ = require('lodash');

// Init
var User;

var initialize = _.once(function () {
    var db = mongoose.connect(config.get('mongodbUrl'));
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

    //There can be only one username
    usersSchema.index({ user: 1}, { unique: true });

    //create model out of schema
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
	if(token==null){
		User.findOneAndUpdate({_id: id}, {$unset: {token: "" }}, function (err) {
              if (err) console.error(err);
           });
	}else{
	   User.findOneAndUpdate({_id: id}, {token: token}, function (err) {
            if (err) console.error(err);
        });
	}
};

//sets a token for user: gets called from client app
var setTeamForUser = function (id, team){
	User.findOneAndUpdate({_id: id}, {team: team}, function (err) {
           if (err) console.error(err);
        });
	
};

//return team für user id
var getTeamForUser = function (id,callback){
	console.log("ID:"+id);
	var wrappedCallback = outputFilterWrapper(callback,'team');
	User.find({_id: id}, 'team', function(err, obj) {                      
    		wrappedCallback(obj[0].team);
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

var getAllActiveTeams = function (callback) {
    console.log("getting all temas");
    var wrappedCallback = outputFilterWrapper(callback, 'team');

    User.distinct('team').and([{ team: { $ne: null } }, {token: { $ne: null}}]).exec(wrappedCallback);
      
};


var updateTokens = function (fromToArray) {
    
	fromToArray.forEach(function (tokenUpdate) {
        User.findOneAndUpdate({token: tokenUpdate.from}, {token: tokenUpdate.to}, function (err) {
            if (err) console.error(err);
        });
    });
};



var getPassForUser = function (username, callback) {
    var wrappedCallback = outputFilterWrapper(callback,'pass');

    User.find({user: username}, wrappedCallback);
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
		//Return team	
		var items = _.map(pushItems, function (pushItem) {
			console.log("IT: ", pushItem);
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

var authenticate = function (user, callback){
	console.log(user);
	getPassForUser(user.user, function (err, userIDs) {
        	if (!err) {
			if(userIDs.length ==0)
				callback(0);
			else if (user.pass == userIDs[0].pass)
				callback(userIDs[0].id)
			else
				callback(0);	   
        	} else {
            		callback(0);
        	}
   	});	
}

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
    getTeamForUser: getTeamForUser,
    getTokenForTeams: getTokenForTeams,
    authenticate: authenticate,
    getAll: getAll,
    getAllActiveTeams: getAllActiveTeams,
    updateTokens: updateTokens,
    removeDevices: removeDevices
});
