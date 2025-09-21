package com.vitacheck.etc;

import java.util.List;

public class OcrRequestDTO {
    public String version;
    public String requestId;
    public long timestamp;
    public String lang;
    public List<OcrImage> images;

    public static class OcrImage {
        public String format;
        public String name;
        public String data;   // Base64 인코딩된 이미지 데이터
        public String url;    // 이미지 URL (data와 url 중 하나만 사용)
    }
}