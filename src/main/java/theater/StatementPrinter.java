package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private static final int BASE_SALARY = 40000;
    private static final int TRAGEDY_FREE_AUDIENCE = 30;
    private static final int TRAGEDY_EXTRA_COST_PER_PERSON = 1000;
    private static final int CENTS_IN_A_DOLLAR = 100;

    private static Map<String, Play> plays;
    private final Invoice invoice;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;

        final StringBuilder result = new StringBuilder(
                "Statement for " + invoice.getCustomer() + System.lineSeparator());

        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance p : invoice.getPerformances()) {
            final Play play = plays.get(p.getPlayID());

            final int rslt = getThisAmount(p);

            // add volume credits
            volumeCredits += getVolumeCredits(p, play);

            // print line for this order
            result.append(String.format(
                    "  %s: %s (%s seats)%n", play.getName(), frmt.format(
                            rslt / CENTS_IN_A_DOLLAR), p.getAudience()));
            totalAmount += rslt;
        }
        result.append(String.format("Amount owed is %s%n", frmt.format(totalAmount / CENTS_IN_A_DOLLAR)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private static int getVolumeCredits(Performance performance, Play play) {
        int result = 0;
        result += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(play.getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    private static int getThisAmount(Performance performance) {
        int thisAmount = 0;
        final Play play = getPlay(performance);
        switch (play.getType()) {
            case "tragedy":
                thisAmount = BASE_SALARY;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += TRAGEDY_EXTRA_COST_PER_PERSON * (performance.getAudience() - TRAGEDY_FREE_AUDIENCE);
                }
                break;
            case "comedy":
                thisAmount = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return thisAmount;
    }

    /**
      * Helper for get this amount.
      * @param performance the performance whose play we want
      * @return the play name
     */
    public static Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }
}
