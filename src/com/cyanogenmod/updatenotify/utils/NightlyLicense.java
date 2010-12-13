package com.cyanogenmod.updatenotify.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class NightlyLicense {
    private PackageManager mPM;
    
    private static final String TAG = "CMUpdateNotify-NightlyLicense";
    private static final String PNAME = "com.koushikdutta.rommanager.license";
    private static final String SIGNATURE = "3082025f308201c8a0030201020204491395d0300d06092a864886f70d01010505003074310b3009060355040613025553310b30090603550408130257413110300e0603550407130753656174746c6531193017060355040a13106b6f757368696b64757474612e636f6d31133011060355040b130a436c6f636b776f726b73311630140603550403130d4b6f757368696b204475747461301e170d3038313130373031313134345a170d3336303332353031313134345a3074310b3009060355040613025553310b30090603550408130257413110300e0603550407130753656174746c6531193017060355040a13106b6f757368696b64757474612e636f6d31133011060355040b130a436c6f636b776f726b73311630140603550403130d4b6f757368696b20447574746130819f300d06092a864886f70d010101050003818d0030818902818100aaf2b6b9761485ad060146da376b67faceefd781bddf4ba92b0ede2d73edf1c59011e4637a9311b2c1640927b80e95ee4154dcfd2f86ff8eb4a295534d29ccb4f55d7e7e69eb20115fddc2edb422e75160c6b333a2ce05c35b67097ab14ca0ff2f63f2ecd64d9f4e4217a739cfaa0413f82b802e75992d649a157c55fc6b3a6b0203010001300d06092a864886f70d010105050003818100aaef343a4c39cabb750c1fc3c7121ac35be1023d6f80a71a48760c5345c1d9e0c8d2c20dffe1b3ed7089717964e1c6fcc122ef444ecf96b3cc448a43733807b48e409a9ca792458704d44984aab356833cfff51bab702bc371c14bf5cd43003cde3a91708364fc397535558c0009b7f033091119440129e64a31a8f15997d52d";

    public NightlyLicense(Context ctx) {
        this.mPM = ctx.getPackageManager();
    }

    public boolean validLicense() {
        boolean valid = false;
        try {
            PackageInfo pkginfo = mPM.getPackageInfo(PNAME, PackageManager.GET_SIGNATURES);
            String signature = pkginfo.signatures[0].toCharsString();
            if (signature.equals(SIGNATURE)) {
                valid = true;
            }
        } catch (NameNotFoundException e) {
        }
        
        Log.d(TAG, "validLicense() = " + valid);
        return valid;
    }
}
