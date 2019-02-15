package cryptodezirecash.org.cryptodezirecashwallet.module.wallet;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import cryptodezirecash.org.cryptodezirecashwallet.CryptoDezireCashApplication;
import cryptodezirecash.org.cryptodezirecashwallet.module.CryptoDezireCashContext;
import global.utils.Iso8601Format;
import global.BackupHelper;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by akshaynexus on 6/29/17.
 */

public class WalletBackupHelper implements BackupHelper{

    public File determineBackupFile(String extraData) {
        CryptoDezireCashContext.Files.EXTERNAL_WALLET_BACKUP_DIR.mkdirs();
        checkState(CryptoDezireCashContext.Files.EXTERNAL_WALLET_BACKUP_DIR.isDirectory(), "%s is not a directory", CryptoDezireCashContext.Files.EXTERNAL_WALLET_BACKUP_DIR);

        final DateFormat dateFormat = Iso8601Format.newDateFormat();
        dateFormat.setTimeZone(TimeZone.getDefault());

        String appName = CryptoDezireCashApplication.getInstance().getVersionName();

        for (int i = 0; true; i++) {
            final StringBuilder filename = new StringBuilder(CryptoDezireCashContext.Files.getExternalWalletBackupFileName(appName));
            filename.append('-');
            filename.append(dateFormat.format(new Date()));
            if (extraData!=null){
                filename.append("-"+extraData);
            }
            if (i > 0)
                filename.append(" (").append(i).append(')');

            final File file = new File(CryptoDezireCashContext.Files.EXTERNAL_WALLET_BACKUP_DIR, filename.toString());
            if (!file.exists())
                return file;
        }
    }

}
