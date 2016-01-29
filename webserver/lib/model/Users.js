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

/*
Sets token for specific user id
if passed token is null, removes token field from user
*/
var setTokenForUser = function (id, token){
	if(token==null){
		User.findOneAndUpdate({_id: id}, {$unset: {token: "" }}, function (err) {
              if (err) errorHandler(err);
           });
	}else{
	   User.findOneAndUpdate({_id: id}, {token: token}, function (err) {
            if (err) errorHandler(err);
        });
	}
};

/*
Set the team for the user which matches the passed id
*/
var setTeamForUser = function (id, team){
	User.findOneAndUpdate({_id: id}, {team: team}, function (err) {
           if (err) errorHandler(err);
        });
	
};

/*
Find the team for the user which has the id which is passed as parameter
See wrapped callback for what will be returned
*/
var getTeamForUser = function (id,callback){
	var wrappedCallback = outputFilterWrapper(callback,'team');
	User.find({_id: id}, 'team', function(err, obj) {
		if(obj[0].team !== undefined)                      
    			wrappedCallback(obj[0].team);
		else
			wrappedCallback([]);
	});
	
};
/*
Get tokens for users which have a team which matches one of the passed teams
See wrapped callback for what will be returned
*/
var getTokenForTeams = function (teams, callback) {
    var wrappedCallback = outputFilterWrapper(callback,'token');

    User.where('team')
        .in(teams)
        .exec(wrappedCallback);
};

/*
Get all users from db
See wrapped callback for what will be returned
*/
var getAll = function (callback) {
    var wrappedCallback = outputFilterWrapper(callback, 'all');
    User.find(wrappedCallback);
};

/*
Find teams of all users which have a token set
Teams are distinct(no multiple values of same team)
See wrapped callback for what will be returned
*/
var getAllActiveTeams = function (callback) {
    var wrappedCallback = outputFilterWrapper(callback, 'team');
    User.distinct('team').and([{ team: { $ne: null } }, {token: { $ne: null}}]).exec(wrappedCallback);
};

/*
Updates all tokens which mach fromToArray.From to new valuesfrom fromToArray.to
*/
var updateTokens = function (fromToArray) {
	fromToArray.forEach(function (tokenUpdate) {
        User.findOneAndUpdate({token: tokenUpdate.from}, {token: tokenUpdate.to}, function (err) {
            if (err) errorHandler(err);
        });
    });
};


/*
Finds user credentials for passed username
See wrapped callback for what will be returned
*/
var getPassForUser = function (username, callback) {
    var wrappedCallback = outputFilterWrapper(callback,'pass');

    User.find({user: username}, wrappedCallback);
};

/*
Find tokens of users which have the passed id
See wrapped callback for what will be returned
*/
var getTokenForUser = function (id, callback) {
    var wrappedCallback = outputFilterWrapper(callback,'token');

    User.find({_id: id}, wrappedCallback);
};



/*
Removes token from users where found token equals passed token
*/
var removeDevice = function (token) {
    Users.update({token: token},{$unset: {token: "" }}, function (err) {
        if (err) 
	   errorHandler(err);
    });
};

/*
Removes token from users where found token is in token array passed from caller
*/
var removeDevices = function (tokens) {
	  
	Users.update({token: {$in: tokens}},{$unset: {token: "" }}, function (err) {
        if (err) errorHandler(err);
    });
};

/*
Format the output before submitting it to callback
Depending on passed argument, different fields will be returned
*/
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

/*
Compares given credentials to DB
Returns id of successful(valid credentials) 
or 0 if no user was found or pw is wrong
*/
var authenticate = function (user, callback){
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

/*
Helper function to display errors
*/
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
