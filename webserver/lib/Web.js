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
    var notifs = [req.body];

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

/*app.post('/sendBatch', function (req, res) {
    var notifs = req.body.notifications;

    var notificationsValid = sendNotifications(notifs);

    res.status(notificationsValid ? 200 : 400).send();
});*/

// Utils API
/*app.get('/users/:user/associations', function (req, res) {
    pushAssociations.getForUser(req.params.user, function (err, items) {
        if (!err) {
            res.send({"associations": items});
        } else {
            res.status(503).send();
        }
    });
});*/

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
    users.getAllTeams(function (err, teams) {
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



app.delete('/users/:user', function (req, res) {
    pushController.unsubscribeUser(req.params.user);
    res.send('ok');
});


app.use('/',auth);
app.use('/', express.static(__dirname + '/../public'));


// Helpers
function sendNotifications(notifs) {

    notifs.forEach(function (notif) {
        var teams = notif.teams,
            androidPayload = notif.data;

	push.sendTeams(teams,androidPayload);
    });

    return true;
}

exports.start = function () {
    app.listen(config.get('webPort'));
    myUser=config.get('auth');
    console.log('Listening on port ' + config.get('webPort') + "...");
};
