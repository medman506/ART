package at.fhj.mad.art.interfaces;

/**
 * Interface for Un-/Subscribe-Functionality given by HttpSubscriptionHelper
 */
public interface ICallbackHttpPOSTHelper {

    /**
     * Called by HttpSubscriptionHelper after a subscribe-request has been send to the Sever
     * Manages the Response from the Server and how the App will react on that response
     *
     * @param response Response-Code from the Server
     */
    void finished_subscribe(String response);

    /**
     * Called by HttpSubscriptionHelper after a unsubscribe-request has been send to the Server
     * and was successful
     *
     *
     */
    void finished_unsubscribe();

    /**
     * Called by HttpSubscriptionHelper after either a subscribe or unsubscribe Request has failed.
     */
    void finished_error();

}
