package theater;

/**
 * Class representing a performance of a play.
 */
public class Performance {

    private int audience;
    private final String playID;

    public Performance(String playID, int audience) {
        this.playID = playID;
        this.audience = audience;
    }

    public int getAudience() {
        return audience;
    }

    public String getPlayID() {
        return playID;
    }
}
