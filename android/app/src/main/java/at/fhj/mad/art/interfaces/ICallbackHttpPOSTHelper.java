package at.fhj.mad.art.interfaces;

/**
 * Interface for Un-/Subscribe-Functionality given by HttpPOSTHelper
 */
public interface ICallbackHttpPOSTHelper {

    /**
     * Called by HttpPOSTHelper after a subscribe-request has been send to the Sever
     * Manages the Response from the Server and how the App will react on that response
     *
     * @param response Response-Code from the Server
     */
    void finished_subscribe(String response);

    /**
     * Called by HttpPOSTHelper after a unsubscribe-request has been send to the Server
     * Manages the response form the Server and how the App will react on that response
     *
     * @param response Response-Code from the Server
     */
    void finished_unsubscribe(String response);

    /**
     * Called by HttpPOSTHelper after either a subscribe or unsubscribe Request has failed.
     */
    void finished_error();

}
