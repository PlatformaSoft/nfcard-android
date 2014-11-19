package ru.platformasoft.nfcard.ber;

import java.util.ArrayList;
import java.util.List;

/**
 * Full representation of ber object
 * it is TLV stuff
 * Created by Sergey Kapustin on 01.10.2014.
 */
public class BerObject {

    public BerTag type;
    public BerLength length;
    public List<BerObject> children;
    public String octetStringValue;
    public int integerValue;
    public byte [] byteArrayValue;

    public BerObject(BerTag type, BerLength length) {
        this.type = type;
        this.length = length;
        this.children = new ArrayList<BerObject>();
        this.octetStringValue = null;
        this.integerValue = 0;
    }

    public void addChild(BerObject child) {
        children.add(child);
    }

    public void setOctetStringValue(String value) {
        octetStringValue = value;
    }

    public void setIntegerValue(Integer value) {
        integerValue = integerValue;
    }

    public void byteArrayValue(byte [] value) {
        byteArrayValue = value;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(type.toString()).append(" ").append(length.toString()).append('\n');
        if (type.isPrimitive) {
            sb.append("PRIMITIVE: '").append(octetStringValue).append("' or ").append(integerValue).append('\n');
        } else {
            for (BerObject child : children) {
                sb.append("=============\n");
                sb.append(child.toString());
                sb.append("=============\n");
            }
        }
        return sb.toString();
    }
}
