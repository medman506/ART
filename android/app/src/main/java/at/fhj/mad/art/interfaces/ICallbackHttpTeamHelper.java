package at.fhj.mad.art.interfaces;

import at.fhj.mad.art.helper.TeamResult;

/**
 * Interface for our HttpStatusHelper-Class
 */
public interface ICallbackHttpTeamHelper {

    /**
     * Callback Method to return TeamResult object to caller
     *
     * @param tr: TeamResult Object
     */
    void returnTeamResult(TeamResult tr);

}
