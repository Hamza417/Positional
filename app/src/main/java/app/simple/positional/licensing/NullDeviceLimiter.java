package app.simple.positional.licensing;

/**
 * A DeviceLimiter that doesn't limit the number of devices that can use a
 * given user's license.
 * <p>
 * Unless you have reason to believe that your application is being pirated
 * by multiple users using the same license (signing in to Market as the same
 * user), we recommend you use this implementation.
 */
public class NullDeviceLimiter implements DeviceLimiter {
    public int isDeviceAllowed() {
        return Policy.LICENSED;
    }
}
