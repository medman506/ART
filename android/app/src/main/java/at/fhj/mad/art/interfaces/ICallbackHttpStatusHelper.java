package at.fhj.mad.art.interfaces;

/**
 * Interface for our HttpStatusHelper-Class
 */
public interface ICallbackHttpStatusHelper {

    /**
     * Get the actual status (online or offline) of our Pushserver
     *
     * @param status Actual status of our Pushserver (true = reachable)
     */
    void isAvailable(boolean status);

}