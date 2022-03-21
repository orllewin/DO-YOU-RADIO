package orllewin.doyouworld.schedule

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class Show(
    val name: String,
    val startsMS: Long,
    val endsMS: Long
){
    companion object{
        fun fromJSON(json: JSONObject): Show?{
            return when {
                json.has("name") && json.has("description") && json.has("starts") && json.has("ends") -> {
                    Show(
                        name = json.getString("name"),
                        startsMS = json.getLong("starts") * 1000,
                        endsMS = json.getLong("ends") * 1000
                    )
                }
                else -> null
            }
        }
    }

    fun getGetDateTimeLabel(): String = SimpleDateFormat("dd/MM/yyyy HH:mmaa").format(Date(startsMS))
}