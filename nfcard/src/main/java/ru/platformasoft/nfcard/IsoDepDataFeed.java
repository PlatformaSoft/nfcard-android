package ru.platformasoft.nfcard;

import android.nfc.tech.IsoDep;

import java.io.IOException;

/**
 * Data feed for IsoDep NFC tag
 * Created by Sergey Kapustin on 01.10.2014.
 */
public class IsoDepDataFeed implements IDataFeed {

    private final IsoDep isoDep;

    public IsoDepDataFeed(IsoDep tag) {
        isoDep = tag;
    }

    @Override
    public byte[] execute(byte[] command) throws IOException {
        return isoDep.transceive(command);
    }
}
