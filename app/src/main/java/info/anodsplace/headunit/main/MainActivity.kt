package info.anodsplace.headunit.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import info.anodsplace.headunit.App
import info.anodsplace.headunit.R
import info.anodsplace.headunit.aap.AapProjectionActivity
import info.anodsplace.headunit.aap.AapService
import info.anodsplace.headunit.utils.AppLog
import info.anodsplace.headunit.utils.NetworkUtils
import info.anodsplace.headunit.utils.SystemUI
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : FragmentActivity() {

    var keyListener: KeyListener? = null
    private val viewModel: MainViewModel by viewModels()

    interface KeyListener
    {
        fun onKeyEvent(event: KeyEvent): Boolean
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        video_button.setOnClickListener {
            val aapIntent = Intent(this@MainActivity, AapProjectionActivity::class.java)
            aapIntent.putExtra(AapProjectionActivity.EXTRA_FOCUS, true)
            startActivity(aapIntent)
        }

        net.setOnClickListener {
            /*
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_content, NetworkListFragment())
                    .commit()
             */

            AppLog.e("Starting service in Wifi mode")
            startService(AapService.createIntent("127.0.0.1", this))
            // startService(AapService.createIntent("192.168.1.173", this))
        }

        usb.setOnClickListener {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_content, UsbListFragment())
                    .commit()
        }

        settings.setOnClickListener {
            // HACK
            video_button.isEnabled = App.provide(this).transport.isAlive
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_content, SettingsFragment())
                    .commit()
        }

        viewModel.register()

        try {
            val currentIp = NetworkUtils.getWifiIpAddress(this)
            val inet = NetworkUtils.intToInetAddress(currentIp)
            val ipView = findViewById<TextView>(R.id.ip_address)
            ipView.text = inet?.hostAddress ?: ""
        } catch (ignored: IOException) { }

        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION
        ), permissionRequestCode)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        SystemUI.hide(window.decorView)
    }

    override fun onResume() {
        super.onResume()
        video_button.isEnabled = App.provide(this).transport.isAlive
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        AppLog.i("onKeyDown: %d", keyCode)

        return keyListener?.onKeyEvent(event) ?: super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        AppLog.i("onKeyUp: %d", keyCode)

        return keyListener?.onKeyEvent(event) ?: super.onKeyUp(keyCode, event)
    }

    companion object {
        private const val permissionRequestCode = 97
    }
}
