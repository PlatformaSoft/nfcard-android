package ru.platformasoft.nfcard.ber;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for BER entities
 * Created by Sergey Kapustin on 01.10.2014.
 */
public class BerUtils {

    public static int parseLength(InputStream input) throws IOException {
        int complexTag = 0;
        int nextByte = input.read();

        boolean isComplexLength = (nextByte & 0x80) > 0;
        if (isComplexLength) {
            int bytesCount = nextByte & 0x7f;
            for (int i = 0; i < bytesCount; i++) {
                complexTag <<= 8;
                complexTag |= input.read();
            }
            return complexTag;
        } else {
            return nextByte & 0x7f;
        }
    }

    public static int parseTag(InputStream input, int prefix) throws IOException {
        int complexTag = 0;
        if (prefix != 0) {
            complexTag = prefix;
        }
        //we need to read subsequent bytes
        boolean needReadNextSubsequentByte = true;
        while (needReadNextSubsequentByte) {
            int nextByte = input.read();
            //first bit means if we have subsequent byte(s)
            needReadNextSubsequentByte = (nextByte & 0x80) > 0;
            int tagPart = nextByte;
            complexTag <<= 8;
            complexTag |= tagPart;
        }
        return complexTag;
    }
}
