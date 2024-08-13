package com.fr0z863xf.fudisk.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.fr0z863xf.fudisk.R
import com.fr0z863xf.fudisk.Utils.SettingsManager

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sM = SettingsManager.getInstance(null)
        view.findViewById<ComposeView>(R.id.settings_compose_view).setContent { SettingsView(this,sM) }
    }
}