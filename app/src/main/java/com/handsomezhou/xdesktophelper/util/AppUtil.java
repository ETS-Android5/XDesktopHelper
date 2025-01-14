
package com.handsomezhou.xdesktophelper.util;

import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.handsomezhou.xdesktophelper.R;
import com.handsomezhou.xdesktophelper.helper.AppInfoHelper;
import com.handsomezhou.xdesktophelper.helper.SettingsHelper;
import com.handsomezhou.xdesktophelper.model.AppInfo;
import com.handsomezhou.xdesktophelper.model.database.AppStartRecord;
import com.handsomezhou.xdesktophelper.util.database.AppStartRecordUtil;

public class AppUtil {
    private static final String TAG = "AppUtil";

    /**
     * Return true when start app success,otherwise return false.
     * 
     * @param context
     * @param packageName
     * @return
     */
    public static boolean startApp(Context context, String packageName) {
        boolean startAppSuccess = false;
        do {
            if ((null == context) || TextUtils.isEmpty(packageName)) {
                break;
            }

            PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);

            if (null != intent) {
                context.startActivity(intent);
                startAppSuccess = true;
            }
        } while (false);

        return startAppSuccess;
    }

    /**
     * @param context
     * @param packageName
     * @param cls
     * @return
     */

    public static boolean startApp(Context context, String packageName, String cls) {
        boolean startAppSuccess = false;
        do {
            if ((null == context) || TextUtils.isEmpty(packageName)) {
                break;
            }
            ComponentName componet = new ComponentName(packageName, cls);
            Intent intent = createLaunchIntent(componet);
            if (context.getPackageManager().getLaunchIntentForPackage(packageName) != null) {
                try{
                    context.startActivity(intent);
                }catch (Exception ex){
                    LogUtil.i(TAG,ex.toString());
                }

                startAppSuccess = true;
            }
            else {
                System.out.println("app not found");
            }
        } while (false);

        return startAppSuccess;
    }

    public static Intent createLaunchIntent(ComponentName componentName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
    }

    /**
     * start app via appinfo
     * 
     * @param context
     * @param appInfo
     */
    public static boolean startApp(Context context, AppInfo appInfo) {
        boolean startAppSuccess = false;
        do {
            if ((null == context) || (null == appInfo)) {
                startAppSuccess = false;
                break;

            }

            if (null != appInfo) {

                if (!appInfo.getPackageName().equals(context.getPackageName())) {
                    startAppSuccess = AppUtil.startApp(context, appInfo.getPackageName(), appInfo.getName());
                    if (false == startAppSuccess) {
                        startAppSuccess=AppUtil.startApp(context,appInfo.getPackageName());
                        if(false==startAppSuccess) {
                            Toast.makeText(context, R.string.app_can_not_be_launched_directly, Toast.LENGTH_SHORT).show();
                            AppInfoHelper.getInstance().setAppInfoChanged(true);
                        }
                    } else {
                        if (true == SettingsHelper.getInstance().isSmartSorting()) {
                            long startTimeMs = System.currentTimeMillis();
                            AppStartRecord appStartRecord = new AppStartRecord(appInfo.getKey(), startTimeMs);
                            appStartRecord.setUpdateTime(TimeUtil.getLogTime());
                            AppStartRecordUtil.update(appStartRecord);
                            AppInfo ai = AppInfoHelper.getInstance().getBaseAllAppInfosHashMap()
                                    .get(appInfo.getKey());
                            if (null != ai) {
                                ai.setCommonWeights(ai.getCommonWeights()
                                        + AppCommonWeightsUtil.getCommonWeights(startTimeMs));
                                Log.i(TAG, ai.getPackageName() + ":" + ai.getCommonWeights());
                                Collections.sort(AppInfoHelper.getInstance().getBaseAllAppInfos(),
                                        AppInfo.mSortByDefault);

                            }
                        }

                    }
                } else {
                    Toast.makeText(context, R.string.the_app_has_been_launched, Toast.LENGTH_SHORT)
                            .show();
                }

            }
        } while (false);

        return startAppSuccess;
    }

    /**
     * whether app can Launch the main activity. Return true when can Launch,otherwise return false.
     * 
     * @param context
     * @param packageName
     * @return
     */
    public static boolean appCanLaunchTheMainActivity(Context context, String packageName) {
        boolean canLaunchTheMainActivity = false;
        do {
            if ((null == context) || TextUtils.isEmpty(packageName)) {
                break;
            }

            PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            canLaunchTheMainActivity = (null == intent) ? (false) : (true);
        } while (false);

        return canLaunchTheMainActivity;
    }

    /**
     * uninstall app via package name
     * 
     * @param context
     * @param packageName
     */
    public static void uninstallApp(Context context, String packageName) {
        Uri packageUri = Uri.parse("package:" + packageName);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DELETE);
        intent.setData(packageUri);
        context.startActivity(intent);
    }

    /**
     * uninstall app via appInfo
     * 
     * @param context
     * @param appInfo
     */
    public static void uninstallApp(Context context, AppInfo appInfo) {
        if ((null == context) || (null == appInfo)) {
            return;
        }

        if (null != appInfo)
        {
            if (!appInfo.getPackageName().equals(context.getPackageName())) {
                AppUtil.uninstallApp(context, appInfo.getPackageName());
            } else {
                Toast.makeText(context, R.string.can_not_to_uninstall_yourself, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void viewApp(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        intent.putExtra("cmp", "com.android.settings/.applications.InstalledAppDetails");
        intent.addCategory("android.intent.category.DEFAULT");
        context.startActivity(intent);
    }

    public static void viewApp(Context context, AppInfo appInfo) {
        if ((null == context) || (null == appInfo)) {
            return;
        }

        if (null != appInfo)
        {
            AppUtil.viewApp(context, appInfo.getPackageName());

        }
    }

    /**
     * get version name via package name
     * 
     * @param context
     * @param packageName
     * @return
     */
    public static String getVersionName(Context context, String packageName) {
        String versionName = null;
        do {
            if ((null == context) || TextUtils.isEmpty(packageName)) {
                break;
            }
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo(packageName, 0);
                versionName = pi.versionName;
            } catch (NameNotFoundException e) {

                e.printStackTrace();
                break;
            }

        } while (false);

        return versionName;
    }

    /**
     *
     * @param context
     * @param packageName
     * @return app exist return true,otherwise return false.
     */
    public static boolean isAppExist(Context context,String packageName) {
        boolean appExist=false;
        do{
            if(TextUtils.isEmpty(packageName)){
                break;
            }

            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
            for (PackageInfo pi : packageInfos) {
                if (packageName.equalsIgnoreCase(pi.packageName)){
                    appExist=true;
                    break;
                }
            }

        }while(false);

        return  appExist;
    }

    public static ApplicationInfo getApplicationInfo(Context context, String packageName){
        ApplicationInfo applicationInfo=null;

        PackageInfo packageInfo=AppUtil.getPackageInfo(context,packageName);
        if(null!=packageInfo){
            applicationInfo=packageInfo.applicationInfo;
        }

        return applicationInfo;
    }

    public static PackageInfo getPackageInfo(Context context,String packageName){
        PackageInfo packageInfo=null;

        do{
            if(null==context){
                break;
            }

            if(CommonUtil.isEmpty(packageName)){
                break;
            }

            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
            for (PackageInfo pi : packageInfos) {
                if (packageName.equalsIgnoreCase(pi.packageName)){
                    packageInfo=pi;
                    break;
                }
            }

        }while (false);

        return packageInfo;
    }

    /**
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getAppName(Context context,String packageName){
        String appName=null;
        do{
            PackageInfo packageInfo=AppUtil.getPackageInfo(context,packageName);
            if(null==packageInfo){
                break;
            }

            PackageManager pm = context.getPackageManager();
            appName = String.valueOf(pm.getApplicationLabel(packageInfo.applicationInfo));
            if(false==CommonUtil.isEmpty(appName)){
                break;
            }

            appName = packageInfo.packageName;

        }while (false);

        return appName;
    }
}
