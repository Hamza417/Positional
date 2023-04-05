package app.simple.positional.math;

import java.time.ZonedDateTime;

/**
 * A very simple yet accurate Sun/Moon calculator without using JPARSEC library.
 *
 * @author T. Alonso Albi - OAN (Spain), email t.alonso@oan.es
 * @version May 25, 2017 (fixed nutation correction and moon age, better accuracy in Moon)
 */
public class SunMoonCalculator {

    /**
     * Radians to degrees.
     */
    public static final double RAD_TO_DEG = 180.0 / Math.PI;

    /**
     * Degrees to radians.
     */
    public static final double DEG_TO_RAD = 1.0 / RAD_TO_DEG;

    /* Arcseconds to radians */
    public static final double ARCSEC_TO_RAD = (DEG_TO_RAD / 3600.0);

    /**
     * Astronomical Unit in km. As defined by JPL.
     */
    public static final double AU = 149597870.691;

    /**
     * Earth equatorial radius in km. IERS 2003 Conventions.
     */
    public static final double EARTH_RADIUS = 6378.1366;

    /**
     * Two times Pi.
     */
    public static final double TWO_PI = 2.0 * Math.PI;

    /**
     * Pi divided by two.
     */
    public static final double PI_OVER_TWO = Math.PI / 2.0;

    /**
     * Julian century conversion constant = 100 * days per year.
     */
    public static final double JULIAN_DAYS_PER_CENTURY = 36525.0;

    /**
     * Seconds in one day.
     */
    public static final double SECONDS_PER_DAY = 86400;

    /**
     * Light time in days for 1 AU. DE405 definition.
     */
    public static final double LIGHT_TIME_DAYS_PER_AU = 0.00577551833109;

    /**
     * Our default epoch. The Julian Day which represents noon on 2000-01-01.
     */
    public static final double J2000 = 2451545.0;

    /**
     * The set of twilights to calculate (types of rise/set events).
     */
    public enum TWILIGHT {
        /**
         * Event ID for calculation of rising and setting times for astronomical
         * twilight. In this case, the calculated time will be the time when the
         * center of the object is at -18 degrees of geometrical elevation below the
         * astronomical horizon. At this time astronomical observations are possible
         * because the sky is dark enough.
         */
        ASTRONOMICAL,
        /**
         * Event ID for calculation of rising and setting times for nautical
         * twilight. In this case, the calculated time will be the time when the
         * center of the object is at -12 degrees of geometric elevation below the
         * astronomical horizon.
         */
        NAUTICAL,
        /**
         * Event ID for calculation of rising and setting times for civil twilight.
         * In this case, the calculated time will be the time when the center of the
         * object is at -6 degrees of geometric elevation below the astronomical
         * horizon.
         */
        CIVIL,
        /**
         * The standard value of 34' for the refraction at the local horizon.
         */
        HORIZON_34arcmin
    }

    /**
     * Possible options to return the rise/set/transit times.
     * Note in the {@linkplain Ephemeris} fields rise/set times
     * are still returned in UT.
     */
    public enum TWILIGHT_MODE {
        /**
         * Closest events.
         */
        CLOSEST,
        /**
         * Compute events for the current date in UT.
         */
        TODAY_UT,
        /**
         * Compute events for the current date in LT.
         */
        TODAY_LT;

        /**
         * Time zone for option {@linkplain #TODAY_LT}, LT-UT, hours.
         */
        public static int timeZone = 0;
    }

    /**
     * The set of events to calculate (rise/set/transit events).
     */
    public enum EVENT {
        /**
         * Rise.
         */
        RISE,
        /**
         * Set.
         */
        SET,
        /**
         * Transit.
         */
        TRANSIT
    }

    /**
     * The set of phases to compute the moon phases.
     */
    public enum MOONPHASE {
        /**
         * New Moon phase.
         */
        NEW_MOON("New Moon:        ", 0),
        /**
         * Crescent quarter phase.
         */
        CRESCENT_QUARTER("Crescent quarter:", 0.25),
        /**
         * Full Moon phase.
         */
        FULL_MOON("Full Moon:       ", 0.5),
        /**
         * Descent quarter phase.
         */
        DESCENT_QUARTER("Descent quarter: ", 0.75);

        /**
         * Phase name.
         */
        public final String phaseName;
        /**
         * Phase value.
         */
        public final double phase;

        MOONPHASE(String name, double ph) {
            phaseName = name;
            phase = ph;
        }
    }

    /**
     * The set of bodies to compute ephemerides.
     */
    public enum BODY {
        MERCURY(0, 2439.7), VENUS(1, 6051.8), MARS(3, 3396.19),
        JUPITER(4, 71492), SATURN(5, 60268), URANUS(6, 25559), NEPTUNE(7, 24764),
        Moon(-2, 1737.4), Sun(-1, 696000), EMB(2, 0);

        /**
         * Equatorial radius in km.
         */
        public final double eqRadius;
        /**
         * Body index for computing the position.
         */
        public final int index;

        BODY(int i, double r) {
            index = i;
            eqRadius = r;
        }
    }

    /**
     * Input values and nutation/obliquity parameters only calculated once.
     */
    private final double obsLon;
    private final double obsLat;
    private final double obsAlt;
    private TWILIGHT twilight = TWILIGHT.HORIZON_34arcmin;
    private TWILIGHT_MODE twilightMode = TWILIGHT_MODE.CLOSEST;
    protected double jd_UT = 0;
    protected double t = 0;
    protected double nutLon = 0, nutObl = 0;
    protected double meanObliquity = 0;
    protected double TTminusUT;
    protected double lst = 0;

    /**
     * Class to hold the results of ephemerides.
     *
     * @author T. Alonso Albi - OAN (Spain)
     */
    public static class Ephemeris {
        private Ephemeris(double azi, double alt, double rise2, double set2,
                          double transit2, double transit_alt, double ra, double dec,
                          double dist, double eclLon, double eclLat, double angR) {
            azimuth = azi;
            elevation = alt;
            rise = rise2;
            set = set2;
            transit = transit2;
            transitElevation = transit_alt;
            rightAscension = ra;
            declination = dec;
            distance = dist;
            illuminationPhase = 100;
            eclipticLongitude = eclLon;
            eclipticLatitude = eclLat;
            angularRadius = angR;
        }

        /**
         * Values for azimuth, elevation, rise, set, and transit for the Sun. Angles in radians, rise ...
         * as Julian days in UT. Distance in AU.
         */
        public double azimuth, elevation, rise, set, transit, transitElevation, distance, rightAscension,
                declination, illuminationPhase, eclipticLongitude, eclipticLatitude, angularRadius;
    }

    /**
     * Ephemeris for the Sun and Moon bodies.
     */
    public Ephemeris sun, moon;

    /**
     * Moon's age in days as an independent variable.
     */
    public double moonAge;
    public double moonPhase;


    /**
     * Main constructor for Sun/Moon calculations. Time should be given in
     * Universal Time (UT), observer angles in radians.
     *
     * @param year   The year.
     * @param month  The month.
     * @param day    The day.
     * @param h      The hour.
     * @param m      Minute.
     * @param s      Second.
     * @param obsLon Longitude for the observer.
     * @param obsLat Latitude for the observer.
     * @param obsAlt Altitude of the observer in m.
     */
    public SunMoonCalculator(int year, int month, int day, int h, int m, int s, double obsLon, double obsLat, int obsAlt) {
        double jd = toJulianDay(year, month, day, h, m, s);

        double ndot = -25.858, c0 = 0.91072 * (ndot + 26.0);
        if (year < -500 || year >= 2200) {
            double u = (jd - 2385800.5) / 36525.0; // centuries since J1820
            TTminusUT = -20 + 32.0 * u * u;
        } else {
            double x = year + (month - 1 + (day - 1) / 30.0) / 12.0;
            double x2 = x * x, x3 = x2 * x, x4 = x3 * x;
            if (year < 1600) {
                TTminusUT = 10535.328003 - 9.9952386275 * x + 0.00306730763 * x2 - 7.7634069836E-6 * x3 + 3.1331045394E-9 * x4 +
                        8.2255308544E-12 * x2 * x3 - 7.4861647156E-15 * x4 * x2 + 1.936246155E-18 * x4 * x3 - 8.4892249378E-23 * x4 * x4;
            } else {
                TTminusUT = -1027175.34776 + 2523.2566254 * x - 1.8856868491 * x2 + 5.8692462279E-5 * x3 + 3.3379295816E-7 * x4 +
                        1.7758961671E-10 * x2 * x3 - 2.7889902806E-13 * x2 * x4 + 1.0224295822E-16 * x3 * x4 - 1.2528102371E-20 * x4 * x4;
            }
            c0 = 0.91072 * (ndot + 25.858);
        }
        double c = -c0 * Math.pow((jd - 2435109.0) / 36525.0, 2);
        if (year < 1955 || year > 2005) TTminusUT += c;

        this.obsLon = obsLon;
        this.obsLat = obsLat;
        this.obsAlt = obsAlt;
        setUTDate(jd);
    }

    public SunMoonCalculator(ZonedDateTime zonedDateTime, double obsLon, double obsLat, int obsAlt) {
        this(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth(), zonedDateTime.getHour(), zonedDateTime.getMinute(), zonedDateTime.getSecond(), obsLon, obsLat, obsAlt);
    }

    /**
     * Sets the rise/set times to return. Default is for the local horizon.
     *
     * @param t The Twilight.
     */
    public void setTwilight(TWILIGHT t) {
        this.twilight = t;
    }

    /**
     * Sets the kind of rise/set times to return. Default is closest events.
     *
     * @param t The Twilight mode.
     */
    public void setTwilightMode(TWILIGHT_MODE t) {
        this.twilightMode = t;
    }

    /**
     * Sets the time zone to use to return rise/set times for today in local time.
     *
     * @param t The time zone (LT-UT in hours) for the twilight mode {@linkplain TWILIGHT_MODE#TODAY_LT}.
     */
    public void setTwilightModeTimeZone(int t) {
        TWILIGHT_MODE.timeZone = t;
    }

    /**
     * Sets the UT date from the provided Julian day and computes the nutation, obliquity, and
     * sidereal time. TT minuts UT1 is not updated since it changes very slowly with time.
     * Use this only to update the computation time for the same year as the one used when the
     * instance was created.
     *
     * @param jd The new Julian day in UT.
     */
    protected void setUTDate(double jd) {
        this.jd_UT = jd;
        this.t = (jd + TTminusUT / SECONDS_PER_DAY - J2000) / JULIAN_DAYS_PER_CENTURY;

        // Compute nutation
        double M1 = (124.90 - 1934.134 * t + 0.002063 * t * t) * DEG_TO_RAD;
        double M2 = (201.11 + 72001.5377 * t + 0.00057 * t * t) * DEG_TO_RAD;
        nutLon = (-(17.2026 + 0.01737 * t) * Math.sin(M1) + (-1.32012 + 0.00013 * t) * Math.sin(M2) + .2088 * Math.sin(2 * M1)) * ARCSEC_TO_RAD;
        nutObl = ((9.2088 + .00091 * t) * Math.cos(M1) + (0.552204 - 0.00029 * t) * Math.cos(M2) - .0904 * Math.cos(2 * M1)) * ARCSEC_TO_RAD;

        // Compute mean obliquity
        double t2 = this.t / 100.0;
        double tmp = t2 * (27.87 + t2 * (5.79 + t2 * 2.45));
        tmp = t2 * (-249.67 + t2 * (-39.05 + t2 * (7.12 + tmp)));
        tmp = t2 * (-1.55 + t2 * (1999.25 + t2 * (-51.38 + tmp)));
        tmp = (t2 * (-4680.93 + tmp)) / 3600.0;
        meanObliquity = (23.4392911111111 + tmp) * DEG_TO_RAD;

        // Obtain local apparent sidereal time
        double jd0 = Math.floor(jd_UT - 0.5) + 0.5;
        double T0 = (jd0 - J2000) / JULIAN_DAYS_PER_CENTURY;
        double secs = (jd_UT - jd0) * SECONDS_PER_DAY;
        double gmst = (((((-6.2e-6 * T0) + 9.3104e-2) * T0) + 8640184.812866) * T0) + 24110.54841;
        double msday = 1.0 + (((((-1.86e-5 * T0) + 0.186208) * T0) + 8640184.812866) / (SECONDS_PER_DAY * JULIAN_DAYS_PER_CENTURY));
        gmst = (gmst + msday * secs) * (15.0 / 3600.0) * DEG_TO_RAD;
        lst = normalizeRadians(gmst + obsLon + nutLon * Math.cos(meanObliquity + nutObl));
    }

    /**
     * Calculates everything for the Sun and the Moon.
     */
    public void calcSunAndMoon() {
        double jd = this.jd_UT;

        // First the Sun
        sun = doCalc(getSun());

        int niter = 15; // Number of iterations to get accurate rise/set/transit times
        sun.rise = obtainAccurateRiseSetTransit(sun.rise, EVENT.RISE, niter, true);
        sun.set = obtainAccurateRiseSetTransit(sun.set, EVENT.SET, niter, true);
        sun.transit = obtainAccurateRiseSetTransit(sun.transit, EVENT.TRANSIT, niter, true);
        if (sun.transit == -1) {
            sun.transitElevation = 0;
        } else {
            // Update Sun's maximum elevation
            setUTDate(sun.transit);
            sun.transitElevation = doCalc(getSun()).transitElevation;
        }

        // Now Moon
        setUTDate(jd);
        moon = doCalc(getMoon());
        double ma = moonAge;
        double ph = moonPhase;

        //niter = 15; // Number of iterations to get accurate rise/set/transit times
        moon.rise = obtainAccurateRiseSetTransit(moon.rise, EVENT.RISE, niter, false);
        moon.set = obtainAccurateRiseSetTransit(moon.set, EVENT.SET, niter, false);
        moon.transit = obtainAccurateRiseSetTransit(moon.transit, EVENT.TRANSIT, niter, false);
        if (moon.transit == -1) {
            moon.transitElevation = 0;
        } else {
            // Update Moon's maximum elevation
            setUTDate(moon.transit);
            moon.transitElevation = doCalc(getMoon()).transitElevation;
        }

        setUTDate(jd);
        moonAge = ma;
        moonPhase = ph;

        // Compute illumination phase percentage for the Moon
        getIlluminationPhase(moon);
    }

    // Sun data from the expansion "Planetary Programs
    // and Tables" by Pierre Bretagnon and Jean-Louis
    // Simon, Willman-Bell, 1986
    private static final double[][] sun_elements = {
            new double[]{403406.0, 0.0, 4.721964, 1.621043},
            new double[]{195207.0, -97597.0, 5.937458, 62830.348067},
            new double[]{119433.0, -59715.0, 1.115589, 62830.821524},
            new double[]{112392.0, -56188.0, 5.781616, 62829.634302},
            new double[]{3891.0, -1556.0, 5.5474, 125660.5691},
            new double[]{2819.0, -1126.0, 1.512, 125660.9845},
            new double[]{1721.0, -861.0, 4.1897, 62832.4766},
            new double[]{0.0, 941.0, 1.163, .813},
            new double[]{660.0, -264.0, 5.415, 125659.31},
            new double[]{350.0, -163.0, 4.315, 57533.85},
            new double[]{334.0, 0.0, 4.553, -33.931},
            new double[]{314.0, 309.0, 5.198, 777137.715},
            new double[]{268.0, -158.0, 5.989, 78604.191},
            new double[]{242.0, 0.0, 2.911, 5.412},
            new double[]{234.0, -54.0, 1.423, 39302.098},
            new double[]{158.0, 0.0, .061, -34.861},
            new double[]{132.0, -93.0, 2.317, 115067.698},
            new double[]{129.0, -20.0, 3.193, 15774.337},
            new double[]{114.0, 0.0, 2.828, 5296.67},
            new double[]{99.0, -47.0, .52, 58849.27},
            new double[]{93.0, 0.0, 4.65, 5296.11},
            new double[]{86.0, 0.0, 4.35, -3980.7},
            new double[]{78.0, -33.0, 2.75, 52237.69},
            new double[]{72.0, -32.0, 4.5, 55076.47},
            new double[]{68.0, 0.0, 3.23, 261.08},
            new double[]{64.0, -10.0, 1.22, 15773.85},
            new double[]{46.0, -16.0, .14, 188491.03},
            new double[]{38.0, 0.0, 3.44, -7756.55},
            new double[]{37.0, 0.0, 4.37, 264.89},
            new double[]{32.0, -24.0, 1.14, 117906.27},
            new double[]{29.0, -13.0, 2.84, 55075.75},
            new double[]{28.0, 0.0, 5.96, -7961.39},
            new double[]{27.0, -9.0, 5.09, 188489.81},
            new double[]{27.0, 0.0, 1.72, 2132.19},
            new double[]{25.0, -17.0, 2.56, 109771.03},
            new double[]{24.0, -11.0, 1.92, 54868.56},
            new double[]{21.0, 0.0, .09, 25443.93},
            new double[]{21.0, 31.0, 5.98, -55731.43},
            new double[]{20.0, -10.0, 4.03, 60697.74},
            new double[]{18.0, 0.0, 4.27, 2132.79},
            new double[]{17.0, -12.0, .79, 109771.63},
            new double[]{14.0, 0.0, 4.24, -7752.82},
            new double[]{13.0, -5.0, 2.01, 188491.91},
            new double[]{13.0, 0.0, 2.65, 207.81},
            new double[]{13.0, 0.0, 4.98, 29424.63},
            new double[]{12.0, 0.0, .93, -7.99},
            new double[]{10.0, 0.0, 2.21, 46941.14},
            new double[]{10.0, 0.0, 3.59, -68.29},
            new double[]{10.0, 0.0, 1.5, 21463.25},
            new double[]{10.0, -9.0, 2.55, 157208.4}
    };

    protected double[] getSun() {
        double L = 0.0, R = 0.0, t2 = t * 0.01;
        double Lp = 0.0, deltat = 0.5, t2p = (t + deltat / JULIAN_DAYS_PER_CENTURY) * 0.01;
        for (double[] sun_element : sun_elements) {
            double v = sun_element[2] + sun_element[3] * t2;
            double u = normalizeRadians(v);
            L = L + sun_element[0] * Math.sin(u);
            R = R + sun_element[1] * Math.cos(u);

            double vp = sun_element[2] + sun_element[3] * t2p;
            double up = normalizeRadians(vp);
            Lp = Lp + sun_element[0] * Math.sin(up);
        }

        double lon = normalizeRadians(4.9353929 + normalizeRadians(62833.196168 * t2) + L / 10000000.0) * RAD_TO_DEG;
        double sdistance = 1.0001026 + R / 10000000.0;

        // Now subtract aberration
        double dlon = ((Lp - L) / 10000000.0 + 62833.196168 * (t2p - t2)) / deltat;
        double aberration = dlon * sdistance * LIGHT_TIME_DAYS_PER_AU;
        lon -= aberration * RAD_TO_DEG;

        double slongitude = lon * DEG_TO_RAD; // apparent longitude (error<0.001 deg)
        double slatitude = 0; // Sun's ecliptic latitude is always negligible

        return new double[]{slongitude, slatitude, sdistance, Math.atan(BODY.Sun.eqRadius / (AU * sdistance))};
    }

    protected double[] getMoon() {
        // These expansions up to t^7 for the mean elements are taken from S. L. Moshier
        /* Mean elongation of moon = D */
        double x = (1.6029616009939659e+09 * t + 1.0722612202445078e+06);
        x += (((((-3.207663637426e-013 * t + 2.555243317839e-011) * t + 2.560078201452e-009) * t - 3.702060118571e-005) * t + 6.9492746836058421e-03) * t /* D, t^3 */
                - 6.7352202374457519e+00) * t * t; /* D, t^2 */
        double phase = normalizeRadians(ARCSEC_TO_RAD * x);

        /* Mean distance of moon from its ascending node = F */
        x = (1.7395272628437717e+09 * t + 3.3577951412884740e+05);
        x += (((((4.474984866301e-013 * t + 4.189032191814e-011) * t - 2.790392351314e-009) * t - 2.165750777942e-006) * t - 7.5311878482337989e-04) * t /* F, t^3 */
                - 1.3117809789650071e+01) * t * t; /* F, t^2 */
        double node = normalizeRadians(ARCSEC_TO_RAD * x);

        /* Mean anomaly of sun = l' (J. Laskar) */
        x = (1.2959658102304320e+08 * t + 1.2871027407441526e+06);
        x += ((((((((1.62e-20 * t - 1.0390e-17) * t - 3.83508e-15) * t + 4.237343e-13) * t + 8.8555011e-11) * t - 4.77258489e-8) * t - 1.1297037031e-5) * t + 8.7473717367324703e-05) * t - 5.5281306421783094e-01) * t * t;
        double sanomaly = normalizeRadians(ARCSEC_TO_RAD * x);

        /* Mean anomaly of moon = l */
        x = (1.7179159228846793e+09 * t + 4.8586817465825332e+05);
        x += (((((-1.755312760154e-012 * t + 3.452144225877e-011) * t - 2.506365935364e-008) * t - 2.536291235258e-004) * t + 5.2099641302735818e-02) * t /* l, t^3 */
                + 3.1501359071894147e+01) * t * t; /* l, t^2 */
        double anomaly = normalizeRadians(ARCSEC_TO_RAD * x);

        /* Mean longitude of moon, re mean ecliptic and equinox of date = L */
        x = (1.7325643720442266e+09 * t + 7.8593980921052420e+05);
        x += (((((7.200592540556e-014 * t + 2.235210987108e-010) * t - 1.024222633731e-008) * t - 6.073960534117e-005) * t + 6.9017248528380490e-03) * t /* L, t^3 */
                - 5.6550460027471399e+00) * t * t; /* L, t^2 */
        double l = normalizeRadians(ARCSEC_TO_RAD * x) * RAD_TO_DEG;

        // Now longitude, with the three main correcting terms of evection,
        // variation, and equation of year, plus other terms (error<0.01 deg)
        // P. Duffet's MOON program taken as reference for the periodic terms
        double E = 1.0 - (.002495 + 7.52E-06 * (t + 1.0)) * (t + 1.0), E2 = E * E;
        double td = t + 1, td2 = t * t;
        double M6 = td * JULIAN_DAYS_PER_CENTURY * 360.0 / 6.798363307E3;
        double NA = normalizeRadians((2.59183275E2 - M6 + (2.078E-3 + 2.2E-6 * td) * td2) * DEG_TO_RAD);
        double C = NA + DEG_TO_RAD * (275.05 - 2.3 * td);

        l += 6.28875 * Math.sin(anomaly) + 1.274018 * Math.sin(2 * phase - anomaly) + .658309 * Math.sin(2 * phase);
        l += 0.213616 * Math.sin(2 * anomaly) - E * .185596 * Math.sin(sanomaly) - 0.114336 * Math.sin(2 * node);
        l += .058793 * Math.sin(2 * phase - 2 * anomaly) + .057212 * E * Math.sin(2 * phase - anomaly - sanomaly) + .05332 * Math.sin(2 * phase + anomaly);
        l += .045874 * E * Math.sin(2 * phase - sanomaly) + .041024 * E * Math.sin(anomaly - sanomaly) - .034718 * Math.sin(phase) - E * .030465 * Math.sin(sanomaly + anomaly);
        l += .015326 * Math.sin(2 * (phase - node)) - .012528 * Math.sin(2 * node + anomaly) - .01098 * Math.sin(2 * node - anomaly) + .010674 * Math.sin(4 * phase - anomaly);
        l += .010034 * Math.sin(3 * anomaly) + .008548 * Math.sin(4 * phase - 2 * anomaly);
        l += -E * .00791 * Math.sin(sanomaly - anomaly + 2 * phase) - E * .006783 * Math.sin(2 * phase + sanomaly) + .005162 * Math.sin(anomaly - phase) + E * .005 * Math.sin(sanomaly + phase);
        l += .003862 * Math.sin(4 * phase) + E * .004049 * Math.sin(anomaly - sanomaly + 2 * phase) + .003996 * Math.sin(2 * (anomaly + phase)) + .003665 * Math.sin(2 * phase - 3 * anomaly);
        l += E * 2.695E-3 * Math.sin(2 * anomaly - sanomaly) + 2.602E-3 * Math.sin(anomaly - 2 * (node + phase));
        l += E * 2.396E-3 * Math.sin(2 * (phase - anomaly) - sanomaly) - 2.349E-3 * Math.sin(anomaly + phase);
        l += E * E * 2.249E-3 * Math.sin(2 * (phase - sanomaly)) - E * 2.125E-3 * Math.sin(2 * anomaly + sanomaly);
        l += -E * E * 2.079E-3 * Math.sin(2 * sanomaly) + E * E * 2.059E-3 * Math.sin(2 * (phase - sanomaly) - anomaly);
        l += -1.773E-3 * Math.sin(anomaly + 2 * (phase - node)) - 1.595E-3 * Math.sin(2 * (node + phase));
        l += E * 1.22E-3 * Math.sin(4 * phase - sanomaly - anomaly) - 1.11E-3 * Math.sin(2 * (anomaly + node));
        l += 8.92E-4 * Math.sin(anomaly - 3 * phase) - E * 8.11E-4 * Math.sin(sanomaly + anomaly + 2 * phase);
        l += E * 7.61E-4 * Math.sin(4 * phase - sanomaly - 2 * anomaly);
        l += E2 * 7.04E-4 * Math.sin(anomaly - 2 * (sanomaly + phase));
        l += E * 6.93E-4 * Math.sin(sanomaly - 2 * (anomaly - phase));
        l += E * 5.98E-4 * Math.sin(2 * (phase - node) - sanomaly);
        l += 5.5E-4 * Math.sin(anomaly + 4 * phase) + 5.38E-4 * Math.sin(4 * anomaly);
        l += E * 5.21E-4 * Math.sin(4 * phase - sanomaly) + 4.86E-4 * Math.sin(2 * anomaly - phase);
        l += E2 * 7.17E-4 * Math.sin(anomaly - 2 * sanomaly);

        double longitude = l * DEG_TO_RAD;
        moonPhase = phase;

        double Psin = 29.530588853;
        if (sun != null) {
            // Get Moon age, more accurate than 'phase' but we need the Sun position
            moonAge = normalizeRadians(longitude - sun.eclipticLongitude) * Psin / TWO_PI;
        } else {
            // Use the phase variable as estimate, less accurate but this is used only when we don't need an accurate value
            moonAge = phase * Psin / TWO_PI;
        }

        // Now Moon parallax
        double p = .950724 + .051818 * Math.cos(anomaly) + .009531 * Math.cos(2 * phase - anomaly);
        p += .007843 * Math.cos(2 * phase) + .002824 * Math.cos(2 * anomaly);
        p += 0.000857 * Math.cos(2 * phase + anomaly) + E * .000533 * Math.cos(2 * phase - sanomaly);
        p += E * .000401 * Math.cos(2 * phase - anomaly - sanomaly) + E * .00032 * Math.cos(anomaly - sanomaly) - .000271 * Math.cos(phase);
        p += -E * .000264 * Math.cos(sanomaly + anomaly) - .000198 * Math.cos(2 * node - anomaly);
        p += 1.73E-4 * Math.cos(3 * anomaly) + 1.67E-4 * Math.cos(4 * phase - anomaly);
        p += -E * 1.11E-4 * Math.cos(sanomaly) + 1.03E-4 * Math.cos(4 * phase - 2 * anomaly);
        p += -8.4E-5 * Math.cos(2 * anomaly - 2 * phase) - E * 8.3E-5 * Math.cos(2 * phase + sanomaly);
        p += 7.9E-5 * Math.cos(2 * phase + 2 * anomaly) + 7.2E-5 * Math.cos(4 * phase);
        p += E * 6.4E-5 * Math.cos(2 * phase - sanomaly + anomaly)
                - E * 6.3E-5 * Math.cos(2 * phase + sanomaly - anomaly);
        p += E * 4.1E-5 * Math.cos(sanomaly + phase) + E * 3.5E-5 * Math.cos(2 * anomaly - sanomaly);
        p += -3.3E-5 * Math.cos(3 * anomaly - 2 * phase) - 3E-5 * Math.cos(anomaly + phase);
        p += -2.9E-5 * Math.cos(2 * (node - phase)) - E * 2.9E-5 * Math.cos(2 * anomaly + sanomaly);
        p += E2 * 2.6E-5 * Math.cos(2 * (phase - sanomaly)) - 2.3E-5 * Math.cos(2 * (node - phase) + anomaly);
        p += E * 1.9E-5 * Math.cos(4 * phase - sanomaly - anomaly);

        // So Moon distance in Earth radii is, more or less,
        double distance = 1.0 / Math.sin(p * DEG_TO_RAD);

        // Ecliptic latitude with nodal phase (error<0.01 deg)
        l = 5.128189 * Math.sin(node) + 0.280606 * Math.sin(node + anomaly) + 0.277693 * Math.sin(anomaly - node);
        l += .173238 * Math.sin(2 * phase - node) + .055413 * Math.sin(2 * phase + node - anomaly);
        l += .046272 * Math.sin(2 * phase - node - anomaly) + .032573 * Math.sin(2 * phase + node);
        l += .017198 * Math.sin(2 * anomaly + node) + .009267 * Math.sin(2 * phase + anomaly - node);
        l += .008823 * Math.sin(2 * anomaly - node) + E * .008247 * Math.sin(2 * phase - sanomaly - node) + .004323 * Math.sin(2 * (phase - anomaly) - node);
        l += .0042 * Math.sin(2 * phase + node + anomaly) + E * .003372 * Math.sin(node - sanomaly - 2 * phase);
        l += E * 2.472E-3 * Math.sin(2 * phase + node - sanomaly - anomaly);
        l += E * 2.222E-3 * Math.sin(2 * phase + node - sanomaly);
        l += E * 2.072E-3 * Math.sin(2 * phase - node - sanomaly - anomaly);
        l += E * 1.877E-3 * Math.sin(node - sanomaly + anomaly) + 1.828E-3 * Math.sin(4 * phase - node - anomaly);
        l += -E * 1.803E-3 * Math.sin(node + sanomaly) - 1.75E-3 * Math.sin(3 * node);
        l += E * 1.57E-3 * Math.sin(anomaly - sanomaly - node) - 1.487E-3 * Math.sin(node + phase);
        l += -E * 1.481E-3 * Math.sin(node + sanomaly + anomaly) + E * 1.417E-3 * Math.sin(node - sanomaly - anomaly);
        l += E * 1.35E-3 * Math.sin(node - sanomaly) + 1.33E-3 * Math.sin(node - phase);
        l += 1.106E-3 * Math.sin(node + 3 * anomaly) + 1.02E-3 * Math.sin(4 * phase - node);
        l += 8.33E-4 * Math.sin(node + 4 * phase - anomaly) + 7.81E-4 * Math.sin(anomaly - 3 * node);
        l += 6.7E-4 * Math.sin(node + 4 * phase - 2 * anomaly) + 6.06E-4 * Math.sin(2 * phase - 3 * node);
        l += 5.97E-4 * Math.sin(2 * (phase + anomaly) - node);
        l += E * 4.92E-4 * Math.sin(2 * phase + anomaly - sanomaly - node)
                + 4.5E-4 * Math.sin(2 * (anomaly - phase) - node);
        l += 4.39E-4 * Math.sin(3 * anomaly - node) + 4.23E-4 * Math.sin(node + 2 * (phase + anomaly));
        l += 4.22E-4 * Math.sin(2 * phase - node - 3 * anomaly)
                - E * 3.67E-4 * Math.sin(sanomaly + node + 2 * phase - anomaly);
        l += -E * 3.53E-4 * Math.sin(sanomaly + node + 2 * phase) + 3.31E-4 * Math.sin(node + 4 * phase);
        l += E * 3.17E-4 * Math.sin(2 * phase + node - sanomaly + anomaly);
        l += E2 * 3.06E-4 * Math.sin(2 * (phase - sanomaly) - node) - 2.83E-4 * Math.sin(anomaly + 3 * node);
        double W1 = 4.664E-4 * Math.cos(NA);
        double W2 = 7.54E-5 * Math.cos(C);

        double latitude = l * DEG_TO_RAD * (1.0 - W1 - W2);

        return new double[]{longitude, latitude, distance * EARTH_RADIUS / AU, Math.atan(BODY.Moon.eqRadius / (distance * EARTH_RADIUS))};
    }

    /**
     * Compute the topocentric position of the body.
     *
     * @param pos Values for the ecliptic longitude, latitude, distance and so on from previous methods for the specific body.
     * @return The ephemeris object with the output position
     */
    protected Ephemeris doCalc(double[] pos) {
        return doCalc(pos, false);
    }

    /**
     * Compute the position of the body.
     *
     * @param pos        Values for the ecliptic longitude, latitude, distance and so on from previous methods for the specific body.
     * @param geocentric True to return geocentric position. Set this to false generally.
     * @return The ephemeris object with the output position
     */
    protected Ephemeris doCalc(double[] pos, boolean geocentric) {
        // Correct for nutation in longitude
        pos[0] = pos[0] + nutLon;

        // Ecliptic to equatorial coordinates using true obliquity
        double cl = Math.cos(pos[1]);
        double x = pos[2] * Math.cos(pos[0]) * cl;
        double y = pos[2] * Math.sin(pos[0]) * cl;
        double z = pos[2] * Math.sin(pos[1]);
        double sinEcl = Math.sin(meanObliquity + nutObl), cosEcl = Math.cos(meanObliquity + nutObl);
        double tmp = y * cosEcl - z * sinEcl;
        z = y * sinEcl + z * cosEcl;
        y = tmp;

        // Obtain topocentric rectangular coordinates
        double xtopo = x, ytopo = y, ztopo = z;
        if (!geocentric) {
            double geocLat = (obsLat - .1925 * Math.sin(2 * obsLat) * DEG_TO_RAD);
            double sinLat = Math.sin(geocLat);
            double cosLat = Math.cos(geocLat);
            double geocR = 1.0 - Math.pow(Math.sin(obsLat), 2) / 298.257;
            double radiusAU = (geocR * EARTH_RADIUS + obsAlt * 0.001) / AU;
            double[] correction = new double[]{
                    radiusAU * cosLat * Math.cos(lst),
                    radiusAU * cosLat * Math.sin(lst),
                    radiusAU * sinLat};
            xtopo -= correction[0];
            ytopo -= correction[1];
            ztopo -= correction[2];
        }

        // Obtain topocentric equatorial coordinates
        double ra = 0.0;
        double dec = PI_OVER_TWO;
        if (ztopo < 0.0) dec = -dec;
        if (ytopo != 0.0 || xtopo != 0.0) {
            ra = Math.atan2(ytopo, xtopo);
            dec = Math.atan2(ztopo / Math.hypot(xtopo, ytopo), 1.0);
        }
        double dist = Math.sqrt(xtopo * xtopo + ytopo * ytopo + ztopo * ztopo);

        // Hour angle
        double angh = lst - ra;

        // Obtain azimuth and geometric alt
        double sinLat = Math.sin(obsLat);
        double cosLat = Math.cos(obsLat);
        double sinDec = Math.sin(dec), cosDec = Math.cos(dec);
        double h = sinLat * sinDec + cosLat * cosDec * Math.cos(angh);
        double alt = Math.asin(h);
        double azy = Math.sin(angh);
        double azx = Math.cos(angh) * sinLat - sinDec * cosLat / cosDec;
        double azi = Math.PI + Math.atan2(azy, azx); // 0 = north

        if (geocentric)
            return new Ephemeris(azi, alt, -1, -1, -1, -1, normalizeRadians(ra), dec, dist,
                    pos[0], pos[1], pos[3]);

        // Get apparent elevation
        alt = refraction(alt);

        switch (twilight) {
            case HORIZON_34arcmin:
                // Rise, set, transit times, taking into account Sun/Moon angular radius (pos[3]).
                // The 34' factor is the standard refraction at horizon.
                // Removing angular radius will do calculations for the center of the disk instead
                // of the upper limb.
                tmp = -(34.0 / 60.0) * DEG_TO_RAD - pos[3];
                break;
            case CIVIL:
                tmp = -6 * DEG_TO_RAD;
                break;
            case NAUTICAL:
                tmp = -12 * DEG_TO_RAD;
                break;
            case ASTRONOMICAL:
                tmp = -18 * DEG_TO_RAD;
                break;
        }

        // Compute cosine of hour angle
        tmp = (Math.sin(tmp) - sinLat * sinDec) / (cosLat * cosDec);
        /* Length of a sidereal day in days according to IERS Conventions. */
        double siderealDayLength = 1.00273781191135448;
        double celestialHoursToEarthTime = 1.0 / (siderealDayLength * TWO_PI);

        // Make calculations for the meridian
        double transit_alt = Math.asin(sinDec * sinLat + cosDec * cosLat);
        transit_alt = refraction(transit_alt);

        // Obtain the current transit event in time
        double transit = getTwilightEvent(celestialHoursToEarthTime, ra, 0);

        // Make calculations for rise and set
        double rise = -1, set = -1;
        if (Math.abs(tmp) <= 1.0) {
            double ang_hor = Math.abs(Math.acos(tmp));
            rise = getTwilightEvent(celestialHoursToEarthTime, ra, -ang_hor);
            set = getTwilightEvent(celestialHoursToEarthTime, ra, ang_hor);
        }

        return new Ephemeris(azi, alt, rise, set, transit, transit_alt,
                normalizeRadians(ra), dec, dist, pos[0], pos[1], pos[3]);
    }

    private double getTwilightEvent(double celestialHoursToEarthTime, double ra, double angh) {
        double jdToday_UT = Math.floor(jd_UT - 0.5) + 0.5;

        double eventTime = celestialHoursToEarthTime * normalizeRadians(ra + angh - lst);
        double eventTimePrev = celestialHoursToEarthTime * (normalizeRadians(ra + angh - lst) - TWO_PI);
        double eventDatePrev_UT = Math.floor(jd_UT + eventTimePrev - 0.5) + 0.5;

        if (//jdToday_UT == eventDatePrev_UT &&
                Math.abs(eventTimePrev) < Math.abs(eventTime) && twilightMode == TWILIGHT_MODE.CLOSEST)
            eventTime = eventTimePrev;
        if (twilightMode == TWILIGHT_MODE.TODAY_UT) {
            double eventDate_UT = Math.floor(jd_UT + eventTime - 0.5) + 0.5;
            if (jdToday_UT != eventDate_UT) eventTime = -jd_UT - 1;
            if (jdToday_UT == eventDatePrev_UT) eventTime = eventTimePrev;
        }
        if (twilightMode == TWILIGHT_MODE.TODAY_LT) {
            double tz = TWILIGHT_MODE.timeZone / 24.0, jdToday_LT = Math.floor(jd_UT + tz - 0.5) + 0.5;
            double eventDate_LT = Math.floor(jd_UT + tz + eventTime - 0.5) + 0.5;
            if (jdToday_LT != eventDate_LT) eventTime = -jd_UT - 1;

            double eventDatePrev_LT = Math.floor(jd_UT + tz + eventTimePrev - 0.5) + 0.5;
            if (jdToday_LT == eventDatePrev_LT) eventTime = eventTimePrev;

            double eventTimeNext = celestialHoursToEarthTime * (normalizeRadians(ra + angh - lst) + TWO_PI);
            double eventDateNext_LT = Math.floor(jd_UT + tz + eventTimeNext - 0.5) + 0.5;
            if (jdToday_LT == eventDateNext_LT) eventTime = eventTimeNext;
        }

        return jd_UT + eventTime;
    }

    /**
     * Corrects geometric elevation for refraction if it is greater than -3 degrees.
     *
     * @param alt Geometric elevation in radians.
     * @return Apparent elevation.
     */
    private double refraction(double alt) {
        if (alt <= -3 * DEG_TO_RAD) return alt;

        double altIn = alt, prevAlt = alt;
        int niter = 0;
        do {
            double altOut = computeGeometricElevation(alt);
            alt = altIn - (altOut - alt);
            niter++;
            if (Math.abs(prevAlt - alt) < 0.001 * DEG_TO_RAD) break;
            prevAlt = alt;
        } while (niter < 8);

        return alt;
    }

    /**
     * Compute geometric elevation from apparent elevation. Note ephemerides
     * calculates geometric elevation, so an inversion is required, something
     * achieved in method {@linkplain #refraction(double)} by iteration.
     *
     * @param alt Apparent elevation in radians.
     * @return Geometric elevation in radians.
     */
    private double computeGeometricElevation(double alt) {
        double Ps = 1010; // Pressure in mb
        double Ts = 10 + 273.15; // Temperature in K
        double altDeg = alt * RAD_TO_DEG;

        // Bennet 1982 formulae for optical wavelengths, do the job but not accurate close to horizon
        // Yan 1996 formulae would be better but with much more lines of code
        double r = DEG_TO_RAD * Math.abs(Math.tan(PI_OVER_TWO - (altDeg + 7.31 / (altDeg + 4.4)) * DEG_TO_RAD)) / 60.0;
        double refr = r * (0.28 * Ps / Ts);
        return Math.min(alt - refr, PI_OVER_TWO);

    }

    /**
     * Sets the illumination phase field for the provided body.
     * Sun position must be computed before calling this method.
     *
     * @param body The ephemeris object for this body.
     */
    protected void getIlluminationPhase(Ephemeris body) {
        double dlon = body.rightAscension - sun.rightAscension;
        double cosElong = (Math.sin(sun.declination) * Math.sin(body.declination) +
                Math.cos(sun.declination) * Math.cos(body.declination) * Math.cos(dlon));

        double RE = sun.distance;
        double RO = body.distance;
        // Use elongation cosine as trick to solve the rectangle and get RP (distance body - sun)
        double RP = Math.sqrt(-(cosElong * 2.0 * RE * RO - RE * RE - RO * RO));

        double DPH = ((RP * RP + RO * RO - RE * RE) / (2.0 * RP * RO));
        body.illuminationPhase = 100 * (1.0 + DPH) * 0.5;
    }

    /**
     * Transforms a common date into a Julian day number (counting days from Jan 1, 4713 B.C. at noon).
     * Dates before October, 15, 1582 are assumed to be in the Julian calendar, after that the Gregorian one is used.
     *
     * @param year  Year.
     * @param month Month.
     * @param day   Day.
     * @param h     Hour.
     * @param m     Minute.
     * @param s     Second.
     * @return Julian day number.
     */
    protected double toJulianDay(int year, int month, int day, int h, int m, int s) {
        // The conversion formulas are from Meeus, chapter 7.
        boolean julian = year < 1582 || (year == 1582 && month < 10) || (year == 1582 && month == 10 && day < 15); // Use Gregorian calendar
        int M = month;
        int Y = year;
        if (M < 3) {
            Y--;
            M += 12;
        }
        int A = Y / 100;
        int B = julian ? 0 : 2 - A + A / 4;

        double dayFraction = (h + (m + (s / 60.0)) / 60.0) / 24.0;
        return dayFraction + (int) (365.25D * (Y + 4716)) + (int) (30.6001 * (M + 1)) + day + B - 1524.5;
    }

    /**
     * Transforms a Julian day (rise/set/transit fields) to a common date.
     *
     * @param jd The Julian day.
     * @return A set of integers: year, month, day, hour, minute, second.
     */
    protected static int[] getDate(double jd) {
        // The conversion formulas are from Meeus,
        // Chapter 7.
        double Z = Math.floor(jd + 0.5);
        double F = jd + 0.5 - Z;
        double A = Z;
        if (Z >= 2299161D) {
            int a = (int) ((Z - 1867216.25) / 36524.25);
            A += 1 + a - a / 4.0;
        }
        double B = A + 1524;
        int C = (int) ((B - 122.1) / 365.25);
        int D = (int) (C * 365.25);
        int E = (int) ((B - D) / 30.6001);

        double exactDay = F + B - D - (int) (30.6001 * E);
        int day = (int) exactDay;
        int month = (E < 14) ? E - 1 : E - 13;
        int year = C - 4715;
        if (month > 2) year--;
        double h = ((exactDay - day) * SECONDS_PER_DAY) / 3600.0;

        int hour = (int) h;
        double m = (h - hour) * 60.0;
        int minute = (int) m;
        int second = (int) ((m - minute) * 60.0);

        return new int[]{year, month, day, hour, minute, second};
    }

    /**
     * Returns a date as a string.
     *
     * @param jd The Julian day.
     * @return The String.
     */
    public static String getDateAsString(double jd) {
        if (jd == -1) return "NO RISE/SET/TRANSIT FOR THIS OBSERVER/DATE";

        int[] date = getDate(jd);
        String zyr = "", zmo = "", zh = "", zm = "", zs = "";
        if (date[1] < 10) zyr = "0";
        if (date[2] < 10) zmo = "0";
        if (date[3] < 10) zh = "0";
        if (date[4] < 10) zm = "0";
        if (date[5] < 10) zs = "0";
        return date[0] + "/" + zyr + date[1] + "/" + zmo + date[2] + " " + zh + date[3] + ":" + zm + date[4] + ":" + zs + date[5] + " UT";
    }

    /**
     * Reduce an angle in radians to the range (0 - 2 Pi).
     *
     * @param r Value in radians.
     * @return The reduced radians value.
     */
    protected static double normalizeRadians(double r) {
        if (r < 0 && r >= -TWO_PI) return r + TWO_PI;
        if (r >= TWO_PI && r < 2 * TWO_PI) return r - TWO_PI;
        if (r >= 0 && r < TWO_PI) return r;

        r -= TWO_PI * Math.floor(r / TWO_PI);
        if (r < 0.) r += TWO_PI;

        return r;
    }

    /**
     * Computes an accurate rise/set/transit time for a moving object.
     *
     * @param riseSetJD Start date for the event.
     * @param index     Event identifier.
     * @param niter     Maximum number of iterations.
     * @param sun       True for the Sun.
     * @return The Julian day in UT for the event, 1s accuracy.
     */
    private double obtainAccurateRiseSetTransit(double riseSetJD, EVENT index, int niter, boolean sun) {
        double step = -1;
        for (int i = 0; i < niter; i++) {
            if (riseSetJD == -1) return riseSetJD; // -1 means no rise/set from that location
            setUTDate(riseSetJD);
            Ephemeris out;
            if (sun) {
                out = doCalc(getSun());
            } else {
                out = doCalc(getMoon());
            }

            double val = out.rise;
            if (index == EVENT.SET) val = out.set;
            if (index == EVENT.TRANSIT) val = out.transit;
            step = Math.abs(riseSetJD - val);
            riseSetJD = val;
            if (step <= 1.0 / SECONDS_PER_DAY) break; // convergency reached
        }
        if (step > 1.0 / SECONDS_PER_DAY)
            return -1; // did not converge => without rise/set/transit in this date
        return riseSetJD;
    }


    // ************


    /**
     * Returns the instant of a given moon phase.
     *
     * @param phase The phase.
     * @return The instant of that phase, accuracy around 1 minute or better.
     */
    public double getMoonPhaseTime(MOONPHASE phase) {
        double accuracy = 10 / (30 * SECONDS_PER_DAY); // 10s / lunar cycle length in s => 10s accuracy
        double refPhase = phase.phase;
        double oldJD = jd_UT, oldMoonAge = moonAge;
        while (true) {
            double age = normalizeRadians((getMoon()[0] - getSun()[0])) / TWO_PI - refPhase;
            if (age < 0) age += 1;
            if (age < accuracy || age > 1 - accuracy) break;
            if (age < 0.5) {
                jd_UT -= age;
            } else {
                jd_UT += 1 - age;
            }
            setUTDate(jd_UT);
        }
        double out = jd_UT;
        setUTDate(oldJD);
        moonAge = oldMoonAge;
        return out;
    }

    /**
     * Returns the dates of the official (geocentric) Spring and Autumn equinoxes.
     *
     * @return Dates of equinoxes, accuracy around 1 minute.
     */
    public double[] getEquinoxes() {
        double jdOld = jd_UT;
        double[] out = new double[2];

        double prec = 1.0 / 86400.0; // Output precision 1s, accuracy around 1 minute
        int year = getDate(jd_UT)[0];

        for (int i = 0; i < 2; i++) {
            int month = 3, day = 18;
            if (i == 1) month = 9;
            double jd = toJulianDay(year, month, day, 0, 0, 0);
            setUTDate(jd);
            double min = -1, minT = -1;
            double stepDays = 0.25, lastDec = -1;
            while (true) {
                double decAbs = Math.abs(doCalc(getSun(), true).declination);
                if (decAbs < min || min == -1) {
                    min = decAbs;
                    minT = jd;
                }
                if (decAbs > lastDec && lastDec >= 0) {
                    if (Math.abs(stepDays) < prec) {
                        out[i] = minT;
                        break;
                    }
                    stepDays = -stepDays / 2;
                }
                lastDec = decAbs;
                jd += stepDays;
                setUTDate(jd);
            }
        }
        setUTDate(jdOld);
        return out;
    }

    /**
     * Returns the dates of the official (geocentric) Summer and Winter solstices.
     *
     * @return Dates of solstices, accuracy around 1 minute.
     */
    public double[] getSolstices() {
        double jdOld = jd_UT;
        double[] out = new double[2];

        double prec = 1.0 / 86400.0; // Output precision 1s, accuracy around 1 minute
        int year = getDate(jd_UT)[0];

        for (int i = 0; i < 2; i++) {
            int month = 6, day = 18;
            if (i == 1) month = 12;
            double jd = toJulianDay(year, month, day, 0, 0, 0);
            setUTDate(jd);
            double max = -1, maxT = -1;
            double stepDays = 0.25, lastDec = -1;
            while (true) {
                double decAbs = Math.abs(doCalc(getSun(), true).declination);
                if (decAbs > max || max == -1) {
                    max = decAbs;
                    maxT = jd;
                }
                if (decAbs < lastDec && lastDec >= 0) {
                    if (Math.abs(stepDays) < prec) {
                        out[i] = maxT;
                        break;
                    }
                    stepDays = -stepDays / 2;
                }
                lastDec = decAbs;
                jd += stepDays;
                setUTDate(jd);
            }
        }
        setUTDate(jdOld);
        return out;
    }

    /**
     * Returns the maximum/minimum elevation time for the Sun or the Moon.
     *
     * @param forSun   True for the Sun, false for the Moon.
     * @param inferior True to get the minimum elevation time.
     * @return The Julian day of the culmination instant, which is
     * only slightly different than the transit.
     */
    public double getCulminationTime(boolean forSun, boolean inferior) {
        double jdOld = jd_UT;
        double jd = forSun ? sun.transit : moon.transit;
        if (inferior) jd += 0.5 * (jd > jdOld ? -1 : 1);
        double startPrecSec = 20.0 / SECONDS_PER_DAY, endPrecSec = 0.25 / SECONDS_PER_DAY;
        setUTDate(jd);
        Ephemeris ephem = forSun ? doCalc(getSun()) : doCalc(getMoon());
        double refAlt = ephem.elevation;

        while (Math.abs(startPrecSec) > endPrecSec) {
            jd += startPrecSec;
            setUTDate(jd);
            ephem = forSun ? doCalc(getSun()) : doCalc(getMoon());
            if (ephem.elevation < refAlt && !inferior) startPrecSec *= -0.25;
            if (ephem.elevation > refAlt && inferior) startPrecSec *= -0.25;
            refAlt = ephem.elevation;
        }

        setUTDate(jdOld);
        return jd;
    }

    /**
     * Returns the instant when the Sun or the Moon reaches a given azimuth.
     *
     * @param forSun  True for the Sun, false for the Moon.
     * @param azimuth The azimuth value to search for.
     * @return The Julian day of the azimuth instant.
     */
    public double getAzimuthTime(boolean forSun, double azimuth) {
        double jdOld = jd_UT;
        double jd = forSun ? sun.transit : moon.transit;
        double startPrecSec = 500.0 / SECONDS_PER_DAY, endPrecSec = 0.25 / SECONDS_PER_DAY;
        setUTDate(jd);
        Ephemeris ephem = forSun ? doCalc(getSun()) : doCalc(getMoon());
        double refDif = Math.abs(ephem.azimuth - azimuth);

        while (Math.abs(startPrecSec) > endPrecSec) {
            jd += startPrecSec;
            setUTDate(jd);
            ephem = forSun ? doCalc(getSun()) : doCalc(getMoon());
            double dif = Math.abs(ephem.azimuth - azimuth);
            if (dif == 0) break;
            if (dif > refDif) startPrecSec *= -0.25;
            refDif = dif;
        }

        setUTDate(jdOld);
        return jd;
    }

    /**
     * Returns the orientation angles of the lunar disk figure. Illumination fraction
     * is returned in the main program. Simplification of the method presented by
     * Eckhardt, D. H., "Theory of the Libration of the Moon", Moon and planets 25, 3
     * (1981), without the physical librations of the Moon. Accuracy around 0.5 deg
     * for each value.
     * Moon and Sun positions must be computed before calling this method.
     *
     * @return Optical libration in longitude, latitude, position angle of
     * axis, bright limb angle, and paralactic angle.
     */
    public double[] getMoonDiskOrientationAngles() {
        double moonLon = moon.eclipticLongitude, moonLat = moon.eclipticLatitude,
                moonRA = moon.rightAscension, moonDEC = moon.declination;
        double sunRA = sun.rightAscension, sunDEC = sun.declination;

        // Obliquity of ecliptic
        double eps = meanObliquity + nutObl;
        // Moon's argument of latitude
        double F = (93.2720993 + 483202.0175273 * t - 0.0034029 * t * t - t * t * t / 3526000.0 + t * t * t * t / 863310000.0) * DEG_TO_RAD;
        // Moon's inclination
        double I = 1.54242 * DEG_TO_RAD;
        // Moon's mean ascending node longitude
        double omega = (125.0445550 - 1934.1361849 * t + 0.0020762 * t * t + t * t * t / 467410.0 - t * t * t * t / 18999000.0) * DEG_TO_RAD;

        double cosI = Math.cos(I), sinI = Math.sin(I);
        double cosMoonLat = Math.cos(moonLat), sinMoonLat = Math.sin(moonLat);
        double cosMoonDec = Math.cos(moonDEC), sinMoonDec = Math.sin(moonDEC);

        // Obtain optical librations lp and bp
        double W = moonLon - omega;
        double sinA = Math.sin(W) * cosMoonLat * cosI - sinMoonLat * sinI;
        double cosA = Math.cos(W) * cosMoonLat;
        double A = Math.atan2(sinA, cosA);
        double lp = normalizeRadians(A - F);
        double sinbp = -Math.sin(W) * cosMoonLat * sinI - sinMoonLat * cosI;
        double bp = Math.asin(sinbp);

        // Obtain position angle of axis p
        double x = sinI * Math.sin(omega);
        double y = sinI * Math.cos(omega) * Math.cos(eps) - cosI * Math.sin(eps);
        double w = Math.atan2(x, y);
        double sinp = Math.hypot(x, y) * Math.cos(moonRA - w) / Math.cos(bp);
        double p = Math.asin(sinp);

        // Compute bright limb angle bl
        double bl = (Math.PI + Math.atan2(Math.cos(sunDEC) * Math.sin(moonRA - sunRA), Math.cos(sunDEC) *
                sinMoonDec * Math.cos(moonRA - sunRA) - Math.sin(sunDEC) * cosMoonDec));

        // Parallactic angle par
        y = Math.sin(lst - moonRA);
        x = Math.tan(obsLat) * cosMoonDec - sinMoonDec * Math.cos(lst - moonRA);
        double par;
        if (x != 0.0) {
            par = Math.atan2(y, x);
        } else {
            par = (y / Math.abs(y)) * PI_OVER_TWO;
        }
        return new double[]{lp, bp, p, bl, par};
    }


    /**
     * Main test program.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        System.out.println("SunMoonCalculator test run");

        try {
            int year = 2020, month = 6, day = 9, h = 18, m = 0, s = 0;
            double obsLon = -4 * DEG_TO_RAD, obsLat = 40 * DEG_TO_RAD;
            int obsAlt = 0; // Altitude in meters

            SunMoonCalculator smc = new SunMoonCalculator(year, month, day, h, m, s, obsLon, obsLat, obsAlt);
            smc.setTwilightMode(TWILIGHT_MODE.TODAY_UT); // Default is TWILIGHT_MODE.CLOSEST
            smc.setTwilightModeTimeZone(3); // Only for TWILIGHT_MODE.TODAY_LT

            smc.calcSunAndMoon();

            String degSymbol = "\u00b0";
            System.out.println("Sun");
            System.out.println(" Az:       " + (float) (smc.sun.azimuth * RAD_TO_DEG) + degSymbol);
            System.out.println(" El:       " + (float) (smc.sun.elevation * RAD_TO_DEG) + degSymbol);
            System.out.println(" Dist:     " + (float) (smc.sun.distance) + " AU");
            System.out.println(" RA:       " + (float) (smc.sun.rightAscension * RAD_TO_DEG) + degSymbol);
            System.out.println(" DEC:      " + (float) (smc.sun.declination * RAD_TO_DEG) + degSymbol);
            System.out.println(" Ill:      " + (float) (smc.sun.illuminationPhase) + "%");
            System.out.println(" ang.R:    " + (float) (smc.sun.angularRadius * RAD_TO_DEG) + degSymbol);
            System.out.println(" Rise:     " + SunMoonCalculator.getDateAsString(smc.sun.rise));
            System.out.println(" Set:      " + SunMoonCalculator.getDateAsString(smc.sun.set));
            System.out.println(" Transit:  " + SunMoonCalculator.getDateAsString(smc.sun.transit) + " (elev. " + (float) (smc.sun.transitElevation * RAD_TO_DEG) + degSymbol + ")");

            System.out.println("Moon");
            System.out.println(" Az:       " + (float) (smc.moon.azimuth * RAD_TO_DEG) + degSymbol);
            System.out.println(" El:       " + (float) (smc.moon.elevation * RAD_TO_DEG) + degSymbol);
            System.out.println(" Dist:     " + (float) (smc.moon.distance * AU) + " km");
            System.out.println(" RA:       " + (float) (smc.moon.rightAscension * RAD_TO_DEG) + degSymbol);
            System.out.println(" DEC:      " + (float) (smc.moon.declination * RAD_TO_DEG) + degSymbol);
            System.out.println(" Ill:      " + (float) (smc.moon.illuminationPhase) + "%");
            System.out.println(" ang.R:    " + (float) (smc.moon.angularRadius * RAD_TO_DEG) + degSymbol);
            System.out.println(" Age:      " + (float) (smc.moonAge) + " days");
            System.out.println(" Rise:     " + SunMoonCalculator.getDateAsString(smc.moon.rise));
            System.out.println(" Set:      " + SunMoonCalculator.getDateAsString(smc.moon.set));
            System.out.println(" Transit:  " + SunMoonCalculator.getDateAsString(smc.moon.transit) + " (elev. " + (float) (smc.moon.transitElevation * RAD_TO_DEG) + degSymbol + ")");

            smc.setTwilight(TWILIGHT.ASTRONOMICAL);
            smc.calcSunAndMoon();

            System.out.println();
            System.out.println("Astronomical twilights:");
            System.out.println("Sun");
            System.out.println(" Rise:     " + SunMoonCalculator.getDateAsString(smc.sun.rise));
            System.out.println(" Set:      " + SunMoonCalculator.getDateAsString(smc.sun.set));
            System.out.println("Moon");
            System.out.println(" Rise:     " + SunMoonCalculator.getDateAsString(smc.moon.rise));
            System.out.println(" Set:      " + SunMoonCalculator.getDateAsString(smc.moon.set));

            System.out.println();
            System.out.println("Closest Moon phases:");
            for (int i = 0; i < MOONPHASE.values().length; i++) {
                MOONPHASE mp = MOONPHASE.values()[i];
                System.out.println(" " + mp.phaseName + "  " + SunMoonCalculator.getDateAsString(smc.getMoonPhaseTime(mp)));
            }

            double[] equinox = smc.getEquinoxes();
            double[] solstices = smc.getSolstices();
            System.out.println();
            System.out.println("Equinoxes and solstices:");
            System.out.println(" Spring equinox:    " + SunMoonCalculator.getDateAsString(equinox[0]));
            System.out.println(" Autumn equinox:    " + SunMoonCalculator.getDateAsString(equinox[1]));
            System.out.println(" Summer solstice:   " + SunMoonCalculator.getDateAsString(solstices[0]));
            System.out.println(" Winter solstice:   " + SunMoonCalculator.getDateAsString(solstices[1]));

            // Expected accuracy over 1800 - 2200:
            // - Sun: 0.001 deg in RA/DEC, 0.003 deg or 10 arcsec in Az/El.
            //        <1s in rise/set/transit times. 1 min in Equinoxes/Solstices
            //        Can be used over 6 millenia around year 2000 with a similar accuracy.
            // - Mon: 0.005 deg or better. 30 km in distance
            //        2s or better in rise/set/transit times. 1 minute in lunar phases.
            //        Can be used between 1000 A.D. - 3000 A.D. with an accuracy around 0.1 deg.
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}