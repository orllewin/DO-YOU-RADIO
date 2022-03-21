package orllewin.doyouworld

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import orllewin.doyouworld.databinding.ActivityMainBinding
import orllewin.doyouworld.schedule.ScheduleScraper
import orllewin.doyouworld.schedule.Show

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val rotate = RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
        duration = 1800
        repeatCount = Animation.INFINITE
        interpolator = LinearInterpolator()
    }
    private var playing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.do_you_blue)))

        binding.doYouTitle.setOnClickListener { openWeb("https://doyou.world/", getColor(R.color.do_you_blue)) }
        binding.subscribeButton.setOnClickListener { openWeb("https://ko-fi.com/doyouworld/", getColor(R.color.header_action_button)) }

        binding.trackIdsButtons.setOnClickListener { openWeb("https://doyoutrackid.com", getColor(R.color.small_button)) }
        binding.chatButton.setOnClickListener { openWeb("https://minnit.chat/DOYOUFAMILY?mobile&popout", getColor(R.color.small_button)) }
        binding.playRadio.setOnClickListener {
            when {
                playing -> {
                    playing = false
                    startService(Intent(this, RadService::class.java).also { intent ->
                        intent.putExtra("action", STOP_RAD_SERVICE)
                    })
                    binding.playRadio.text = getString(R.string.play_radio)
                    binding.banana.clearAnimation()
                }
                else -> {
                    playing = true
                    startService(Intent(this, RadService::class.java).also { intent ->
                        intent.putExtra("action", START_RAD_SERVICE)
                    })
                    binding.playRadio.text = getString(R.string.stop_radio)
                    binding.banana.startAnimation(rotate)
                }
            }
        }

        Thread {
            getSchedule()
        }.start()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun getSchedule(){
        ScheduleScraper().getSchedule({ error ->
           Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }, { shows ->
            val now = System.currentTimeMillis()
            var currentShow: Show? = null
            var nextShow: Show? = null
            shows.forEach { show ->
                when {
                    show.startsMS < now && show.endsMS > now -> currentShow = show
                }
                when {
                    show.startsMS > now && nextShow == null -> nextShow = show
                }
            }

            runOnUiThread {
                when {
                    currentShow != null -> {
                        binding.showStatus.text = "On air: ${currentShow!!.name}"
                        binding.showStatus.setTextColor(Color.BLACK)
                    }
                    nextShow != null -> {
                        binding.showStatus.text = "Next show: ${nextShow!!.getGetDateTimeLabel()}:\n${nextShow!!.name}"
                        binding.showStatus.setTextColor(Color.BLACK)
                    }
                }
            }

        })
    }

    private fun openWeb(url: String, colour: Int){

        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(colour)
            .setSecondaryToolbarColor(Color.WHITE)
            .build()

        CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(defaultColors)
            .build()
            .launchUrl(this, Uri.parse(url))
    }
}