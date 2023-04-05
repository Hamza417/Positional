package app.simple.positional.math

import android.graphics.*

object DrawMoonBitmap {
    /**
     * Draw a circle, then draw a half circle on top of it, then draw a quarter circle on top of that,
     * then flip the image if the phase is less or equal to 0.5 (0 when we use scale -180 to 180)
     *
     */
    fun getLunarPhaseBitmap(phaseValue: Double, fraction: Double, smc: SunMoonCalculator, lat: Double, hemi: Boolean, size: Int = 500): Bitmap {

        /** INITIAL SET OF VARIABLES USED */
        /** WITH SHRED ZONE, SOME VARIABLES NEEDS TO BE CORRECTED, DO NOT DIVIDE FRACTION & PHASE HAS -180 - 0 - +180 SCALE */
        // SHRED ZONE
        //val set1 = MoonPosition.compute().now().at(48.0,20.0).execute()
        //val set2 = MoonIllumination.compute().now().execute()
        //var percentIlluminated = set2.fraction // calculate the percentage of the moon that is illuminated
        //val phaseValue = set2.phase // phase from -180 (new moon - waxing) to 180 (new moon - waning)
        //val axis = 0f // TODO: Needs to be computed
        //val brightLimb = 0f // TODO: Needs to be computed
        //val parallactic = set1.parallacticAngle.toFloat() // TODO: Differs from SunMoonCalculator a lot, maybe because SMC is also using Sun Position?

        // SMC ---
        var percentIlluminated = fraction // 100
        val p1 = smc.moonDiskOrientationAngles
        val axis = Math.toDegrees(p1[2]).toFloat()
        val brightLimb = Math.toDegrees(p1[3]).toFloat()
        val parallactic = Math.toDegrees(p1[4]).toFloat()

        /*
        Log.d(TAG, "percentIlluminated: $percentIlluminated")
        Log.d(TAG, "phaseValue: $phaseValue")
        Log.d(TAG, "axis: $axis")
        Log.d(TAG, "brightLimb: $brightLimb")
        Log.d(TAG, "parallactic: $parallactic")
         */

        /** COMPUTE INITIAL ROTATION - DON'T CHANGE THIS */
        var initialRotation = 0f // INITIAL ROTATION
        if (phaseValue <= 0.5) initialRotation -= 180f // ROTATE BY 180° when moon is waxing (-180, 0) because our draw method works only in one direction

        /** COMPUTE BRIGHT LIMB ROTATION - THIS PART IS CRITICAL */
        var brightLimbRotate = brightLimb - 90f
        if (brightLimb > 180f) {
            brightLimbRotate = brightLimb - 270f
        }
        brightLimbRotate -= axis
        val lastRotate = parallactic - axis

        /** FINAL ROTATION
         * IF LOCATION IS SET, CALCULATE ROTATION
         * IF LOCATION IS NOT SET, SET ROTATION TO 0 (ZERO) AND ROTATE BY 180° (FLIP) WHEN USER SELECTS SOUTHERN HEMISPHERE
         * */
        val finalRotation = if (lat != 0.0) (initialRotation + brightLimbRotate + lastRotate)
        else if (!hemi) 180f
        else 0f

        /** SLIGHTLY EDIT ILLUMINATION SO RESULTED BITMAP WONT LOOK BAD */
        when (percentIlluminated) {
            in 0.01..0.05 -> percentIlluminated = 0.05
            in 0.06..0.15 -> percentIlluminated += 0.05
            in 0.16..0.25 -> percentIlluminated += 0.04
            in 0.26..0.35 -> percentIlluminated += 0.03
            in 0.36..0.45 -> percentIlluminated += 0.02
            in 0.55..0.65 -> percentIlluminated -= 0.05
            in 0.66..0.75 -> percentIlluminated -= 0.04
            in 0.76..0.85 -> percentIlluminated -= 0.03
            in 0.86..0.95 -> percentIlluminated -= 0.02
        }

        /** BITMAP COLORS */
        // DARK COLOR FOR MOON BACKGROUND (MOON NOT VISIBLE)
        val shadowPaint = Paint()
        // shadowPaint.setShadowLayer(10f, 0f, 0f, Color.rgb(52, 52, 52))
        shadowPaint.color = Color.rgb(52, 52, 52)
        shadowPaint.isAntiAlias = true

        // WHITE COLOR FOR BRIGHT LIMB
        val brightPaint = Paint()
        brightPaint.color = Color.WHITE
        brightPaint.isAntiAlias = true

        /** SET CANVAS */
        val radius = size / 2f // calculate the radius of the moon = CENTER POINT
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        /** DRAW MOON BACKGROUND */
        // draw dark part / background. Moons shadow part needs to be in the background due to bad image look on watch faces
        canvas.drawCircle(radius, radius, radius, shadowPaint)

        /** ROTATE CANVAS */
        canvas.save()
        canvas.rotate(finalRotation, radius, radius)

        /** DRAW MOON LOOK FROM 0 --> 50% ILLUMINATION */
        if (percentIlluminated > 0.01 && percentIlluminated <= 0.5) {
            // if the moon is in the first half of its cycle
            // draw a white semicircle to represent the illuminated portion of the moon

            canvas.drawArc(
                    (size / 2) - radius,
                    (size / 2) - radius,
                    (size / 2) + radius,
                    (size / 2) + radius,
                    90f,
                    180f,
                    true,
                    brightPaint
            )
            // draw a white oval to represent the illuminated portion of the moon
            val ovalBounds = RectF()
            val ovalLeft = (size / 2 - radius + 2 * radius * percentIlluminated).toFloat()
            val ovalRight = (size / 2 + radius - 2 * radius * percentIlluminated).toFloat()
            ovalBounds[ovalLeft, (size / 2 - radius), ovalRight] = (size / 2 + radius)
            canvas.drawOval(ovalBounds, shadowPaint)

        }
        /** DRAW MOON LOOK FROM 50 --> 100% ILLUMINATION */
        else if (percentIlluminated > 0.5) {
            // if the moon is in the second half of its cycle
            // draw a white semicircle to represent the illuminated portion of the moon
            canvas.drawArc(
                    (size / 2) - radius,
                    (size / 2) - radius,
                    (size / 2) + radius,
                    (size / 2) + radius,
                    90f,
                    180f,
                    true,
                    brightPaint
            )
            // draw a white oval to represent the illuminated portion of the moon
            val ovalBounds = RectF()
            val ovalLeft = (size / 2 + radius - 2 * radius * percentIlluminated).toFloat()
            val ovalRight = (size / 2 - radius + 2 * radius * percentIlluminated).toFloat()
            ovalBounds[ovalLeft, (size / 2 - radius), ovalRight] = (size / 2 + radius)
            canvas.drawOval(ovalBounds, brightPaint)

        }
        /** RESTORE CANVAS AFTER ROTATION */
        canvas.restore()
        canvas.save()

        /** RETURN MOON BITMAP */
        return Bitmap.createScaledBitmap(bitmap, size, size, true)
    }
}