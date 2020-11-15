package com.example.vizassist.utilities;

import android.graphics.Bitmap;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility class with methods to perform http operations
 */
public class HttpUtilities {

    /**
     * Make a {@link HttpURLConnection} that can be used to send a POST request with image data.
     *
     * @param bitmap    image to be sent to server
     * @param urlString URL address of OCR server
     * @return {@link HttpURLConnection} to be used to connect to server
     * @throws IOException
     */
    public static HttpURLConnection makeHttpPostConnectionToUploadImage(Bitmap bitmap,
                                                                        String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");//http protocal

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);//compress bitmap to bos
        byte[] data = bos.toByteArray();//turn to raw data
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(data, ContentType.IMAGE_JPEG);//entity is for convey form data or table information.

        conn.addRequestProperty("Content-length", byteArrayEntity.getContentLength() + ""); //add length is common practice
        conn.addRequestProperty(byteArrayEntity.getContentType().getName(),
                byteArrayEntity.getContentType().getValue());//like to add JSon like data to this connection

        OutputStream os = conn.getOutputStream(); // output stream is to write something on server
        // byteArrayEntity.writeTo(conn.getOutputStream()); // conn.getOutputStream() will be closed automatically
        byteArrayEntity.writeTo(os); // both lines are fine
        os.close();
        return conn;
    }

    /**
     * Parse OCR response return by OCR server.
     *
     * @param httpURLConnection @{@link HttpURLConnection} used to connect to OCR server, which
     *                          contains a response JSON if succeeded.
     * @return a string representing text found in the image sent to OCR server
     * @throws JSONException
     * @throws IOException
     */
    public static String parseOCRResponse(HttpURLConnection httpURLConnection) throws JSONException,
            IOException {
        JSONObject resultObject = new JSONObject(readStream(httpURLConnection.getInputStream()));
        String result = resultObject.getString("text");
        return result;
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
}
