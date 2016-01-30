package at.fhj.mad.art.helper;

/**
 * Created by mayerhfl13 on 29.01.2016.
 */
public class TeamResult {

    private boolean isActive;
    private String team;

    public TeamResult(boolean isActive, String team) {
        this.isActive = isActive;
        this.team = team;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
