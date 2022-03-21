package orllewin.doyouworld

import com.google.android.exoplayer2.Player

object Log {

    private const val TAG = "DOYOUWORLD"

    fun l(message: String){
        println("$TAG: $message")
    }

    fun playbackState(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> l("playbackState: STATE_IDLE")
            Player.STATE_BUFFERING -> l("playbackState: STATE_BUFFERING")
            Player.STATE_READY -> l("playbackState: STATE_READY")
            Player.STATE_ENDED -> l("playbackState: STATE_ENDED")
            else ->l ("playbackState: Unrecognised state")
        }
    }
}