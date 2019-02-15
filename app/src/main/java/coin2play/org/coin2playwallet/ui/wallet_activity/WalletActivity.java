package coin2play.org.coin2playwallet.ui.wallet_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.common.base.Splitter;

import org.coin2playj.core.Coin;
import org.coin2playj.core.Transaction;
import org.coin2playj.uri.SendURI;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chain.BlockchainState;
import coin2play.org.coin2playwallet.R;
import coin2play.org.coin2playwallet.ui.newqrscanner.BarcodeCaptureActivity;
import global.exceptions.NoPeerConnectedException;
import global.Coin2PlayRate;
import coin2play.org.coin2playwallet.ui.base.BaseDrawerActivity;
import coin2play.org.coin2playwallet.ui.base.dialogs.SimpleTextDialog;
import coin2play.org.coin2playwallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import coin2play.org.coin2playwallet.ui.qr_activity.QrActivity;
import coin2play.org.coin2playwallet.ui.settings_backup_activity.SettingsBackupActivity;
import coin2play.org.coin2playwallet.ui.transaction_request_activity.RequestActivity;
import coin2play.org.coin2playwallet.ui.transaction_send_activity.SendActivity;
import coin2play.org.coin2playwallet.ui.upgrade.UpgradeWalletActivity;
import coin2play.org.coin2playwallet.utils.AnimationUtils;
import coin2play.org.coin2playwallet.utils.DialogsUtil;
import coin2play.org.coin2playwallet.utils.scanner.ScanActivity;

import static android.Manifest.permission.CAMERA;
import static coin2play.org.coin2playwallet.service.IntentsConstants.ACTION_NOTIFICATION;
import static coin2play.org.coin2playwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static coin2play.org.coin2playwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static coin2play.org.coin2playwallet.ui.transaction_send_activity.SendActivity.INTENT_ADDRESS;
import static coin2play.org.coin2playwallet.ui.transaction_send_activity.SendActivity.INTENT_EXTRA_TOTAL_AMOUNT;
import static coin2play.org.coin2playwallet.ui.transaction_send_activity.SendActivity.INTENT_MEMO;
import static coin2play.org.coin2playwallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by Neoperol on 5/11/17.
 */

public class WalletActivity extends BaseDrawerActivity {

    private static final int SCANNER_RESULT = 122;

    private View root;
    private View container_txs;

    private TextView txt_value;
    private TextView txt_unnavailable;
    private TextView txt_local_currency;
    private TextView txt_watch_only;
    private View view_background;
    private View container_syncing;
    private Coin2PlayRate coin2playRate;
    private TransactionsFragmentBase txsFragment;
    private static final int RC_BARCODE_CAPTURE = 9001;

    // Receiver
    private LocalBroadcastManager localBroadcastManager;

    private IntentFilter coin2playServiceFilter = new IntentFilter(ACTION_NOTIFICATION);
    private BroadcastReceiver coin2playServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_NOTIFICATION)){
                if(intent.getStringExtra(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_ON_COIN_RECEIVED)){
                    // Check if the app is on foreground to update the view.
                    if (!isOnForeground)return;
                    updateBalance();
                    txsFragment.refresh();
                }
            }

        }
    };

    @Override
    protected void beforeCreate(){
        /*
        if (!appConf.isAppInit()){
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            finish();
        }
        // show report dialog if something happen with the previous process
        */
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        setTitle(R.string.my_wallet);
        root = getLayoutInflater().inflate(R.layout.fragment_wallet, container);
        View containerHeader = getLayoutInflater().inflate(R.layout.fragment_coin2play_amount,header_container);
        header_container.setVisibility(View.VISIBLE);
        txt_value = (TextView) containerHeader.findViewById(R.id.pivValue);
        txt_unnavailable = (TextView) containerHeader.findViewById(R.id.txt_unnavailable);
        container_txs = root.findViewById(R.id.container_txs);
        txt_local_currency = (TextView) containerHeader.findViewById(R.id.txt_local_currency);
        txt_watch_only = (TextView) containerHeader.findViewById(R.id.txt_watch_only);
        view_background = root.findViewById(R.id.view_background);
        container_syncing = root.findViewById(R.id.container_syncing);
        // Open Send
        root.findViewById(R.id.fab_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coin2playModule.isWalletWatchOnly()){
                    Toast.makeText(v.getContext(),R.string.error_watch_only_mode,Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(v.getContext(), SendActivity.class));
            }
        });
        root.findViewById(R.id.fab_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), RequestActivity.class));
            }
        });

        FloatingActionMenu floatingActionMenu = (FloatingActionMenu) root.findViewById(R.id.fab_menu);
        floatingActionMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened){
                    AnimationUtils.fadeInView(view_background,200);
                }else {
                    AnimationUtils.fadeOutGoneView(view_background,200);
                }
            }
        });

        txsFragment = (TransactionsFragmentBase) getSupportFragmentManager().findFragmentById(R.id.transactions_fragment);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        setNavigationMenuItemChecked(0);

        init();

        // register
        localBroadcastManager.registerReceiver(coin2playServiceReceiver,coin2playServiceFilter);

        updateState();
        updateBalance();

        // check if this wallet need an update:
        try {
            if(coin2playModule.isBip32Wallet() && coin2playModule.isSyncWithNode()){
                if (!coin2playModule.isWalletWatchOnly() && coin2playModule.getAvailableBalanceCoin().isGreaterThan(Transaction.DEFAULT_TX_FEE)) {
                    Intent intent = UpgradeWalletActivity.createStartIntent(
                            this,
                            getString(R.string.upgrade_wallet),
                            "An old wallet version with bip32 key was detected, in order to upgrade the wallet your coins are going to be sweeped" +
                                    " to a new wallet with bip44 account.\n\nThis means that your current mnemonic code and" +
                                    " backup file are not going to be valid anymore, please write the mnemonic code in paper " +
                                    "or export the backup file again to be able to backup your coins." +
                                    "\n\nPlease wait and not close this screen. The upgrade + blockchain sychronization could take a while."
                                    +"\n\nTip: If this screen is closed for user's mistake before the upgrade is finished you can find two backups files in the 'Download' folder" +
                                    " with prefix 'old' and 'upgrade' to be able to continue the restore manually."
                                    + "\n\nThanks!",
                            "sweepBip32"
                    );
                    startActivity(intent);
                }
            }
        } catch (NoPeerConnectedException e) {
            e.printStackTrace();
        }
    }

    private void updateState() {
        txt_watch_only.setVisibility(coin2playModule.isWalletWatchOnly()?View.VISIBLE:View.GONE);
    }

    private void init() {
        // Start service if it's not started.
        coin2playApplication.startCoin2PlayService();

        if (!coin2playApplication.getAppConf().hasBackup()){
            long now = System.currentTimeMillis();
            if (coin2playApplication.getLastTimeRequestedBackup()+1800000L<now) {
                coin2playApplication.setLastTimeBackupRequested(now);
                SimpleTwoButtonsDialog reminderDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                        this,
                        getString(R.string.reminder_backup),
                        getString(R.string.reminder_backup_body),
                        new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                            @Override
                            public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                                startActivity(new Intent(WalletActivity.this, SettingsBackupActivity.class));
                                dialog.dismiss();
                            }

                            @Override
                            public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                                dialog.dismiss();
                            }
                        }
                );
                reminderDialog.setLeftBtnText(getString(R.string.button_dismiss));
                reminderDialog.setLeftBtnTextColor(Color.BLACK);
                reminderDialog.setRightBtnText(getString(R.string.button_ok));
                reminderDialog.show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // unregister
        //localBroadcastManager.unregisterReceiver(localReceiver);
        localBroadcastManager.unregisterReceiver(coin2playServiceReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_qr){
            startActivity(new Intent(this, QrActivity.class));
            return true;
        }else if (item.getItemId()==R.id.action_scan){
            if (!checkPermission(CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permsRequestCode = 200;
                    String[] perms = {"android.permission.CAMERA"};
                    requestPermissions(perms, permsRequestCode);
                }
            }
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
            intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Create a list of Data objects
    public List<TransactionData> fill_with_data() {

        List<TransactionData> data = new ArrayList<>();

        data.add(new TransactionData("Sent Coin2Play", "18:23", R.mipmap.ic_transaction_receive,"56.32", "701 USD" ));
        data.add(new TransactionData("Sent Coin2Play", "1 days ago", R.mipmap.ic_transaction_send,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Coin2Play", "2 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Coin2Play", "2 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Coin2Play", "3 days ago", R.mipmap.ic_transaction_send,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Coin2Play", "3 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));

        data.add(new TransactionData("Sent Coin2Play", "4 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Coin2Play", "4 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Coin2Play", "one week ago", R.mipmap.ic_transaction_send,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Coin2Play", "one week ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Coin2Play", "one week ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Coin2Play", "one week ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD" ));

        return data;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE){
            if (resultCode== CommonStatusCodes.SUCCESS) {
                try {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String address = barcode.displayValue;
                    final String usedAddress;
                    String bitcoinUrl = address;
                    String uri = bitcoinUrl;
                    String query = uri.split("\\?")[1];
                    final Map<String, String> map = Splitter.on('&').trimResults().withKeyValueSeparator("=").split(query);

//                    String tempstr =
                    String addresss = bitcoinUrl.replaceAll("coin2play:(.*)\\?.*", "$1");
                    final String amounta = map.get("amount");
                    final  String label = map.get("label");
                    Log.i("addressAA", "Map: " + map);





                        Log.i("addressAA", "Scanned Address is : " + address);

//                     SendURI coin2playUri = new SendURI(addresss);
                        usedAddress = addresss;
                    if ( bitcoinUrl.toLowerCase().contains("amount") || bitcoinUrl.toLowerCase().contains("amount") && bitcoinUrl.toLowerCase().contains("label")
                            ) {
                        final Coin amount = Coin.parseCoin(amounta);
                        if (amount != null && Integer.parseInt(amounta) > 0){
                            final String memo = label.replaceAll("%20","");
                            StringBuilder text = new StringBuilder();
                            text.append(getString(R.string.amount)).append(": ").append(amount.toFriendlyString());
                            if (memo != null){
                                text.append("\n").append(getString(R.string.description)).append(": ").append(memo);
                            }

                            SimpleTextDialog dialogFragment = DialogsUtil.buildSimpleTextDialog(this,
                                    getString(R.string.payment_request_received),
                                    text.toString())
                                    .setOkBtnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(v.getContext(), SendActivity.class);
                                            intent.putExtra(INTENT_ADDRESS,usedAddress);
                                            intent.putExtra(INTENT_EXTRA_TOTAL_AMOUNT,amount);
                                            intent.putExtra(INTENT_MEMO,memo);
                                            startActivity(intent);
                                        }
                                    });
                            dialogFragment.setImgAlertRes(R.drawable.ic_send_action);
                            dialogFragment.setAlignBody(SimpleTextDialog.Align.LEFT);
                            dialogFragment.setImgAlertRes(R.drawable.ic_fab_recieve);
                            dialogFragment.show(getFragmentManager(),"payment_request_dialog");
                           // DialogsUtil.showCreateAddressLabelDialog(this,usedAddress);
                            return;
                        }

                    }
                    else{
                        DialogsUtil.showCreateAddressLabelDialog(this,usedAddress);

                    }
                        //final Coin amount = Coin.parseCoin(amounta);





                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this,"Bad address",Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),permission);

        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void updateBalance() {
        Coin availableBalance = coin2playModule.getAvailableBalanceCoin();
        txt_value.setText(!availableBalance.isZero()?availableBalance.toFriendlyString():"0 C2P");
        Coin unnavailableBalance = coin2playModule.getUnnavailableBalanceCoin();
        txt_unnavailable.setText(!unnavailableBalance.isZero()?unnavailableBalance.toFriendlyString():"0 C2P");
        if (coin2playRate == null)
            coin2playRate = coin2playModule.getRate(coin2playApplication.getAppConf().getSelectedRateCoin());
        if (coin2playRate!=null) {
            txt_local_currency.setText(
                    coin2playApplication.getCentralFormats().format(
                            new BigDecimal(availableBalance.getValue() * coin2playRate.getRate().doubleValue()).movePointLeft(8)
                    )
                    + " "+coin2playRate.getCode()
            );
        }else {
            txt_local_currency.setText("0");
        }
    }

    @Override
    protected void onBlockchainStateChange(){
        if (blockchainState == BlockchainState.SYNCING){
            AnimationUtils.fadeInView(container_syncing,500);
        }else if (blockchainState == BlockchainState.SYNC){
            AnimationUtils.fadeOutGoneView(container_syncing,500);
        }else if (blockchainState == BlockchainState.NOT_CONNECTION){
            AnimationUtils.fadeInView(container_syncing,500);
        }
    }
}
