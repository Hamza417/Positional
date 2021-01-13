package app.simple.positional.licensing;

/**
 * Policy used by {@link LicenseChecker} to determine whether a user should have
 * access to the application.
 */
public interface Policy {
    
    /**
     * LICENSED means that the server returned back a valid license response
     */
    int LICENSED = 0x0100;
    /**
     * NOT_LICENSED means that the server returned back a valid license response
     * that indicated that the user definitively is not licensed
     */
    int NOT_LICENSED = 0x0231;
    /**
     * RETRY means that the license response was unable to be determined ---
     * perhaps as a result of faulty networking
     */
    int RETRY = 0x0123;
    
    /**
     * Provide results from contact with the license server. Retry counts are
     * incremented if the current value of response is RETRY. Results will be
     * used for any future policy decisions.
     *
     * @param response the result from validating the server response
     * @param rawData  the raw server response data, can be null for RETRY
     */
    void processServerResponse(int response, ResponseData rawData);
    
    /**
     * Check if the user should be allowed access to the application.
     */
    boolean allowAccess();
    
    /**
     * Gets the licensing URL returned by the server that can enable access for unlicensed apps (e.g.
     * buy app on the Play Store).
     */
    String getLicensingUrl();
}
