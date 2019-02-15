package coin2play.org.coin2playwallet.ui.base;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import coin2play.org.coin2playwallet.Coin2PlayApplication;
import global.Coin2PlayModule;

/**
 * Created by akshaynexus on 6/29/17.
 */

public class BaseFragment extends Fragment {

    protected Coin2PlayApplication coin2playApplication;
    protected Coin2PlayModule coin2playModule;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        coin2playApplication = Coin2PlayApplication.getInstance();
        coin2playModule = coin2playApplication.getModule();
    }

    protected boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getActivity(),permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
