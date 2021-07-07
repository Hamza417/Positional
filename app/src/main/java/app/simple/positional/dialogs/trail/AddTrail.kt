package app.simple.positional.dialogs.trail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import app.simple.positional.R
import app.simple.positional.database.instances.TrailDatabase
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.model.Trails
import app.simple.positional.preferences.TrailPreferences
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTrail : CustomDialogFragment() {

    private lateinit var textInputLayout: TextInputLayout
    private lateinit var textInputEditText: TextInputEditText
    private lateinit var save: DynamicRippleButton

    private val list = arrayListOf<Trails>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_trail_name, container, false)

        textInputEditText = view.findViewById(R.id.trail_name_edit_text)
        textInputLayout = view.findViewById(R.id.trail_name_edit_text_layout)
        save = view.findViewById(R.id.save)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = Room.databaseBuilder(requireContext(), TrailDatabase::class.java, "%%_trails.db").build()
            list.addAll(db.trailDao()!!.getAllTrails())
            db.close()
        }

        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                for (trail in list) {
                    if (trail.trailName == s.toString()) {
                        textInputLayout.isErrorEnabled = true
                        save.isClickable = false
                    } else {
                        textInputLayout.isErrorEnabled = false
                        save.isClickable = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        save.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val trails = Trails(
                        System.currentTimeMillis(),
                        textInputEditText.text.toString()
                )

                val db = Room.databaseBuilder(requireContext(), TrailDatabase::class.java, "%%_trails.db").build()
                db.trailDao()?.insertTrail(trails)
                db.close()

                TrailPreferences.setLastTrailName(textInputEditText.text.toString())

                withContext(Dispatchers.Main) {
                    dismiss()
                }
            }
        }
    }

    companion object {
        fun newInstance(): AddTrail {
            val args = Bundle()
            val fragment = AddTrail()
            fragment.arguments = args
            return fragment
        }
    }
}
