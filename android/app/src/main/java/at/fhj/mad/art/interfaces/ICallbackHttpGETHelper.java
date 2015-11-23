package at.fhj.mad.art.interfaces;

/**
 * Interface for our HttpGETHelper-Class
 */
public interface ICallbackHttpGETHelper {

    /**
     * Get the actual status (online or offline) of our Pushserver
     *
     * @param status Actual status of our Pushserver (true = reachable)
     */
    void isAvailable(boolean status);
}
