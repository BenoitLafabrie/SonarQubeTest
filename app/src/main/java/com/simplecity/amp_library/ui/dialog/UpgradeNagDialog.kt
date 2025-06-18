package com.simplecity.amp_library.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.simplecity.amp_library.R
import com.simplecity.amp_library.utils.SettingsManager
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class UpgradeNagDialog : DialogFragment() {

    @Inject lateinit var settingsManager: SettingsManager

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager.setNagMessageRead()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialDialog.Builder(context!!)
            .title(context!!.resources.getString(R.string.get_pro_title))
            .content(context!!.resources.getString(R.string.get_pro_message))
            .positiveText(R.string.btn_upgrade)
            .onPositive { _: MaterialDialog, which: MaterialDialog.ButtonCallback ->
                // Show IAP or open Play Store
                val playStoreIntent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context!!.packageName}")
                )
                startActivity(playStoreIntent)

            }
            .negativeText(R.string.get_pro_button_no)

        return builder.build()
    }
}