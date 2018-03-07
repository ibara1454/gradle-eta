package com.typelead.gradle.utils;

import java.util.Map;
import java.util.Iterator;

public class PrintHelper {
    public static <K, V> String toString(Map<K,V> map) {
        StringBuilder result = new StringBuilder();
        Iterator<Map.Entry<K,V>> it = map.entrySet().iterator();
        result.append("{");
        if (it.hasNext()) {
            Map.Entry<K,V> entry = it.next();
            result.append(entry.getKey().toString());
            result.append(": ");
            result.append(entry.getValue().toString());
            while (it.hasNext()) {
                entry = it.next();
                result.append(", ");
                result.append(entry.getKey().toString());
                result.append(": ");
                result.append(entry.getValue().toString());
            }
        }
        result.append("}");
        return result.toString();
    }

    public static String friendlyVersion(String version) {
        String[] versions = version.split("\\.");
        int newLen = versions.length - 1;
        String last = versions[newLen];
        String[] newVersions = new String[newLen];
        System.arraycopy(versions, 0, newVersions, 0, newLen);
        return String.join(".", newVersions) + "b" + last;
    }
}
