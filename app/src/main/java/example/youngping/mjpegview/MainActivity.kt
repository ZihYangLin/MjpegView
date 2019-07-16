package example.youngping.mjpegview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.yangping.mjpegview.MjpegView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val VIDEO_URL = "http://201.166.63.44/axis-cgi/mjpg/video.cgi"
    }

    private var mVideoUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button_enter.setOnClickListener {
            mVideoUrl = edit_input.text.toString()
            startVideo()
        }

        button_cancel.setOnClickListener {
            mjpegView.stopPlayback()
        }

        button_default.setOnClickListener {
            mVideoUrl = VIDEO_URL
            startVideo()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mVideoUrl.isNotEmpty()) {
            startVideo()
        }
    }

    override fun onPause() {
        super.onPause()
        mjpegView.stopPlayback()
    }

    private fun startVideo() {
        mjpegView.stopPlayback()
        mjpegView.startPlayback(mVideoUrl)
        mjpegView.setOnMjpegCompletedListener(object : MjpegView.OnMjpegCompletedListener {
            override fun onCompeleted() {
                Toast.makeText(this@MainActivity, "OnCompleted.", Toast.LENGTH_LONG).show()
                mjpegView.startPlayback(mVideoUrl)
            }

            override fun onFailure(error: String) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
