package ee.tkasekamp.europawaranalyzer.core;

/**
 * Enum for Results of Battles and Wars
 */
public enum Result {
    WON ("Won"),
    LOST ("Lost"),
    WHITE ("White Peace"),
    UNKNOWN ("Unknown"),
    UNFINISHED ("Unfinished");
    private final String resultString;
    Result(String resultString) { this.resultString = resultString; }
    public String getResultString() {
        return resultString;
    }
}
