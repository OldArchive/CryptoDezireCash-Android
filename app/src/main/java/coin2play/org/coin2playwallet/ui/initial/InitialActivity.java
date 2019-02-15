package coin2play.org.coin2playwallet.ui.initial;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import coin2play.org.coin2playwallet.Coin2PlayApplication;
import coin2play.org.coin2playwallet.ui.splash_activity.SplashActivity;
import coin2play.org.coin2playwallet.ui.wallet_activity.WalletActivity;
import coin2play.org.coin2playwallet.utils.AppConf;

/**
 * Created by akshaynexus on 8/19/17.
 */

public class InitialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Coin2PlayApplication coin2playApplication = Coin2PlayApplication.getInstance();
        AppConf appConf = coin2playApplication.getAppConf();
        // show report dialog if something happen with the previous process
        Intent intent;
        if (!appConf.isAppInit() || appConf.isSplashSoundEnabled()){
            intent = new Intent(this, SplashActivity.class);
        }else {
            intent = new Intent(this, WalletActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
