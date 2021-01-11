/*
 * This file is part of Millennium, licensed under the MIT License.
 *
 * Copyright (C) 2020 Millennium & Team
 *
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.warfaremc.earth.miscellaneous

import eu.warfaremc.earth.plugin
import org.apache.commons.io.FileUtils
import java.io.*
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


class DownloadHelper {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun handleArchive(url: URL, targetDir: File): File {
            if (targetDir.exists() == false)
                targetDir.mkdirs()
            val result: Result<File> = kotlin.runCatching {
                val file = File(targetDir, "tmp.t19441d1101y21.zip")
                if (file.exists() == false)
                    file.createNewFile()
                FileUtils.copyURLToFile(url, file)
                file
            }
            if (result.isSuccess)
                return handleArchive(result.getOrNull()!!, targetDir)
            throw IOException("Failed to download file: ", result.exceptionOrNull())
        }

        @JvmStatic
        @Throws(IOException::class)
        fun handleArchive(archive: File, targetDir: File): File {
            if (archive.exists() == false)
                throw IOException(archive.absolutePath + " does not exist")
            if (buildDirectory(targetDir) == false)
                throw IOException("Could not create directory: $targetDir")
            val zip = ZipFile(archive, 0x1, StandardCharsets.UTF_8)
            val entries: Enumeration<*> = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                val file = File(targetDir, File.separator + entry.name)
                if (buildDirectory(file.parentFile) == false)
                    throw IOException("Could not create directory: " + file.parentFile)
                if (entry.isDirectory == false) {
                    copyInputStream(zip.getInputStream(entry), BufferedOutputStream(FileOutputStream(file)))
                } else {
                    if (buildDirectory(file) == false) {
                        throw IOException("Could not create directory: $file")
                    }
                }
            }
            zip.close()
            return archive
        }

        @JvmStatic
        fun buildDirectory(file: File): Boolean
                = file.exists() || file.mkdirs()

        //region private
        @JvmStatic
        @Throws(IOException::class)
        private fun copyInputStream(i: InputStream, o: OutputStream) {
            val buffer = ByteArray(1024)
            var length = i.read(buffer)
            while (length >= 0) {
                o.write(buffer, 0, length)
                length = i.read(buffer)
            }
            i.close()
            o.close()
        }
        //endregion private
    }
}