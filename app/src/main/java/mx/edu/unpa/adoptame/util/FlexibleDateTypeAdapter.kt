package mx.edu.unpa.adoptame.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Spring/Jackson puede devolver LocalDateTime como texto ISO o como arreglo [año, mes, día, hora, min, seg].
 */
class FlexibleDateTypeAdapter : TypeAdapter<String?>() {

    override fun write(out: JsonWriter, value: String?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.STRING -> reader.nextString()
            JsonToken.BEGIN_ARRAY -> {
                reader.beginArray()
                val year = reader.nextInt()
                val month = reader.nextInt()
                val day = reader.nextInt()
                val hour = if (reader.hasNext() && reader.peek() != JsonToken.END_ARRAY) {
                    reader.nextInt()
                } else {
                    0
                }
                val minute = if (reader.hasNext() && reader.peek() != JsonToken.END_ARRAY) {
                    reader.nextInt()
                } else {
                    0
                }
                val second = if (reader.hasNext() && reader.peek() != JsonToken.END_ARRAY) {
                    reader.nextInt()
                } else {
                    0
                }
                while (reader.hasNext()) {
                    reader.skipValue()
                }
                reader.endArray()
                String.format("%04d-%02d-%02dT%02d:%02d:%02d", year, month, day, hour, minute, second)
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
