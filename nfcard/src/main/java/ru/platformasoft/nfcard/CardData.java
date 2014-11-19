package ru.platformasoft.nfcard;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity reflects the card data available
 * Created by Sergey Kapustin on 06.10.2014.
 */
public class CardData implements Parcelable {

    /**
     * Track 2 equivalent data
     */
    public EmvClient.Track2Data track2;

    /**
     * Card type (mastercard/visa etc.)
     * To be implemented
     */
    public String cardType = "unknown";

    public List<TransactionLog> tranactionLog = new ArrayList<TransactionLog>();

    public CardData(Parcel in) {
        track2 = in.readParcelable(ClassLoader.getSystemClassLoader());
        cardType = in.readString();
        TransactionLog[] logs = (TransactionLog[]) in.readParcelableArray(ClassLoader.getSystemClassLoader());
        if (tranactionLog == null) tranactionLog = new ArrayList<TransactionLog>();
        for (int i = 0; i < logs.length; i++) {
            tranactionLog.add(logs[i]);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        track2.writeToParcel(dest, flags);
        dest.writeString(cardType);
        dest.writeParcelableArray((Parcelable[]) tranactionLog.toArray(), flags);
    }

    /**
     * Builder for card data.
     */
    public static class Builder {

        private CardData data;

        public Builder() {
            data = new CardData();
        }

        public Builder setTrack2(EmvClient.Track2Data data) {
            this.data.track2 = data;
            return this;
        }

        public Builder setCardType(String type) {
            this.data.cardType = type;
            return this;
        }

        public Builder addLogEntry(TransactionLog log) {
            this.data.tranactionLog.add(log);
            return this;
        }

        public CardData build() {
            return data;
        }
    }

    public static final Parcelable.Creator<CardData> CREATOR
            = new Parcelable.Creator<CardData>() {
        public CardData createFromParcel(Parcel in) {
            return new CardData(in);
        }

        public CardData[] newArray(int size) {
            return new CardData[size];
        }
    };

    private CardData() {}
}
