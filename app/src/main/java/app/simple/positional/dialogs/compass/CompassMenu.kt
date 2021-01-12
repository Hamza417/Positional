package app.simple.positional.dialogs.compass

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.dialogs.settings.HtmlViewer
import app.simple.positional.preference.CompassPreference.isFlowerBloom
import app.simple.positional.preference.CompassPreference.setDirectionCode
import app.simple.positional.preference.CompassPreference.setFlowerBloom
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialog
import java.lang.ref.WeakReference

class CompassMenu(private val weakReference: WeakReference<Compass>) : CustomBottomSheetDialog() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    private lateinit var toggleFlower: SwitchCompat
    private lateinit var toggleCode: SwitchCompat
    private lateinit var bloomText: TextView
    private lateinit var bloomSkinsText: TextView
    private lateinit var blooms: LinearLayout
    private lateinit var speed: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_compass_menu, container, false)

        toggleFlower = view.findViewById(R.id.toggle_flower)
        toggleCode = view.findViewById(R.id.toggle_code)
        bloomText = view.findViewById(R.id.compass_bloom_text)
        bloomSkinsText = view.findViewById(R.id.compass_bloom_skins_text)
        blooms = view.findViewById(R.id.compass_bloom_theme)
        speed = view.findViewById(R.id.compass_speed)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.FLAVOR != "lite") {
            toggleFlower.isChecked = isFlowerBloom()
        } else {
            bloomText.setTextColor(Color.GRAY)
            bloomSkinsText.setTextColor(Color.GRAY)
        }

        toggleFlower.setOnCheckedChangeListener { _, isChecked ->
            if (BuildConfig.FLAVOR != "lite") {
                weakReference.get()?.setFlower(isChecked)
                setFlowerBloom(isChecked)
            } else {
                HtmlViewer().newInstance("Buy").show(childFragmentManager, "buy")
                toggleFlower.isChecked = false
            }
        }

        blooms.setOnClickListener {
            if (BuildConfig.FLAVOR != "lite") {
                val compassBloom = CompassBloom(weakReference)
                compassBloom.show(parentFragmentManager, "null")
            } else {
                HtmlViewer().newInstance("Buy").show(childFragmentManager, "buy")
                toggleFlower.isChecked = false
            }
        }

        toggleCode.setOnCheckedChangeListener { _, isChecked ->
            weakReference.get()?.showDirectionCode = isChecked
            setDirectionCode(isChecked)
        }

        speed.setOnClickListener {
            val compassSpeed = WeakReference(CompassSpeed(weakReference))
            compassSpeed.get()?.show(parentFragmentManager, "null")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        weakReference.clear()
    }
}
