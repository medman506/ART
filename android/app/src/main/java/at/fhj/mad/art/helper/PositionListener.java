package at.fhj.mad.art.helper;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import at.fhj.mad.art.interfaces.ICallbackPositionListener;

/**
 * Androids LocationListener methods are implemented here
 */
public class PositionListener implements LocationListener {

    private ICallbackPositionListener iCallbackPositionListener;

    @Override
    public void onLocationChanged(Location location) {
        iCallbackPositionListener.handleLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void setiCallbackPositionListener(ICallbackPositionListener iCallbackPositionListener) {
        this.iCallbackPositionListener = iCallbackPositionListener;
    }
}
