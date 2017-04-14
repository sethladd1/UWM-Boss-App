package com.uwmbossapp.uwmboss.utils;




        import android.util.Log;

        import java.io.BufferedOutputStream;
        import java.io.ByteArrayOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStreamWriter;
        import java.net.CookieHandler;
        import java.net.CookieManager;
        import java.net.HttpCookie;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.List;

        import javax.net.ssl.HttpsURLConnection;

/**
 * Helper class for working with a remote server
 */
public class HttpHelper {

    /**
     * Returns text from a URL on a web server
     *
     * @param address
     * @return
     * @throws IOException
     */
       private static String TAG = "HttpHelper";

    public static String makeHTTPRequest(String address, String message, String requestType) throws IOException {

        InputStream is = null;
        try {
            Log.i(TAG, "makeHTTPRequest: msg=" + message);
            Log.i(TAG, "makeHTTPRequest: url=" +address);

            URL url = new URL(address);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            requestType = requestType.trim();
            conn.setRequestMethod(requestType.toUpperCase());
            conn.setDoInput(true);
            if(requestType.equalsIgnoreCase("post") || requestType.equalsIgnoreCase("put") || requestType.equalsIgnoreCase("patch")) {
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(message);
                writer.flush();
                writer.close();
            }
            conn.connect();

            int responseCode = conn.getResponseCode();
            Log.i(TAG, "postToUrl: " +responseCode);
            if (responseCode != 200) {
                throw new IOException("Got response code " + responseCode);

            }
            is = conn.getInputStream();
            return readStream(is);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }
    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private static String readStream(InputStream stream) throws IOException {

        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        BufferedOutputStream out = null;
        try {
            int length = 0;
            out = new BufferedOutputStream(byteArray);
            while ((length = stream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            return byteArray.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

}
