package ru.platformasoft.nfcard;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is entity that describes transaction log format (TAG - LENTGTH)
 * Created by Sergey Kapustin on 06.10.2014.
 */
public class TransactionLogFormat {

    /**
     * ISO 4217 allows us to map currency codes
     * TODO: add more currencies into this map
     */
    public static final Map<String, String> CURRENCY_ISO_4217_MAP = new HashMap<String,String>();
    static {
        CURRENCY_ISO_4217_MAP.put("0643", "RUB");
        CURRENCY_ISO_4217_MAP.put("0810", "RUB");
        CURRENCY_ISO_4217_MAP.put("0998", "USD");
        CURRENCY_ISO_4217_MAP.put("0840", "USD");
        CURRENCY_ISO_4217_MAP.put("0997", "USD");
        CURRENCY_ISO_4217_MAP.put("0978", "EUR");
    }

    /*
    Section of tags supported by nfcard, according to EMVCo specifications
     */
    public static final int TAG_TRANSACTION_DATE = 0x9A;
    public static final int TAG_TRANSACTION_CID = 0x9F27;
    public static final int TAG_TRANSACTION_AMOUNT = 0x9F02;
    public static final int TAG_TRANSACTION_CURRENCY = 0x5F2A;

    /**Usually cards hold no more than this amount of records in log format*/
    private static final int EXPECTED_TAG_SEQ_SIZE = 10;

    private List<Pair<Integer, Integer>> mTagSequence = new ArrayList<Pair<Integer, Integer>>(EXPECTED_TAG_SEQ_SIZE);

    /**
     * Add new record to this transaction log format
     * @param tag - tag value
     * @param len - length of data expected in log transactions
     */
    public void addRecord(int tag, int len) {
        System.err.println("Add tag format: " + tag + " - " + len);
        mTagSequence.add(new Pair<Integer, Integer>(tag, len));
    }

    /**
     * Utility feature that allows to convert currency code into readable format
     * @param input
     * @return
     */
    public static final String convertCurrency(String input) {
        String mapped = CURRENCY_ISO_4217_MAP.get(input);
        if (mapped == null) return input;
        return mapped;
    }

    /**
     * Read fine information from crypto info data
     * TODO: add more information (currently data is extracted just from b7 b8
     * @param cid - cryptogram information data
     * @return human-readable String
     */
    public static final String convertCryptogramInformation(int cid) {
        int b78 = (cid & 0xC0) >> 6;
        switch (b78) {
            case 0:
                return "Transaction Declined";
            case 1:
                return "Transaction Approved";
            case 2:
                return "Online Auth Requested";
            case 3:
                return "AAR";

        }
        return String.valueOf(cid)  ;
    }

    /**
     * Tag format iterator
     * @return
     */
    public Iterator<Pair<Integer, Integer>> iterator() {
        return mTagSequence.iterator();
    }
}
