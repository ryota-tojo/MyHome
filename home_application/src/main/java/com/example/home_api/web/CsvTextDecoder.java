package com.example.home_api.web;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

final class CsvTextDecoder {
    private CsvTextDecoder(){}

    static String decode(byte[] bytes){
        String text;
        try{
            text=StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString();
        }catch(CharacterCodingException ex){
            text=new String(bytes,Charset.forName("MS932"));
        }
        return text.startsWith("\uFEFF")?text.substring(1):text;
    }
}
