package ru.platformasoft.nfcard.ber;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Decorator for input stream that fetches us BER entities
 * Created by Sergey Kapustin on 01.10.2014.
 */
public class BerInputStream extends InputStream {

    private InputStream source;

    @Override
    public int available() throws IOException {

        return source.available();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public void mark(int readlimit) {
        source.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return source.markSupported();
    }

    @Override
    public int read() throws IOException {
        return source.read();
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return source.read(buffer);
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        return source.read(buffer, byteOffset, byteCount);
    }

    @Override
    public void reset() throws IOException {
        source.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return source.skip(byteCount);
    }

    public BerInputStream(InputStream source) {
        this.source = source;
    }

    public BerTag readBerType() throws IOException {
        return new BerTag(source);
    }

    public BerLength readBerLength() throws IOException {
        return new BerLength(source);
    }

    public BerObject readBerObject() throws IOException {
        BerTag type = readBerType();
        BerLength length = readBerLength();
//        Log.d("BIS", "TYPE READ:" + type);
//        Log.d("BIS", "LEN READ: " + length.toString());
        BerObject result = new BerObject(type, length);
        //now we need to read the length bytes and do something with it
        byte [] valueBuffer = new byte[length.length];
        read(valueBuffer, 0, valueBuffer.length);//fill in the value
        //now it depends on the type
        if (type.isPrimitive) {
            switch (type.berTag) {
                case BerTag.FLAG_PRIMITIVE_INTEGER:
                case BerTag.FLAG_PRIMITIVE_BOOLEAN:
                case 7:
                    //it is integer
                    result.integerValue = 0;
                    for (int i = valueBuffer.length - 1; i>=0; i--) {
                        result.integerValue <<= 8;
                        result.integerValue |= valueBuffer[i];
                    }
                    break;
                case BerTag.FLAG_PRIMITIVE_OCTET_STRING:
                    result.octetStringValue = new String(valueBuffer);
                    break;
                default:
                    //well, this is sequence!
                    result.octetStringValue = new String(valueBuffer);
                    break;
//                default:
//                    throw new IOException("Unknown primitive object class: " + type.objectClass);

            }
            //remember bytes, we might need them.
            result.byteArrayValue = valueBuffer;
        } else {
            //it is non-primitive, it should be a complex stuff
            BerInputStream subStream = new BerInputStream(new ByteArrayInputStream(valueBuffer));
            while (subStream.available() > 0) {
                result.addChild(subStream.readBerObject());
            }
        }
        return result;
    }
}
