package com.fluidtouch.noteshelf.commons.utils

object StringUtil {
    @JvmStatic
    fun getFileName(path: String): String {
        val lastIndex = path.lastIndexOf("/")
        return path.substring(lastIndex + 1)
    }

    @JvmStatic
    fun getFileExtension(string: String): String {
        val lastIndex = string.lastIndexOf(".")
        return if (lastIndex > 0) string.substring(lastIndex + 1) else ""
    }
}