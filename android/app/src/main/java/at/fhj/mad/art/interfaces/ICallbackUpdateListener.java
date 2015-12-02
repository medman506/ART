package at.fhj.mad.art.interfaces;

/**
 * Interface for the List-Update after a Notification has been received
 */
public interface ICallbackUpdateListener {

    /**
     * Update the Tasklist with the newest Tasks from the DB
     */
    void handleListUpdate();
}
