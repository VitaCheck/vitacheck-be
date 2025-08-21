package com.vitacheck.dto;

import java.util.List;

public class OcrRequest {
    public String version;
    public String requestId;
    public long timestamp;
    public String lang;
    public List<OcrImage> images;

    public static class OcrImage {
        public String format; // "png" 등
        public String name;   // "shot" 등
        public String data;   // base64 본문
    }
}