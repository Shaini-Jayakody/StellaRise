package com.example.stellarise.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.Toast
import com.example.stellarise.R
import com.example.stellarise.data.DataManager

class SettingsFragment : Fragment() {

    private lateinit var dataManager: DataManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            dataManager = DataManager(requireContext())
            setupButtons(view)
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "Error in onViewCreated: ${e.message}", e)
        }
    }

    private fun setupButtons(view: View) {
        // Theme button
        view.findViewById<Button>(R.id.btnTheme).setOnClickListener {
            showThemeDialog()
        }
        
        // Notifications button
        val btnNotifications = view.findViewById<Button>(R.id.btnNotifications)
        updateNotificationButtonState(btnNotifications)
        btnNotifications.setOnClickListener {
            showNotificationSettings()
        }
        
        // Data & Privacy button
        view.findViewById<Button>(R.id.btnDataPrivacy).setOnClickListener {
            showDataPrivacyDialog()
        }
        
        // Widget button (replaced Export Data)
        val btnWidget = view.findViewById<Button>(R.id.btnExportData)
        updateWidgetButtonState(btnWidget)
        btnWidget.setOnClickListener {
            showWidgetSettings()
        }
        
        // Clear Data button
        view.findViewById<Button>(R.id.btnClearData).setOnClickListener {
            showClearDataDialog()
        }
        
        // About button
        view.findViewById<Button>(R.id.btnAbout).setOnClickListener {
            showAboutDialog()
        }
    }


    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Theme")
            .setItems(themes) { _, _ ->
                // Save preference & apply theme here
            }
            .show()
    }

    private fun updateNotificationButtonState(button: Button) {
        val isNotificationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // For older Android versions, notifications are enabled by default
        }
        
        if (isNotificationEnabled) {
            button.text = "Disable"
            button.isEnabled = true
            button.alpha = 1.0f
        } else {
            button.text = "Allow"
            button.isEnabled = true
            button.alpha = 1.0f
        }
    }

    private fun showNotificationSettings() {
        val isNotificationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // For older Android versions, notifications are enabled by default
        }
        
        if (isNotificationEnabled) {
            // Notifications are enabled, show disable option
            showDisableNotificationDialog()
        } else {
            // Notifications are disabled, show enable option
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) -> {
                        // Show explanation and request permission
                        showNotificationPermissionExplanationDialog()
                    }
                    else -> {
                        // Request permission directly
                        requestNotificationPermission()
                    }
                }
            } else {
                // For older Android versions, notifications are enabled by default
                showNotificationPermissionGrantedDialog()
            }
        }
    }

    private fun showNotificationPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Allow Notifications")
            .setMessage("StellaRise needs notification permission to send you reminders for:\n\n• Hydration reminders\n• Habit completion notifications\n• Daily motivation messages\n\nThis helps you stay on track with your wellness goals!")
            .setPositiveButton("Allow") { _, _ ->
                requestNotificationPermission()
            }
            .setNegativeButton("Not Now", null)
            .show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showDisableNotificationDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Disable Notifications")
            .setMessage("Notifications are currently enabled. You'll stop receiving:\n\n• Hydration reminders\n• Habit completion notifications\n• Daily motivation messages\n\nTo disable notifications, you'll need to go to your device's Settings > Apps > StellaRise > Notifications and turn them off.")
            .setPositiveButton("Open Settings") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openNotificationSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = android.net.Uri.parse("package:${requireContext().packageName}")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Please go to Settings > Apps > StellaRise > Notifications", Toast.LENGTH_LONG).show()
        }
    }

    private fun showNotificationPermissionGrantedDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Notification Settings")
            .setMessage("✅ Notifications are enabled!\n\nYou'll receive:\n• Hydration reminders\n• Habit completion notifications\n• Daily motivation messages\n\nTo manage notification settings, go to your device's Settings > Apps > StellaRise > Notifications")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(requireContext(), "Notification permission granted! 🌟", Toast.LENGTH_SHORT).show()
                    showNotificationPermissionGrantedDialog()
                    // Update button state
                    view?.findViewById<Button>(R.id.btnNotifications)?.let { button ->
                        updateNotificationButtonState(button)
                    }
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "Notification permission denied. You can enable it later in Settings.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    private fun showDataPrivacyDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Data & Privacy")
            .setMessage("Your data is stored locally on your device and is not shared with third parties.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateWidgetButtonState(button: Button) {
        // For now, we'll assume widgets can be added/removed
        // In a real implementation, you'd check if widgets are actually installed
        val hasWidgets = checkIfWidgetsInstalled()
        
        if (hasWidgets) {
            button.text = "Remove"
        } else {
            button.text = "Add"
        }
    }

    private fun checkIfWidgetsInstalled(): Boolean {
        // This is a simplified check - in reality, you'd need to track widget instances
        // For now, we'll use a simple approach based on user preference
        val prefs = requireContext().getSharedPreferences("stellarise_data", android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean("widget_installed", false)
    }

    private fun setWidgetInstalled(installed: Boolean) {
        val prefs = requireContext().getSharedPreferences("stellarise_data", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("widget_installed", installed).apply()
    }

    private fun showWidgetSettings() {
        val hasWidgets = checkIfWidgetsInstalled()
        
        if (hasWidgets) {
            showRemoveWidgetDialog()
        } else {
            showAddWidgetDialog()
        }
    }

    private fun showAddWidgetDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Add Widget to Home Screen")
            .setMessage("To add the StellaRise widget to your home screen:\n\n1. Long press on your home screen\n2. Tap 'Widgets' or 'Add Widget'\n3. Find 'StellaRise' in the widget list\n4. Drag the widget to your desired location\n\nThe widget will show your daily progress and allow quick access to your habits!")
            .setPositiveButton("Open Widget Settings") { _, _ ->
                openWidgetSettings()
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun showRemoveWidgetDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Remove Widget")
            .setMessage("To remove the StellaRise widget from your home screen:\n\n1. Long press on the widget\n2. Drag it to 'Remove' or 'Delete'\n3. Or go to Widgets settings and remove it\n\nThis will remove the widget but keep your app data safe.")
            .setPositiveButton("Mark as Removed") { _, _ ->
                setWidgetInstalled(false)
                updateWidgetButtonState(view?.findViewById(R.id.btnExportData)!!)
                Toast.makeText(requireContext(), "Widget marked as removed! 🗑️", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openWidgetSettings() {
        try {
            // Try to open the widget picker
            val intent = Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_PICK)
            intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID)
            startActivity(intent)
            
            // Show a dialog to mark widget as installed after user adds it
            AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                .setTitle("Widget Added?")
                .setMessage("Did you successfully add the StellaRise widget to your home screen?\n\nIf you don't see 'Stellarise' in the widget list, try:\n1. Restarting your launcher\n2. Clearing launcher cache\n3. Reinstalling the app")
                .setPositiveButton("Yes, Added") { _, _ ->
                    setWidgetInstalled(true)
                    updateWidgetButtonState(view?.findViewById(R.id.btnExportData)!!)
                    Toast.makeText(requireContext(), "Widget marked as added! 📱", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Not Yet", null)
                .show()
        } catch (e: Exception) {
            // Fallback: Show instructions
            AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                .setTitle("Widget Instructions")
                .setMessage("To add the widget:\n\n1. Go to your home screen\n2. Long press on empty space\n3. Select 'Widgets'\n4. Find 'Stellarise' and add it\n\nIf you don't see 'Stellarise' in the widget list, the widget might not be properly installed. Try restarting your launcher or reinstalling the app.")
                .setPositiveButton("Mark as Added") { _, _ ->
                    setWidgetInstalled(true)
                    updateWidgetButtonState(view?.findViewById(R.id.btnExportData)!!)
                    Toast.makeText(requireContext(), "Widget marked as added! 📱", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("OK", null)
                .show()
        }
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Clear All Data")
            .setMessage("This will permanently delete all your data. Cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                dataManager.clearAllData()
                // Notify parent activity to refresh all fragments
                (activity as? com.example.stellarise.MainActivity)?.refreshAllFragments()
                AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                    .setTitle("Data Cleared")
                    .setMessage("All data has been cleared successfully.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About Stellarise")
            .setMessage("Stellarise v1.0.0\n\nTrack your habits, mood, and hydration.\n\nMade with ⭐ for your wellness journey!")
            .setPositiveButton("OK", null)
            .show()
    }

}




