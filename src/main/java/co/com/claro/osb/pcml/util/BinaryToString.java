package co.com.claro.osb.pcml.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BinaryToString {

    private BinaryToString() {
    }

    public static String getBinaryContentAsString(Object input) {
        if (input == null) {
            return "";
        }
        if (input instanceof byte[]) {
            return new String((byte[]) input, StandardCharsets.UTF_8);
        }
        if (input instanceof InputStream) {
            return readStream((InputStream) input, StandardCharsets.UTF_8);
        }
        if (input instanceof String) {
            return (String) input;
        }
        // fallback: attempt to Base64 encode
        return Base64.getEncoder().encodeToString(input.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String readStream(InputStream stream, Charset charset) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        try {
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } catch (IOException e) {
            return "";
        }
        return new String(output.toByteArray(), charset);
    }
}
