package app.simple.positional.constants

import android.graphics.Color.parseColor
import app.simple.positional.R

object LauncherBackground {
    val vectorBackground = intArrayOf(
            R.drawable.launcher_day_01,
            R.drawable.launcher_day_02,
            R.drawable.launcher_day_03,
            R.drawable.launcher_day_04,
            R.drawable.launcher_day_05,
            R.drawable.launcher_day_06,
            R.drawable.launcher_day_07,
            R.drawable.launcher_day_08,
            R.drawable.launcher_day_09,
            R.drawable.launcher_day_10,
            R.drawable.launcher_day_11,
            R.drawable.launcher_day_12,
            R.drawable.launcher_day_13,
            R.drawable.launcher_day_14,
            R.drawable.launcher_day_15,
            R.drawable.launcher_day_16,
            R.drawable.launcher_day_17,
            R.drawable.launcher_day_18,
            R.drawable.launcher_day_19,
            R.drawable.launcher_day_20,
            R.drawable.launcher_day_21,
            R.drawable.launcher_day_22,
            R.drawable.launcher_day_23,
            R.drawable.launcher_day_24,
            R.drawable.launcher_day_25,
            R.drawable.launcher_day_26,
            R.drawable.launcher_day_27,
            R.drawable.launcher_day_28,
            R.drawable.launcher_day_29,
            R.drawable.launcher_day_30,
            R.drawable.launcher_day_31,
            R.drawable.launcher_day_32,
            R.drawable.launcher_day_33,
            R.drawable.launcher_day_34,
            R.drawable.launcher_day_35,
            R.drawable.launcher_day_36,
            R.drawable.launcher_day_37,
            R.drawable.launcher_day_38,
            R.drawable.launcher_day_39,
            R.drawable.launcher_day_40,
            R.drawable.launcher_day_41,
            R.drawable.launcher_day_42,
            R.drawable.launcher_day_43,
            R.drawable.launcher_day_44,
            R.drawable.launcher_day_45,
            R.drawable.launcher_day_46,
            R.drawable.launcher_day_47,
            R.drawable.launcher_day_48,
            R.drawable.launcher_day_49,
            R.drawable.launcher_day_50,
            R.drawable.launcher_day_51,
            R.drawable.launcher_day_52,
            R.drawable.launcher_day_53,
            R.drawable.launcher_day_54,
            R.drawable.launcher_day_55,
            R.drawable.launcher_day_56,
            R.drawable.launcher_day_57,
            R.drawable.launcher_day_58,
            R.drawable.launcher_day_59,
            R.drawable.launcher_day_60,
            R.drawable.launcher_day_61,
            R.drawable.launcher_day_62,
            R.drawable.launcher_day_63,
            R.drawable.launcher_day_64,
            R.drawable.launcher_day_65,
            R.drawable.launcher_day_66,
            R.drawable.launcher_day_67,
            R.drawable.launcher_day_68,
            R.drawable.launcher_day_69,
            R.drawable.launcher_day_70,
            R.drawable.launcher_day_71,
            R.drawable.launcher_day_72,
            R.drawable.launcher_day_73,
            R.drawable.launcher_day_74,
            R.drawable.launcher_day_75,
    )

    val vectorBackgroundNight = intArrayOf(
            R.drawable.launcher_night_01,
            R.drawable.launcher_night_02,
            R.drawable.launcher_night_03,
            R.drawable.launcher_night_04,
            R.drawable.launcher_night_05,
            R.drawable.launcher_night_06,
            R.drawable.launcher_night_07,
            R.drawable.launcher_night_08,
            R.drawable.launcher_night_09,
            R.drawable.launcher_night_10,
            R.drawable.launcher_night_11,
            R.drawable.launcher_night_12,
            R.drawable.launcher_night_13,
            R.drawable.launcher_night_14,
            R.drawable.launcher_night_15,
            R.drawable.launcher_night_16
    )

    /**
     * The gradient color pattern is linear and offset is +Y to -Y and
     * it is advisable to use the darker shade first and light shade after
     * to achieve a nice looking gradient tint
     */
    val vectorColors: Array<IntArray> = arrayOf(
            intArrayOf(0xFFF6E58D.toInt(), 0xFFE056FD.toInt()), // 01
            intArrayOf(0xFFFFD71D.toInt(), 0xFF804700.toInt()), // 02
            intArrayOf(0xFFAA8659.toInt(), 0xFFAA8659.toInt()), // 03
            intArrayOf(0xFF9D56A0.toInt(), 0xFF246887.toInt()), // 04
            intArrayOf(0xFFDE542A.toInt(), 0xFFBA2D0A.toInt()), // 05
            intArrayOf(0xFF52618C.toInt(), 0xFF6B8EA9.toInt()), // 06
            intArrayOf(0xFF434E94.toInt(), 0xFF081146.toInt()), // 07
            intArrayOf(0xFFDE7E42.toInt(), 0xFFBF5047.toInt()), // 08
            intArrayOf(0xFFC75124.toInt(), 0xFFFBB58E.toInt()), // 09
            intArrayOf(0xFF3CB1C3.toInt(), 0xFFB5B1A5.toInt()), // 10
            intArrayOf(0xFF2D6E76.toInt(), 0xFF589180.toInt()), // 11
            intArrayOf(0xFFC2602A.toInt(), 0xFFE89144.toInt()), // 12
            intArrayOf(0xFFFC6F55.toInt(), 0xFFC3687B.toInt()), // 13
            intArrayOf(0xFF69525E.toInt(), 0xFF432131.toInt()), // 14
            intArrayOf(0xFF441F00.toInt(), 0xFFBE804C.toInt()), // 15
            intArrayOf(0xFFB58061.toInt(), 0xFFD08F4D.toInt()), // 16
            intArrayOf(0xFFFF8A9F.toInt(), 0xFFB6577C.toInt()), // 17
            intArrayOf(0xFFFFE8C7.toInt(), 0xFFF5C579.toInt()), // 18
            intArrayOf(0xFF91AEAD.toInt(), 0xFF2F4A5D.toInt()), // 19
            intArrayOf(0xFF008D7D.toInt(), 0xFF1E0B53.toInt()), // 20
            intArrayOf(0xFF872133.toInt(), 0xFFBB4657.toInt()), // 21
            intArrayOf(0xFF44408D.toInt(), 0xFF1C7EB6.toInt()), // 22
            intArrayOf(0xFF047A62.toInt(), 0xFF9DF3C4.toInt()), // 23
            intArrayOf(0xFFEBAC59.toInt(), 0xFF9C314C.toInt()), // 24
            intArrayOf(0xFF00748E.toInt(), 0xFF004B5B.toInt()), // 25
            intArrayOf(0xFF20804C.toInt(), 0xFF6BE297.toInt()), // 26
            intArrayOf(0xFF48543E.toInt(), 0xFFBEA46F.toInt()), // 27
            intArrayOf(0xFF7E2845.toInt(), 0xFFE15B64.toInt()), // 28
            intArrayOf(0xFF344B70.toInt(), 0xFFBFCADB.toInt()), // 29
            intArrayOf(0xFF7B8C62.toInt(), 0xFF4B594B.toInt()), // 30
            intArrayOf(0xFF6F493C.toInt(), 0xFFF68815.toInt()), // 31
            intArrayOf(0xFF1C629B.toInt(), 0xFF1D71B5.toInt()), // 32
            intArrayOf(0xFFEA9633.toInt(), 0xFFFECE35.toInt()), // 33
            intArrayOf(0xFFA14118.toInt(), 0xFFDB6532.toInt()), // 34
            intArrayOf(0xFF1B72A7.toInt(), 0xFF1BA0D1.toInt()), // 35
            intArrayOf(0xFFA12C34.toInt(), 0xFFF87136.toInt()), // 36
            intArrayOf(0xFFDE4339.toInt(), 0xFFFF9B43.toInt()), // 37
            intArrayOf(0xFF25B782.toInt(), 0xFF006F7A.toInt()), // 38
            intArrayOf(0xFFED6959.toInt(), 0xFFF6AA4D.toInt()), // 39
            intArrayOf(0xFF2D3F43.toInt(), 0xFF5C6B4D.toInt()), // 40
            intArrayOf(0xFF9C3F88.toInt(), 0xFFBD2E94.toInt()), // 41
            intArrayOf(0xFF129483.toInt(), 0xFF3EAF98.toInt()), // 42
            intArrayOf(0xFFFF9266.toInt(), 0xFFFFDC78.toInt()), // 43
            intArrayOf(0xFF3D6199.toInt(), 0xFFFA7169.toInt()), // 44
            intArrayOf(0xFF28ADD1.toInt(), 0xFFFF9F29.toInt()), // 45
            intArrayOf(0xFF0042AE.toInt(), 0xFF5BA9FD.toInt()), // 46
            intArrayOf(0xFF8B4428.toInt(), 0xFFFA9C00.toInt()), // 47
            intArrayOf(0xFF7E4C3A.toInt(), 0xFFE7833D.toInt()), // 48
            intArrayOf(0xFF282A57.toInt(), 0xFFDE5654.toInt()), // 49
            intArrayOf(0xFF5E5A39.toInt(), 0xFF828439.toInt()), // 50
            intArrayOf(0xFFBE5C35.toInt(), 0xFFEABC83.toInt()), // 51
            intArrayOf(0xFF328A62.toInt(), 0xFF71BB7F.toInt()), // 52
            intArrayOf(0xFF008CA2.toInt(), 0xFF00C8CA.toInt()), // 53
            intArrayOf(0xFF0085BB.toInt(), 0xFF66D9DB.toInt()), // 54
            intArrayOf(0xFF5A6DA3.toInt(), 0xFF7382C3.toInt()), // 55
            intArrayOf(0xFF00919B.toInt(), 0xFF00BDC6.toInt()), // 56
            intArrayOf(0xFFFF7979.toInt(), 0xFFF6E58D.toInt()), // 57
            intArrayOf(0xFFC6AA9B.toInt(), 0xFFF4D7BE.toInt()), // 58
            intArrayOf(0xFF446977.toInt(), 0xFF5A7E91.toInt()), // 59
            intArrayOf(0xFFEE8E50.toInt(), 0xFFFCC647.toInt()), // 60
            intArrayOf(0xFF355872.toInt(), 0xFF3B71A3.toInt()), // 61
            intArrayOf(0xFF918047.toInt(), 0xFFC4A847.toInt()), // 62
            intArrayOf(0xFF008F9F.toInt(), 0xFF47FAB0.toInt()), // 63
            intArrayOf(0xFF67381B.toInt(), 0xFFB29983.toInt()), // 64
            intArrayOf(0xFFCC790A.toInt(), 0xFFFBB03B.toInt()), // 65
            intArrayOf(0xFFE57123.toInt(), 0xFFF4944D.toInt()), // 66
            intArrayOf(0xFF0F417E.toInt(), 0xFF4377AA.toInt()), // 67
            intArrayOf(0xFF02162F.toInt(), 0xFF0F417E.toInt()), // 68
            intArrayOf(0xFFCA8A64.toInt(), 0xFFFFE18E.toInt()), // 69
            intArrayOf(0xFFCC4622.toInt(), 0xFFFE9448.toInt()), // 70
            intArrayOf(0xFFE66716.toInt(), 0xFFFA8E10.toInt()), // 71
            intArrayOf(0xFFE24949.toInt(), 0xFFB52C2C.toInt()), // 72
            intArrayOf(0xFF5F8E55.toInt(), 0xFFB5B784.toInt()), // 73
            intArrayOf(0xFFF16000.toInt(), 0xFFF0C549.toInt()), // 74
            intArrayOf(0xFF696E12.toInt(), 0xFFB79521.toInt()), // 75
    )

    val vectorNightColors: Array<IntArray> = arrayOf(
            intArrayOf(0xFFFFA32A.toInt(), 0xFFCB555B.toInt()), // 01
            intArrayOf(0xFF3664A6.toInt(), 0xFF56ABBB.toInt()), // 02
            intArrayOf(0xFF4C355E.toInt(), 0xFF311B3F.toInt()), // 03
            intArrayOf(0xFF527AAA.toInt(), 0xFF20344A.toInt()), // 04
            intArrayOf(0xFF3392B1.toInt(), 0xFF1B2A65.toInt()), // 05
            intArrayOf(0xFF212B4F.toInt(), 0xFF131E3A.toInt()), // 06
            intArrayOf(0xFF2D4383.toInt(), 0xFFAE4AA0.toInt()), // 07
            intArrayOf(0xFF9FB0CC.toInt(), 0xFF121C33.toInt()), // 08
            intArrayOf(0xFF030B26.toInt(), 0xFF463959.toInt()), // 09
            intArrayOf(0xFF608396.toInt(), 0xFFF88063.toInt()), // 10
            intArrayOf(0xFF554686.toInt(), 0xFF6E51C9.toInt()), // 11
            intArrayOf(0xFFF0A87F.toInt(), 0xFF415262.toInt()), // 12
            intArrayOf(0xFFFFEC84.toInt(), 0xFFFF8B88.toInt()), // 13
            intArrayOf(0xFF225664.toInt(), 0xFF65CBBF.toInt()), // 14
            intArrayOf(0xFF35556F.toInt(), 0xFF5492C2.toInt()), // 15
            intArrayOf(0xFF0096FF.toInt(), 0xFF1A47A2.toInt()), // 16
    )
}
