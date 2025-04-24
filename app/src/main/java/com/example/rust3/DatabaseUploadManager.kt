package com.example.rust3

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DatabaseUploadManager(private val artifactPath: String) {

    private val modelFile = File(artifactPath, "model.bin")

    fun checkModelFileExistence(): Boolean {
        return modelFile.exists()
    }

    fun uploadModelFile(uploadUrl: String) {

        if (modelFile.exists()) {
            try {
                val connection = URL(uploadUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                connection.doOutput = true

                // Read file and send the bytes directly
                modelFile.inputStream().use { input ->
                    connection.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    println("File uploaded successfully.")
                } else {
                    println("Failed to upload file. Response code: ${connection.responseCode}")
                }

                connection.disconnect()

            } catch (e: IOException) {
                println("Error uploading file: ${e.message}")
            }
        } else {
            println("Model file does not exist.")
        }
    }
}

