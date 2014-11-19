package ru.platformasoft.nfcard.ber;

import java.util.Iterator;

/**
 * Ber value is something that represents value
 * Created by Sergey Kapustin on 01.10.2014.
 */
public abstract class BerValue {

    /**
     * Is ber value primitive?
     * @return
     */
    public abstract boolean isPrimitive();

    /**
     * Fetch sub-entities iterator
     * @return
     */
    public abstract Iterator<BerObject> iterator();
}
