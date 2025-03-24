package com.example.smartscan;

import com.google.mlkit.nl.translate.TranslateLanguage;

import java.util.LinkedHashMap;
import java.util.Map;

public class DanhSachNgonNgu {
    public static Map <String, String> danhSachNgonNgu (){
        Map <String,String> ngonNgu = new LinkedHashMap<>();
        ngonNgu.put("Tiếng Việt", TranslateLanguage.VIETNAMESE);
        ngonNgu.put("Tiếng Anh", TranslateLanguage.ENGLISH);
        ngonNgu.put("Tiếng Nhật", TranslateLanguage.JAPANESE);
        ngonNgu.put("Tiếng Trung", TranslateLanguage.CHINESE);
        ngonNgu.put("Tiếng Hàn", TranslateLanguage.KOREAN);
        ngonNgu.put("Tiếng Pháp", TranslateLanguage.FRENCH);
        ngonNgu.put("Tiếng Đức", TranslateLanguage.GERMAN);
        ngonNgu.put("Tiếng Tây Ban Nha", TranslateLanguage.SPANISH);
        ngonNgu.put("Tiếng Nga", TranslateLanguage.RUSSIAN);
        ngonNgu.put("Tiếng Thái", TranslateLanguage.THAI);
        ngonNgu.put("Tiếng Indonesia", TranslateLanguage.INDONESIAN);
        return ngonNgu;
    }
}
