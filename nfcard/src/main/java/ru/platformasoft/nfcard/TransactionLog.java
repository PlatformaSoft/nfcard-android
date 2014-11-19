package ru.platformasoft.nfcard;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Transaction Log model that is exported to library user
 * (single entry)
 * Created by Sergey Kapustin on 06.10.2014.
 */
public class TransactionLog implements Parcelable {

    /**Currency*/
    public String currency;

    /**Transaction authorized amount*/
    public float amount;

    /**Readable cryptogram information data*/
    public String cryptogramInformationData;

    /**Year of transaction*/
    public int year;

    /**Month of transaction*/
    public int month;

    /**Day of transaction*/
    public int day;

    public TransactionLog() {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currency);
        dest.writeFloat(amount);
        dest.writeString(cryptogramInformationData);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
    }
}
