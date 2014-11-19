package platformasoft.ru.nfcardexample;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import ru.platformasoft.nfcard.CardData;
import ru.platformasoft.nfcard.EmvClient;
import ru.platformasoft.nfcard.TransactionLog;
import ru.platformasoft.nfcard.android.AbstractEmvClientActivity;

/**
 * Sample nfcard usage activity:
 * reads card track2 and transaction log and displays it
 */
public class NfcardSampleActivity extends AbstractEmvClientActivity {

    private TextView mCardValidThru;
    private TextView mCardPan;
    private ListView mTransactionLog;

    private CardData mCardData;

    private TransactionLogAdapter mLogAdapter;


    private class TransactionLogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCardData.tranactionLog.size();
        }

        @Override
        public Object getItem(int i) {
            return mCardData.tranactionLog.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.row_transaction, null);
            }

            TransactionLog log = mCardData.tranactionLog.get(i);

            TextView tvDate = (TextView) convertView.findViewById(R.id.value_transaction_date);
            TextView tvAmount = (TextView) convertView.findViewById(R.id.value_amount);
            TextView tvType = (TextView) convertView.findViewById(R.id.value_transaction_type);
            TextView tvCurrency = (TextView) convertView.findViewById(R.id.value_currency);

            tvDate.setText(log.day + "." + log.month + "." + log.year);
            tvAmount.setText(String.valueOf(log.amount));
            tvCurrency.setText(log.currency);
            tvType.setText(log.cryptogramInformationData);

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcard_sample);

        mCardValidThru = (TextView) findViewById(R.id.value_card_valid_thru);
        mCardPan = (TextView) findViewById(R.id.value_card_pan);
        mTransactionLog = (ListView) findViewById(R.id.transaction_log);
    }

    @Override
    protected void onCardReadResult(CardData cardData) {
        EmvClient.Track2Data track2 = cardData.track2;
        if (track2 != null) {
            mCardValidThru.setText(track2.month + "/" + track2.year);
            mCardPan.setText(track2.pan);
        }

        mCardData = cardData;
        Log.d("nfc", "Total record logs: " + mCardData.tranactionLog.size());
        mLogAdapter = new TransactionLogAdapter();
        mTransactionLog.setAdapter(mLogAdapter);
    }

    @Override
    protected void onCardReadError(Exception error) {
        error.printStackTrace();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nfcard_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
