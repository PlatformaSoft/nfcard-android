package ru.platformasoft.nfcard.ber;

import java.io.IOException;
import java.io.InputStream;

/**
 * This entity represents length of the BER
 * Created by Sergey Kapustin on 01.10.2014.
 */
public class BerLength {

    public final int length;

    public BerLength(InputStream input) throws IOException {
        length = BerUtils.parseLength(input);
    }

    public String toString() {
        return "LEN={" + String.valueOf(length) + "}";
    }

}
