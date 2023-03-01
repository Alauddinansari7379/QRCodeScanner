package com.example.qrcodescanner

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.example.qrcodescanner.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var codeScanner: CodeScanner
    var REQUEST_CAMERA_PERMISSION=101
    var resultData=""
    private val HTTPS = "https://"
    private val HTTP = "http://"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

        if (checkPermission()) {
            checkPermission()
        }else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION);
        }

//
//        var clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//        var clip = ClipData.newPlainText("label", file.readText())
//        clipboard.setPrimaryClip() = clip
//        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                binding.tvResult.setText(it.text)
                 resultData= binding.tvResult.text.toString()

                vibrateOnce(this)
                beep(100)

                //  Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
            }
        }
        binding.btnCopy.setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("label", resultData)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()

        }
        binding.btnOpenInBrowser.setOnClickListener {
//            val url = "https://www.example.com"
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.data = Uri.parse(resultData)
//            startActivity(intent)
            openBrowser(this,resultData)
         //   startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resultData)))
//            try {
//                val intentUrl = "www.google.com"
//              //  val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(resultData))
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resultData)))
//               // startActivity(webIntent)
//            } catch (e: ActivityNotFoundException) {
//                Toast.makeText(this, "Dowanload browser: ",
//                    Toast.LENGTH_LONG).show()
//                /*show Error Toast
//                        or
//
//                  Open play store to download browser*/
//
//            }

        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
//                Toast.makeText(this, "Camera initialization error: ${it.message}",
//                    Toast.LENGTH_LONG).show()
            }
        }

        binding.btnReScan.setOnClickListener {
            binding.tvResult.text!!.clear()
            codeScanner.startPreview()

        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }
    fun openBrowser(context: Context, url: String) {
        var url = url
        if (!url.startsWith(HTTP) && !url.startsWith(HTTPS)) {
            url = HTTP + url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(
            Intent.createChooser(intent, "Choose browser")) // Choose browser is arbitrary :)
    }

    fun Context.copyToClipboard(text: CharSequence){
        val clipboard = ContextCompat.getSystemService(this,ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("",text))
    }

    fun vibrateOnce(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vib = vibratorManager.defaultVibrator
            vib.vibrate(VibrationEffect.createOneShot(200, 1))
        } else {
            val vib = context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
            vib.vibrate(100)
        }
    }
    val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
    fun beep(duration: Int) {
        toneG.startTone(ToneGenerator.TONE_DTMF_S, duration)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
        }, (duration + 500).toLong())
    }
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}