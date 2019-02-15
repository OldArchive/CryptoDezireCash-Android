package coin2play.org.coin2playwallet.rate;

/**
 * Created by akshaynexus on 7/5/17.
 */
public class RequestCoin2PlayRateException extends Exception {
    public RequestCoin2PlayRateException(String message) {
        super(message);
    }

    public RequestCoin2PlayRateException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestCoin2PlayRateException(Exception e) {
        super(e);
    }
}
