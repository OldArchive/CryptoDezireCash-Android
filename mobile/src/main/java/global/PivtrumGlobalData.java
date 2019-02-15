package global;

import java.util.ArrayList;
import java.util.List;

import pivtrum.PivtrumPeerData;

/**
 * Created by akshaynexus on 7/2/17.
 */

public class PivtrumGlobalData {

    public static final String FURSZY_TESTNET_SERVER = "45.76.139.85";

    public static final String[] TRUSTED_NODES = new String[]{"45.76.139.85","167.99.183.19","45.77.134.98","199.247.17.25","80.240.31.120"};

    public static final List<PivtrumPeerData> listTrustedHosts(){
        List<PivtrumPeerData> list = new ArrayList<>();
        list.add(new PivtrumPeerData(FURSZY_TESTNET_SERVER,8443,55552));
        for (String trustedNode : TRUSTED_NODES) {
            list.add(new PivtrumPeerData(trustedNode,2221,55552));
        }
        return list;
    }

}
