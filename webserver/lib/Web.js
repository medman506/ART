var config = require('./Config');
var express = require('express');
var basicAuth = require('basic-auth');
var _ = require('lodash');
var users = require('./model/Users');
var push = require('./PushController');

var app = express();

var myUser;

// Middleware
app.use(express.compress());
app.use(express.bodyParser());


var auth = function (req, res, next) {
  function unauthorized(res) {
    res.set('WWW-Authenticate', 'Basic realm=Authorization Required');
    return res.send(401);
  };

  var user = basicAuth(req);

  if (!user || !user.name || !user.pass) {
    return unauthorized(res);
  };

  if (user.name === myUser.user && user.pass === myUser.password) {
    return next();
  } else {
    return unauthorized(res);
  };
};


app.use(function(err, req, res, next) {
    res.status(500);
    res.render('error', { error: err });
});

app.post('/*',function (req, res, next) {
    if (req.is('application/json')) {
        next();
    } else {
        res.status(406).send();
    }
});

// Main API
app.post('/login', function (req, res) {
    var userInfo = req.body;
     users.authenticate(userInfo, function (retID) {
       
       console.log("LoginID: "+retID.toString());
       res.set('Content-Type', 'text/plain');
       res.set('Content-Length', retID.toString.length);
       res.send(retID.toString());
    });
    
   
});

app.post('/subscribe', function (req, res) {
    var deviceInfo = req.body;
    push.subscribe(deviceInfo);
    
    res.send(200);
});

app.post('/unsubscribe', function (req, res) {
    var data = req.body;

    if (data.user) {
        push.unsubscribeUser(data.user);
    } else if (data.token) {
        push.unsubscribeDevice(data.token);
    } else {
        return res.send(503);
    }

    res.send(200);
});


app.post('/send', function (req, res) {
    console.log("Received Notification");
    var notifs = [req.body];
    console.log(notifs);

   var notificationsValid = sendNotifications(notifs);
   res.status(notificationsValid ? 200 : 400).send();
});

app.post('/users/add', function (req, res) {
    var user = req.body;
    console.log(user);
    users.add(user.id, user.user, user.pass);
    //push.subscribe(deviceInfo);
    
    res.send(200);
});

app.put('/users/:user/team', function (req, res) {
    var id=req.params.user;
    var team= req.body.team;
    console.log(id);
    users.setTeamForUser(id,team);
    res.send(200);
});


app.get('/users/:userID/team', function (req, res) {
    	var id=req.params.userID;
	users.getTeamForUser(id, function (team){
		res.set('Content-Type', 'text/plain');
       		res.set('Content-Length', team.length);
       		res.status(200).send(team);
	});   
});


app.get('/users', function (req, res) {
    console.log("get users");
    users.getAll(function (err, pushAss) {
        if (!err) {
        	console.log("USERS: "+JSON.stringify(pushAss));  
            res.status(200).send(
               JSON.stringify(pushAss,null,"\t")
           );
        } else {
            res.send(503)
        }
    });
});

app.get('/teams', function (req, res) {
    console.log("get teams");
    users.getAllActiveTeams(function (err, teams) {
        if (!err) {
        	console.log("Teams: "+JSON.stringify(teams));  
            res.status(200).send(
               JSON.stringify(teams,null,"\t")
           );
        } else {
            res.send(503)
        }
    });
});


app.use('/',auth);
app.use('/', express.static(__dirname + '/../public'));


// Helpers
function sendNotifications(notifs) {

    notifs.forEach(function (notif) {
        var teams = notif.users,
            androidPayload = notif.data;

	console.log("Teams:"+teams);
	push.sendTeams(teams,androidPayload);
    });

    return true;
}

exports.start = function () {
    app.listen(config.get('webPort'));
    myUser=config.get('auth');
    console.log('Listening on port ' + config.get('webPort') + "...");
};
