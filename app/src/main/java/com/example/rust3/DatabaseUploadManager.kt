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

                modelFile.inputStream().use { input ->
                    connection.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    println("The model was uploaded successfully.")
                } else {
                    println("The model Failed to upload. ${connection.responseCode}")
                }
                connection.disconnect()

            } catch (e: IOException) {
                println("Error uploading the model: ${e.message}")
            }
        } else {
            println("Model file does not exist.")
        }
    }
}

