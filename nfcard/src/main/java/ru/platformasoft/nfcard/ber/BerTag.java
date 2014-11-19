package ru.platformasoft.nfcard.ber;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class that represents BER type
 * Created by Sergey Kapustin on 01.10.2014.
 */
public class BerTag {

    public static final int TYPE_UNIVERSAL = 0x0;
    public static final int TYPE_APPLICATION = 0x1;
    public static final int TYPE_CONTEXT = 0x2;
    public static final int TYPE_PRIVATE = 0x3;

    public static final int FLAG_IS_PRIMITIVE = 0x0;
    public static final int FLAG_IS_CONSTRUCTED = 0x1;

    public static final int FLAG_PRIMITIVE_BOOLEAN = 0x1;
    public static final int FLAG_PRIMITIVE_INTEGER = 0x2;
    public static final int FLAG_PRIMITIVE_OCTET_STRING = 0x4;
    public static final int FLAG_PRIMITIVE_SEQUENCE = 0x10;
    public static final int FLAG_NEED_SUBSEQUENT_BYTES = 0x1F;

    public final int objectClass;
    public final boolean isPrimitive;
    public final int berTag;

    public BerTag(InputStream input) throws IOException {
        int tagByte = (int) input.read();

        objectClass = (tagByte & 0xC0) >> 6;
        isPrimitive = ((tagByte & 0x20) >> 5) == FLAG_IS_PRIMITIVE ? true: false;
        int tag = tagByte;
        int tagTitle = tagByte & FLAG_NEED_SUBSEQUENT_BYTES;

        if (tagTitle == FLAG_NEED_SUBSEQUENT_BYTES) {
            berTag = BerUtils.parseTag(input, tag);
        } else {
            berTag = tag;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        switch (objectClass) {
            case TYPE_UNIVERSAL:
                sb.append("[UNIVERSAL]");
                break;
            case TYPE_APPLICATION:
                sb.append("[APPLICATION]");
                break;
            case TYPE_CONTEXT:
                sb.append("[CONTEXT]");
                break;
            case TYPE_PRIVATE:
                sb.append("[PRIVATE]");
                break;
        }
        sb.append(' ');
        sb.append(isPrimitive ? "{PRMTV}" : "{CMPLX}");
        sb.append(' ');
        sb.append(berTag);
        return sb.toString();
    }
}
