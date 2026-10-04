package com.curea.document.service;

import java.util.Locale;

public final class FileTypes {

    public static final String PDF = "application/pdf";
    public static final String PNG = "image/png";
    public static final String JPEG = "image/jpeg";
    public static final String DOC = "application/msword";
    public static final String XLS = "application/vnd.ms-excel";
    public static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private FileTypes() {
    }

    public static String detect(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length < 4) {
            return null;
        }
        String ext = extension(fileName);
        if (startsWith(bytes, 0x89, 0x50, 0x4E, 0x47)) return PNG;
        if (startsWith(bytes, 0xFF, 0xD8, 0xFF)) return JPEG;
        if (startsWith(bytes, 0x25, 0x50, 0x44, 0x46, 0x2D)) return PDF;
        if (startsWith(bytes, 0x50, 0x4B, 0x03, 0x04)) {
            if ("docx".equals(ext)) return DOCX;
            if ("xlsx".equals(ext)) return XLSX;
            return null;
        }
        if (startsWith(bytes, 0xD0, 0xCF, 0x11, 0xE0)) {
            if ("doc".equals(ext)) return DOC;
            if ("xls".equals(ext)) return XLS;
            return null;
        }
        return null;
    }

    public static boolean isInline(String mimeType) {
        return PDF.equals(mimeType) || PNG.equals(mimeType) || JPEG.equals(mimeType);
    }

    public static String cleanName(String name) {
        if (name == null || name.isBlank()) {
            return "belge";
        }
        String base = name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1);
        base = base.replaceAll("[\\p{Cntrl}\"<>|*?:]", "").trim();
        if (base.isEmpty()) {
            return "belge";
        }
        return base.length() > 200 ? base.substring(base.length() - 200) : base;
    }

    private static String extension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean startsWith(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if ((bytes[i] & 0xFF) != signature[i]) return false;
        }
        return true;
    }
}
