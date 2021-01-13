package app.simple.positional.licensing;

/**
 * Allows the developer to limit the number of devices using a single license.
 * <p>
 * The LICENSED response from the server contains a user identifier unique to
 * the &lt;application, user&gt; pair. The developer can send this identifier
 * to their own server along with some device identifier (a random number
 * generated and stored once per application installation,
 * {@link android.telephony.TelephonyManager#getDeviceId getDeviceId},
 * {@link android.provider.Settings.Secure#ANDROID_ID ANDROID_ID}, etc).
 * The more sources used to identify the device, the harder it will be for an
 * attacker to spoof.
 * <p>
 * The server can look at the &lt;application, user, device id&gt; tuple and
 * restrict a user's application license to run on at most 10 different devices
 * in a week (for example). We recommend not being too restrictive because a
 * user might legitimately have multiple devices or be in the process of
 * changing phones. This will catch egregious violations of multiple people
 * sharing one license.
 */
public interface DeviceLimiter {
    
    /**
     * Checks if this device is allowed to use the given user's license.
     *
     * @return LICENSED if the device is allowed, NOT_LICENSED if not, RETRY if an error occurs
     */
    @SuppressWarnings ("SameReturnValue")
    int isDeviceAllowed();
}
