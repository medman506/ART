package at.fhj.mad.art.interfaces;

import android.location.Location;

/**
 * Interface for our PositionListener-Class
 */
public interface ICallbackPositionListener {

    /**
     * Return exact position of the device (if GPS is on)
     * If GPS is not on, position will be taken from network service (not exact position)
     *
     * @param location Position of the device
     */
    void handleLocation(Location location);
}
