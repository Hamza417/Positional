package app.simple.positional.sparkline.extensions

import android.graphics.Path
import android.graphics.PointF
import app.simple.positional.sparkline.data.CurvePoints

fun Path.cubicTo(controlPoint1: PointF, controlPoint2: PointF, point2: PointF) {
    this.cubicTo(
            controlPoint1.x,
            controlPoint1.y,
            controlPoint2.x,
            controlPoint2.y,
            point2.x,
            point2.y
    )
}

fun Path.cubicTo(curvePoints: CurvePoints) {
    this.cubicTo(
            curvePoints.cp1.x,
            curvePoints.cp1.y,
            curvePoints.cp2.x,
            curvePoints.cp2.y,
            curvePoints.p2.x,
            curvePoints.p2.y
    )
}
