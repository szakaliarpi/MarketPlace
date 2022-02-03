package com.example.bazaar.utils

class ApiString(
        private val map: MutableMap<String, String>?) {

    data class Builder(
            var map: MutableMap<String, String>? = null) {

        fun map(map: MutableMap<String, String>) = apply { this.map = map }
        fun build() = ApiString(map)
    }

    fun getString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("{")
        val sizeOfMap = map!!.size
        var counter = 0
        map.forEach {
            stringBuilder.append("\"" + it.key + "\"")

            if (it.value.toIntOrNull() != null) {
                if (counter != sizeOfMap - 1) {
                    stringBuilder.append(" : " + it.value + " ")
                } else {
                    stringBuilder.append(" : " + it.value)
                }
            } else {
                stringBuilder.append(":\"" + it.value + "\"")
            }

            if (counter != sizeOfMap - 1) {
                stringBuilder.append(",")
            }

            counter++
        }
        stringBuilder.append("}")

        return String(stringBuilder)
    }
}