package util;

import java.util.*;

/**
 * JSON helper đơn giản, KHÔNG cần thư viện ngoài.
 * Chỉ hỗ trợ đúng cấu trúc cần dùng cho giao thức phân công.
 */
public class JsonHelper {

    // ==================== BUILD JSON ====================

    /** Escape chuỗi cho JSON */
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    /** Tạo JSON object từ Map<String, Object> */
    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":");
            sb.append(valueToJson(e.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String valueToJson(Object v) {
        if (v == null) return "null";
        if (v instanceof Number) return v.toString();
        if (v instanceof Boolean) return v.toString();
        if (v instanceof String) return "\"" + escape((String) v) + "\"";
        if (v instanceof Map) return toJson((Map<String, Object>) v);
        if (v instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (List<?>) v) {
                if (!first) sb.append(",");
                first = false;
                sb.append(valueToJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escape(v.toString()) + "\"";
    }

    // ==================== PARSE JSON ====================

    /** Parse JSON string thành Map */
    public static Map<String, Object> parseObject(String json) {
        json = json.trim();
        if (!json.startsWith("{")) throw new RuntimeException("Invalid JSON object");
        return (Map<String, Object>) parseValue(json, new int[]{0});
    }

    /** Parse JSON string thành List */
    public static List<Object> parseArray(String json) {
        json = json.trim();
        if (!json.startsWith("[")) throw new RuntimeException("Invalid JSON array");
        return (List<Object>) parseValue(json, new int[]{0});
    }

    @SuppressWarnings("unchecked")
    private static Object parseValue(String json, int[] pos) {
        skipWhitespace(json, pos);
        char c = json.charAt(pos[0]);

        if (c == '{') return parseObjectInternal(json, pos);
        if (c == '[') return parseArrayInternal(json, pos);
        if (c == '"') return parseString(json, pos);
        if (c == 't' || c == 'f') return parseBoolean(json, pos);
        if (c == 'n') { pos[0] += 4; return null; }
        return parseNumber(json, pos);
    }

    private static Map<String, Object> parseObjectInternal(String json, int[] pos) {
        Map<String, Object> map = new LinkedHashMap<>();
        pos[0]++; // skip {
        skipWhitespace(json, pos);
        if (json.charAt(pos[0]) == '}') { pos[0]++; return map; }

        while (true) {
            skipWhitespace(json, pos);
            String key = parseString(json, pos);
            skipWhitespace(json, pos);
            pos[0]++; // skip :
            Object value = parseValue(json, pos);
            map.put(key, value);
            skipWhitespace(json, pos);
            if (json.charAt(pos[0]) == '}') { pos[0]++; return map; }
            pos[0]++; // skip ,
        }
    }

    private static List<Object> parseArrayInternal(String json, int[] pos) {
        List<Object> list = new ArrayList<>();
        pos[0]++; // skip [
        skipWhitespace(json, pos);
        if (json.charAt(pos[0]) == ']') { pos[0]++; return list; }

        while (true) {
            list.add(parseValue(json, pos));
            skipWhitespace(json, pos);
            if (json.charAt(pos[0]) == ']') { pos[0]++; return list; }
            pos[0]++; // skip ,
        }
    }

    private static String parseString(String json, int[] pos) {
        pos[0]++; // skip opening "
        StringBuilder sb = new StringBuilder();
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]);
            if (c == '\\') {
                pos[0]++;
                char next = json.charAt(pos[0]);
                if (next == '"') sb.append('"');
                else if (next == '\\') sb.append('\\');
                else if (next == 'n') sb.append('\n');
                else if (next == 'r') sb.append('\r');
                else if (next == 't') sb.append('\t');
                else sb.append(next);
            } else if (c == '"') {
                pos[0]++;
                return sb.toString();
            } else {
                sb.append(c);
            }
            pos[0]++;
        }
        return sb.toString();
    }

    private static Object parseNumber(String json, int[] pos) {
        int start = pos[0];
        boolean isFloat = false;
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]);
            if (c == '.' || c == 'e' || c == 'E') isFloat = true;
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) break;
            pos[0]++;
        }
        String num = json.substring(start, pos[0]);
        if (isFloat) return Double.parseDouble(num);
        long val = Long.parseLong(num);
        if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) return (int) val;
        return val;
    }

    private static Boolean parseBoolean(String json, int[] pos) {
        if (json.startsWith("true", pos[0])) { pos[0] += 4; return true; }
        pos[0] += 5; return false;
    }

    private static void skipWhitespace(String json, int[] pos) {
        while (pos[0] < json.length() && Character.isWhitespace(json.charAt(pos[0]))) pos[0]++;
    }

    // ==================== CONVENIENCE ====================

    /** Lấy String từ Map, trả về defaultVal nếu null */
    public static String getString(Map<String, Object> map, String key, String defaultVal) {
        Object v = map.get(key);
        return v != null ? v.toString() : defaultVal;
    }

    /** Lấy int từ Map */
    public static int getInt(Map<String, Object> map, String key, int defaultVal) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) try { return Integer.parseInt((String) v); } catch (Exception e) {}
        return defaultVal;
    }

    /** Lấy boolean từ Map */
    public static boolean getBool(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        return false;
    }

    /** Lấy List<Map> từ Map */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getList(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof List) return (List<Map<String, Object>>) v;
        return new ArrayList<>();
    }

    /** Lấy List<String> từ Map */
    @SuppressWarnings("unchecked")
    public static List<String> getStringList(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) v) {
                result.add(item != null ? item.toString() : "");
            }
            return result;
        }
        return new ArrayList<>();
    }
}
