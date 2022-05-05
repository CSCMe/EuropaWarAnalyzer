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

    public static Result getResultFromNumber(String line) {
        switch (line) {
            case "1": return Result.WHITE;
            case "2": return Result.WON;
            case "3": return Result.LOST;
            default: return Result.UNKNOWN;
        }
    }

    public static Result getResultFromYesNo(String line) {
        switch (line) {
            case "no":
                return Result.LOST;
            case "yes":
                return Result.WON;
            default:
                return Result.UNKNOWN;
        }
    }

    public String getResultString() {
        return resultString;
    }
}
