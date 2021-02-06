package app.simple.positional.sparkline.data

import android.graphics.PointF

/**
 * Data object that stores points for a curve.
 * (Start and end points and control points for them)
 *
 *  p1, p2 - Start & end points
 *  cp1, cp2 - Start control & end control points
 */

data class CurvePoints(
        val p1: PointF,
        val cp1: PointF,
        val cp2: PointF,
        val p2: PointF
)
