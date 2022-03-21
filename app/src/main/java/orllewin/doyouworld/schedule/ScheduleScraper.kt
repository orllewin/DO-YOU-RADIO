package orllewin.doyouworld.schedule

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class ScheduleScraper {

    fun getSchedule(onError: (message: String) -> Unit, onShows: (shows: List<Show>) -> Unit){
        getScheduleHtml{ html ->
            val line = html.lines().find { line ->
                line.trim().startsWith("var schedule_data = {")
            }

            if(line == null){
                onError("Couldn't find schedule data")
            }else{
                val rawJson = line.substring(line.indexOf("{"), line.lastIndexOf("}") + 1)
                processScheduleJson(rawJson){ shows ->
                    onShows(shows)
                }
            }
        }
    }

    private fun processScheduleJson(rawJson: String, onShows: (shows: List<Show>) -> Unit){
        val scheduleJson = JSONObject(rawJson)
        val showsJsonArray = scheduleJson.getJSONArray("shows")

        val shows = mutableListOf<Show>()
        repeat(showsJsonArray.length()){ index ->
            val showJson = showsJsonArray.getJSONObject(index)
            val show = Show.fromJSON(showJson)
            show?.let{
                shows.add(show)
            }
        }

        onShows(shows)
    }

    private fun getScheduleHtml(onHtml: (webpage: String) -> Unit) {
        URL("https://doyouworld.airtime.pro/embed/weekly-program").openStream().use { inputStream ->
            InputStreamReader(inputStream).use { inputStreamReader ->
                BufferedReader(inputStreamReader).use { bufferReader ->
                    val html: StringBuilder = StringBuilder()
                    var line: String?
                    while (bufferReader.readLine().also { line = it } != null) {
                        html.append("$line\n")
                    }
                    onHtml(html.toString())
                }
            }
        }
    }
}