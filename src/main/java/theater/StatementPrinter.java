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
        final StringBuilder result = new StringBuilder(
                "Statement for " + invoice.getCustomer() + System.lineSeparator());

        for (Performance p : invoice.getPerformances()) {
            final Play play = plays.get(p.getPlayID());
            final int rslt = getAmount(p);
            usd(result, play, rslt, p.getAudience());
        }

        extracted(result, getTotalAmount());
        result.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));
        return result.toString();
    }

    /**
     * Computes the total amount owed for all performances in the invoice.
     * Iterates through each performance, calculates its cost, and accumulates
     * the sum into a single total.
     *
     * @return the total amount owed, in cents
     */
    public int getTotalAmount() {
        int totalAmount = 0;
        for (Performance p : invoice.getPerformances()) {
            final int rslt = getAmount(p);
            totalAmount += rslt;
        }
        return totalAmount;
    }

    /**
     * Computes the total volume credits earned for all performances in the invoice.
     * Iterates through each performance, determines the credits for each, and
     * accumulates them into a single total.
     *
     * @return the total volume credits
     */
    public int getTotalVolumeCredits() {
        int volumeCredits = 0;
        for (Performance p : invoice.getPerformances()) {
            final Play play = getPlay(p);
            volumeCredits += getVolumeCredits(p, play);
        }
        return volumeCredits;
    }

    private static void extracted(StringBuilder result, int totalAmount) {
        extracted(result, String.format("Amount owed is %s%n", NumberFormat.getCurrencyInstance(Locale.US).format(
                totalAmount / CENTS_IN_A_DOLLAR)));
    }

    private static void extracted(StringBuilder result, String usd) {
        result.append(usd);
    }

    private static void usd(StringBuilder result,
                                              Play play,
                                              int amount,
                                              int audience) {
        result.append(String.format(
                "  %s: %s (%s seats)%n",
                play.getName(),
                NumberFormat.getCurrencyInstance(Locale.US).format(amount / CENTS_IN_A_DOLLAR),
                audience));
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

    private static int getAmount(Performance performance) {
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
