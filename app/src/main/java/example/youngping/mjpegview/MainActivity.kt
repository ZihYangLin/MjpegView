package example.youngping.mjpegview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.yangping.mjpegview.MjpegView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val VIDEO_URL = "https://cctvn04.freeway.gov.tw:8443/stream/GetStreamVideo?pm=160,A40,0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        mjpegView.startPlayback(VIDEO_URL)
        mjpegView.setFPSEnable(false)
        mjpegView.setOnMjpegCompletedListener(object : MjpegView.OnMjpegCompletedListener {
            override fun onCompeleted() {
                Toast.makeText(this@MainActivity, "OnCompleted.", Toast.LENGTH_LONG).show()
                mjpegView.startPlayback(VIDEO_URL)
            }

            override fun onFailure(error: String) {
0g                Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mjpegView.stopPlayback()
    }
}
