package co.com.claro.osb.pcml.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class StringUtil {

    static final byte[] HEX_CHAR_TABLE = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    private StringUtil() {
    }

    public static String implode(Collection<String> data, String delimiter) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String value : data) {
            if (!first) {
                builder.append(delimiter);
            }
            builder.append(value);
            first = false;
        }
        return builder.toString();
    }

    public static String implodeObject(Collection<Object> data, String delimiter) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object value : data) {
            if (!first) {
                builder.append(delimiter);
            }
            builder.append(value);
            first = false;
        }
        return builder.toString();
    }

    public static String repeat(String value, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length() * count);
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    public static String completeWith(String value, String filler, int size) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= size) {
            return value;
        }
        return value + repeat(filler, size - value.length());
    }

    public static String getWords(String value, int count) {
        if (value == null || count <= 0) {
            return "";
        }
        String[] parts = value.split("\\s+", count + 1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length && i < count; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(parts[i]);
        }
        return builder.toString();
    }

    public static String encodeHex(byte[] bytes) {
        byte[] hex = new byte[2 * bytes.length];
        int index = 0;
        for (byte b : bytes) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0x0F];
        }
        return new String(hex, StandardCharsets.US_ASCII);
    }

    public static String toCamelCase(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder builder = new StringBuilder();
        boolean upper = false;
        for (char c : value.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                upper = true;
            } else if (upper) {
                builder.append(Character.toUpperCase(c));
                upper = false;
            } else {
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static Object[] explode(String value, String delimiter) throws StringUtilException {
        if (value == null) {
            throw new StringUtilException("Cadena vacía");
        }
        if (delimiter == null || delimiter.isEmpty()) {
            throw new StringUtilException("Delimiter vacío");
        }
        String[] parts = value.split(delimiter);
        List<Object> results = new ArrayList<>(parts.length);
        for (String part : parts) {
            results.add(part);
        }
        return results.toArray();
    }

    public static int coutChar(String value, char character) {
        if (value == null) {
            return 0;
        }
        int count = 0;
        for (char c : value.toCharArray()) {
            if (c == character) {
                count++;
            }
        }
        return count;
    }
}
