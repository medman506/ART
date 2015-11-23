var map, lat, long, latlong;
var latlong_arr = [];
var locations;

function setLocations(locationsarray, callback) {
    locations = locationsarray;
    callback();
}

function initMap() {
    latlong = new google.maps.LatLng(47.1500821, 14.403381);
    var mapOptions = {
        zoom: 9,
        center: latlong,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        mapTypeControl: false,
        streetViewControl: false
    }
    map = new google.maps.Map(document.getElementById('map'), mapOptions);
    var marker, i;
    var image='img/user.svg';
    for(i=0; i < locations.length; i++) {
        marker = new google.maps.Marker({
            position: new google.maps.LatLng(locations[i][1], locations[i][2]),
            map: map,
            title: locations[i][0],
            icon: image
        });
    }
}

function errorHandler(err) {
  if(err.code == 1) {
    alert('Error: Access is denied!');
  }else if( err.code == 2) {
    alert('Error: Position is unavailable!');
  }else if( err.code == 3) {
    alert('Error: Timeout!');
  }else {
    alert('Error: Unkown!');
  }
}