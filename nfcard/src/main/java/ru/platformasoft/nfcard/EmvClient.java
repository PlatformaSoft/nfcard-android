package ru.platformasoft.nfcard;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.platformasoft.nfcard.ber.BerInputStream;
import ru.platformasoft.nfcard.ber.BerObject;
import ru.platformasoft.nfcard.ber.BerTag;

/**
 * EMV client allows us to execute some commands on a card
 * Current implementation allows to fetch CardData to the library user
 * Created by Sergey Kapustin on 01.10.2014.
 */
public class EmvClient {

    private static final byte[] PDOL_RELATED_STANDARD_TAG = {(byte)(0x83 & 0xFF), 0x00};
    private static final byte[] LOG_DATA_FORMAT = {(byte) (0x9f & 0xff), (byte) (0x4f & 0xff)};

    private IDataFeed mDataFeed;

    private TransactionLogFormat mLogFormat;

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String printHexBinary(byte[] data) {
        if (data == null)
            return "NULL";
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    //GPO record descriptor data
    private static class GpoDescriptor {
        public byte sfi;
        public byte recMin;
        public byte recMax;

        public GpoDescriptor(byte sfi, byte recMin, byte recMax) {
            this.sfi = sfi;
            this.recMin = recMin;
            this.recMax = recMax;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer("GPO DESCRIPTOR: ");
            sb.append(sfi).append(' ').append(recMin).append(" - ").append(recMax);
            return sb.toString();
        }

    }

    public static class Track2Data implements Parcelable {
        public String pan;
        public int month;
        public int  year;

        public Track2Data() {}

        public Track2Data(Parcel in) {
            pan= in.readString();
            month = in.readInt();
            year = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(pan);
            dest.writeInt(month);
            dest.writeInt(year);
        }

        public static final Parcelable.Creator<Track2Data> CREATOR
                = new Parcelable.Creator<Track2Data>() {
            public Track2Data createFromParcel(Parcel in) {
                return new Track2Data(in);
            }

            public Track2Data[] newArray(int size) {
                return new Track2Data[size];
            }
        };
    }

    public EmvClient(IDataFeed feed) {
        mDataFeed = feed;
    }

    private byte [] createApduCommand(int cla, int ins, int p1, int p2, byte [] data, int expectedLength) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(cla);
        baos.write(ins);
        baos.write(p1);
        baos.write(p2);
        if (data != null) {
            baos.write(data.length);
            baos.write(data, 0, data.length);
        }
        baos.write(expectedLength);
        return baos.toByteArray();
    }

    private BerObject executeRawCommand(byte [] command) throws IOException {
        byte [] apduResponse = mDataFeed.execute(command);

        //check SW1 SW2
        if (apduResponse[apduResponse.length - 2] != (byte)(0x90 & 0xFF)) throw new IOException("Invalid Response");
        if (apduResponse[apduResponse.length - 1] != 0) throw new IOException("Invalid Response");

        BerInputStream bis = new BerInputStream(new ByteArrayInputStream(apduResponse));
        BerObject ber = bis.readBerObject();
        return ber;
    }

    private byte [] rawExecuteRawCommand(byte [] command) throws IOException {
        byte [] apduResponse = mDataFeed.execute(command);

        //check SW1 SW2
        if (apduResponse[apduResponse.length - 2] != (byte)(0x90 & 0xFF)) throw new IOException("Invalid Response");
        if (apduResponse[apduResponse.length - 1] != 0) throw new IOException("Invalid Response");

        return apduResponse;
    }

    private BerObject selectFile(String filename) throws IOException {
        return executeRawCommand(createApduCommand(0x00, 0xa4, 0x04, 0x00, filename.getBytes(), 0x00));
    }

    private BerObject selectApplication(byte [] applicationIdentifier) throws IOException {
        return executeRawCommand(createApduCommand(0x00,0xa4,0x04,0x00,applicationIdentifier, 0x00));
    }

    private BerObject getProcessingOptions() throws IOException {
        return executeRawCommand(createApduCommand(0x80, 0xa8, 0x00, 0x00, PDOL_RELATED_STANDARD_TAG, 0x00));
    }

    private byte [] readGpoData(GpoDescriptor gpo, byte rec) throws IOException {
        return rawExecuteRawCommand(createApduCommand(0x00, 0xb2, rec, ((gpo.sfi << 3) | 0x4), null, 0x00));
    }

    private BerObject readGpoRecord(GpoDescriptor gpo, byte rec) throws IOException {
        return executeRawCommand(createApduCommand(0x00, 0xb2, rec, ((gpo.sfi << 3) | 0x4), null, 0x00));
    }

    private BerObject getData(byte [] format) throws IOException {
        return executeRawCommand(createApduCommand(0x80, 0xca, format[0], format[1], null, 0x00));
    }

    private TransactionLog parseTransactionRecord(byte [] data) throws IOException {
        if (mLogFormat == null) throw new IllegalStateException("No log format known exists");
        TransactionLog log = new TransactionLog();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Iterator<Pair<Integer, Integer>> iterator = mLogFormat.iterator();

        int index = 0;

        while (iterator.hasNext()) {
            Pair<Integer, Integer> nextTag = iterator.next();

            byte [] chunk = new byte[nextTag.second];
            bais.read(chunk);

            int tagInt = nextTag.first;
            switch (tagInt) {
                case TransactionLogFormat.TAG_TRANSACTION_AMOUNT:
                    String str = printHexBinary(chunk);
                    log.amount = Long.parseLong(str) / 100f;
                    break;
                case TransactionLogFormat.TAG_TRANSACTION_CID:
                    log.cryptogramInformationData = TransactionLogFormat.convertCryptogramInformation(chunk[0]);
                    break;
                case TransactionLogFormat.TAG_TRANSACTION_CURRENCY:
                    log.currency = TransactionLogFormat.convertCurrency(printHexBinary(chunk));
                    break;
                case TransactionLogFormat.TAG_TRANSACTION_DATE:
                    str = printHexBinary(chunk);
                    log.year = Integer.parseInt(str.substring(0,2));
                    log.month = Integer.parseInt(str.substring(2,4));
                    log.day = Integer.parseInt(str.substring(4,6));
                    break;
            }
        }

        return log;
    }

    private TransactionLogFormat getTransactionLogFormat(BerObject logFormat) throws IOException {
        byte [] data = logFormat.byteArrayValue;
        BerInputStream bis = new BerInputStream(new ByteArrayInputStream(data));

        TransactionLogFormat result = new TransactionLogFormat();

        while (bis.available() > 0) {
            BerTag tag = bis.readBerType();
            int len = bis.read();

            result.addRecord(tag.berTag, len);
        }

        return result;
    }

    private Track2Data findTrack2Data(BerObject emvTag) {
        if (emvTag.type.berTag != 0x70) throw  new IllegalArgumentException("No EMV Proprietary Template Specified as Argument");

        for (BerObject child : emvTag.children) {
            if (child.type.berTag == 0x57) {
                //it is Track 2 equivalent data
                byte [] track2 = child.byteArrayValue;
                String track2String = printHexBinary(track2);

                int splitIndex = track2String.indexOf('D');
                if (splitIndex < 0) break;//Invalid pan

                Track2Data result = new Track2Data();
                result.pan = track2String.substring(0, splitIndex);
                result.year = Integer.parseInt(track2String.substring(splitIndex + 1, splitIndex + 3));
                result.month = Integer.parseInt(track2String.substring(splitIndex + 3, splitIndex + 5));

                return result;
            }
        }

        // no track2 found, return null
        return null;
    }

    private void fillGpoDescriptors(byte [] afl, List<GpoDescriptor> list) {
        if (afl.length % 4 != 0) throw new IllegalArgumentException("Invalid AFL value");
        int gpoCount = afl.length  / 4;
        for (int i = 0; i < gpoCount; i++) {
            int offset = i * 4;
            list.add(new GpoDescriptor((byte) (afl[offset] >> 3), afl[offset + 1], afl[offset + 2]));
        }
    }

    //fetch AFL record from Response Message Template Format 2 (e.g. such a message is responded by GET PROCESSING OPTIONS
    private byte[] getAfl(BerObject responseMessageTemplateFormat2) throws IOException {
        if (responseMessageTemplateFormat2.type.berTag != 0x77) throw new IllegalArgumentException("Object is not Response Message Template Format 2");
        for (BerObject afl : responseMessageTemplateFormat2.children) {
            if (afl.type.berTag == 0x94) {
                //it is afl!
                return afl.byteArrayValue;
            }
        }
        throw new IllegalArgumentException("No AFL Found!");
    }

    //get the access to SFI record that holds transaction log
    private byte [] getLogEntry(BerObject fciTempalte) {
        for (BerObject fciProprietaryTemplateCandidate : fciTempalte.children) {
            if (fciProprietaryTemplateCandidate.type.berTag == 0xa5) {
                for (BerObject fciIssuerData : fciProprietaryTemplateCandidate.children) {
                    if (fciIssuerData.type.berTag == 0xbf0c) {
                        //find log entry
                        for (BerObject logEntry : fciIssuerData.children) {
                            if (logEntry.type.berTag == 0x9f4d) {
                                //it is log entry
                                return logEntry.byteArrayValue;
                            }
                        }
                        throw new IllegalArgumentException("No Log Entry Found");
                    }
                }
                throw new IllegalArgumentException("No FCI Issuer Discretionary Data found");
            }
        }
        throw new IllegalArgumentException("No FCI Proprietary Template Was Found");
    }

    //get application identifier from file control information
    private byte[] getApplicationIdentifier(BerObject fileControlInformation) {
        if (fileControlInformation.type.berTag != 0x6f) throw new IllegalArgumentException("Ber Object is not FCI Template");
        for (BerObject fciProprietaryTemplateCandidate : fileControlInformation.children) {
            if (fciProprietaryTemplateCandidate.type.berTag == 0xa5) {
                //it is FCI proprietary template
                BerObject fciIssuerData = fciProprietaryTemplateCandidate.children.get(0);
                if (fciIssuerData.type.berTag != 0xbf0c) throw new IllegalArgumentException("No FCI Issuer Discretionary Data found");
                BerObject applicationTemplate = fciIssuerData.children.get(0);
                if (applicationTemplate.type.berTag != 0x61) throw new IllegalArgumentException("No Application template found");
                for (BerObject aidCandidate : applicationTemplate.children) {
                    if (aidCandidate.type.berTag == 0x4f) {
                        //wow, it is AID!
                        return aidCandidate.byteArrayValue;
                    }
                }
                throw new IllegalArgumentException("No AID found");
            }
        }
        throw new IllegalArgumentException("No FCI Proprietary Template record is found");
    }

    public CardData readCardData() throws IOException {

        CardData.Builder builder = new CardData.Builder();

        /*
        Firse of all, select 2.PAY.SYS.DDF01 file
         */
        BerObject selectFileResult = selectFile("2PAY.SYS.DDF01");

        /*
        Now, extract application id, and select this application from response
         */
        byte [] applicationId = getApplicationIdentifier(selectFileResult);
        BerObject selectApplicationResult = selectApplication(applicationId);

        try {
            byte [] logEntry = getLogEntry(selectApplicationResult);
            byte sfiNumber = logEntry[0];
            byte sfiNumberOfRecords = logEntry[1];

            //read the log format
            BerObject logFormat = getData(LOG_DATA_FORMAT);
            mLogFormat = getTransactionLogFormat(logFormat);

            GpoDescriptor d = new GpoDescriptor(sfiNumber, (byte) 0, sfiNumberOfRecords);
            for (byte b = 1; b < sfiNumberOfRecords; b++) {
                byte [] data = readGpoData(d, b);
                TransactionLog transactionLog = parseTransactionRecord(data);
                builder.addLogEntry(transactionLog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        Then get processing options
         */
        BerObject gpoResult = getProcessingOptions();
        byte [] afl = getAfl(gpoResult);

        List<GpoDescriptor> gpos = new ArrayList<GpoDescriptor>();
        fillGpoDescriptors(afl, gpos);

        List<Track2Data> suspects = new ArrayList<Track2Data>(5);

        for (GpoDescriptor descriptor : gpos) {
            for (int i = descriptor.recMax; i <= descriptor.recMax; i++) {
                BerObject gpoRecord = readGpoRecord(descriptor, (byte) i);
                if (gpoRecord.type.berTag == 0x70) {
                    //this is EMV Proprietary Template
                    Track2Data track2 = findTrack2Data(gpoRecord);
                    if (track2 != null) suspects.add(track2);
                }
            }
        }

        if (!suspects.isEmpty()) {
            builder.setTrack2(suspects.get(0));
        }

        return builder.build();
    }

}
