package com.example.saloris

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallbackProvider
import dagger.hilt.android.AndroidEntryPoint

/**
 * This Activity serves a handful of functions:
 * - to host a [NavHostFragment]
 * - to capture KeyEvents
 * - to support Ambient Mode, because [AmbientCallbackProvider] must be an `Activity`.
 *
 * [MainViewModel] is used to coordinate between this Activity and the [ExerciseFragment], which
 * contains UI during an active exercise.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main), AmbientCallbackProvider {

    private val viewModel: MainViewModel by viewModels()

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_STEM_1,
            KeyEvent.KEYCODE_STEM_2,
            KeyEvent.KEYCODE_STEM_3,
            KeyEvent.KEYCODE_STEM_PRIMARY -> {
                viewModel.sendKeyPress()
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = AmbientModeCallback()

    inner class AmbientModeCallback : AmbientModeSupport.AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle) {
            viewModel.sendAmbientEvent(AmbientEvent.Enter(ambientDetails))
        }

        override fun onExitAmbient() {
            viewModel.sendAmbientEvent(AmbientEvent.Exit)
        }

        override fun onUpdateAmbient() {
            viewModel.sendAmbientEvent(AmbientEvent.Update)
        }
    }
}