package ru.platformasoft.nfcard;

import java.io.IOException;

/**
 * Data feed:
 * something that might execute commands represented as byte array
 * Created by Sergey Kapustin on 01.10.2014.
 */
public interface IDataFeed {

    public byte [] execute(byte [] command) throws IOException;
}
