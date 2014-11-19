package ru.platformasoft.nfcard.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;

import ru.platformasoft.nfcard.CardData;
import ru.platformasoft.nfcard.EmvClient;
import ru.platformasoft.nfcard.IsoDepDataFeed;
import ru.platformasoft.nfcard.R;

/**
 * Abstract activity that uses EMV client to fetch card data via NFC
 * Created by Sergey Kapustin on 06.10.2014.
 */
public abstract class AbstractEmvClientActivity extends Activity {

    protected NfcAdapter mAdapter;
    private IntentFilter[] mFilters;

    /**
     * This utility method may be used to find out if NFC feature is even
     * provided by hardware
     *
     * @param ctx = any context
     * @return true, if NFC feature is supported by mobile phone hardware
     */
    public static boolean isNfcFeatureAvailable(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) return false;
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Initialize intent filter for foreground dispatch
         */
        IntentFilter filterTagDiscover = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter filterTechDiscover = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        mFilters = new IntentFilter[]{filterTagDiscover, filterTechDiscover};
    }

    @Override
    public void onResume() {
        super.onResume();

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if (!mAdapter.isEnabled()) {
            /*
            In case NFC adapter is not enabled on the device, ask user if he/she wants to
            enable it in settings
             */
            AlertDialog.Builder builder = new AlertDialog.Builder(AbstractEmvClientActivity.this);
            builder.setTitle(R.string.nfc_ask_turn_on);
            builder.setMessage(R.string.nfc_ask_message);
            builder.setPositiveButton(R.string.nfc_ask_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.nfc_ask_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    AbstractEmvClientActivity.this.finish();
                }
            });
            builder.show();
        }

        //Enable the foregrund dispatch for NFC events

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mAdapter.enableForegroundDispatch(this, pendingIntent, mFilters,
                new String[][]{{NfcF.class.getName(),
                        MifareClassic.class.getName(),
                        MifareUltralight.class.getName(),
                        IsoDep.class.getName(),
                        NfcA.class.getName(),
                        NfcB.class.getName(),
                        NfcV.class.getName(),
                        NdefFormatable.class.getName()}});
    }

    @Override
    public void onNewIntent(Intent intent) {
        //ACTION_TECH_DISCOVERED, ACTION_TAG_DISCOVERED mean target case of use: we discovered NFC source
        if (intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED) || intent.getAction()
                .equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            /*
             * nfc tech discovered action. try read data from a card and post
			 * the results
			 */
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // MifareClassic isoDep = MifareClassic.get(tagFromIntent);
            IsoDep isoDep = IsoDep.get(tagFromIntent);
            isoDep.setTimeout(5000);

            try {
                //connect to NFC data feed
                isoDep.connect();

                //initialize data feed, and EMV client
                EmvClient client = new EmvClient(new IsoDepDataFeed(isoDep));

                //read the card data
                CardData cardDataResult = client.readCardData();

                if (cardDataResult != null) {
                    //notify successors about card data read
                    onCardReadResult(cardDataResult);
                } else {
                    //looks, like client gave us no card data, take it as an error
                    throw new NullPointerException("No card data available");
                }
            } catch (Exception e) {
                //in case card read error, notify successors about it
                onCardReadError(e);
            } finally {
                //Finally, consume iso dep connection
                try {
                    isoDep.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This one is called as soon as card data is read through the nfc data feed
     *
     * @param cardData - incoming card data
     */
    protected abstract void onCardReadResult(CardData cardData);

    /**
     * This one is called if no card data is available through the nfc data feed
     *
     * @param error
     */
    protected abstract void onCardReadError(Exception error);
}
