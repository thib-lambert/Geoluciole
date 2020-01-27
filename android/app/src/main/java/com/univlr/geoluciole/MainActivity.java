package com.univlr.geoluciole;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.icu.text.UnicodeSet;
import android.location.Location;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hypertrack.hyperlog.HyperLog;
import com.univlr.geoluciole.adapter.ViewPagerAdapter;
import com.univlr.geoluciole.location.LocationUpdatesService;
import com.univlr.geoluciole.location.Utils;
import com.univlr.geoluciole.model.FormModel;
import com.univlr.geoluciole.model.badge.BadgeManager;
import com.univlr.geoluciole.permissions.Permission;
import com.univlr.geoluciole.sender.HttpProvider;
import com.univlr.geoluciole.ui.achievements.AchievementsFragment;
import com.univlr.geoluciole.ui.achievements.BadgeListFragment;
import com.univlr.geoluciole.ui.home.HomeFragment;
import com.univlr.geoluciole.ui.preferences.PreferencesFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends LocationActivity implements AchievementsFragment.OnFragmentInteractionListener, BadgeListFragment.OnFragmentInteractionListener {
    public static final String PREFERENCES = "Saved_Pref";
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
    };

    private LocationUpdatesService mService = null;
    private boolean mBound = false;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            // checking permissions
            ArrayList<Permission> unauthorizedPermissions = retrieveUnauthorizedPermissions();
            if(!unauthorizedPermissions.isEmpty()) {
                requestPermissions(unauthorizedPermissions);
                if (!unauthorizedPermissions.contains(Permission.FINE_LOCATION_PERMISSION)) {
                    enableGPSIfNeeded();
                }
            } else {
                enableGPSIfNeeded();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private BottomNavigationView navView;
    private MenuItem prevMenuItem;
    private ViewPager viewPager;

    private HomeFragment homeFragment;
    private AchievementsFragment dashboardFragment;
    private PreferencesFragment notificationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FormModel form = (FormModel) getIntent().getSerializableExtra("Form");
        System.out.println("Main Activity form retrieved : " + form);
        super.onCreate(savedInstanceState);

        checkPowerSavingMode();
        checkConstructorLayer();

        // temporary receiver
        myReceiver = new MyReceiver();

        // UI
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        prevMenuItem = null;
        viewPager = findViewById(R.id.viewpager);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        viewPager.setCurrentItem(0);
                        try {
                            HomeFragment fragment = (HomeFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem());
                            fragment.updateProgressBar();
                            fragment.updateSwitch(mService);

                        } catch (NullPointerException npe) {
                            //do nothing
                        }
                        break;
                    case R.id.navigation_achievements:
                        viewPager.setCurrentItem(1);
                        FragmentTransaction fragmentTransaction = MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.badgeList_fragment_container,
                                new BadgeListFragment());

                        fragmentTransaction.commit();
                        break;
                    case R.id.navigation_dashboard:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // do nothing
            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navView.getMenu().getItem(0).setChecked(false);
                }
                if (position == 0) {
                    try {
                        HomeFragment homeFragment = (HomeFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem());
                        homeFragment.updateLastBadgeView();
                    } catch (NullPointerException np) {
                        Log.i(TAG, np.getMessage());
                    }
                }
                if (position == 1) {
                    try {
                        FragmentTransaction fragmentTransaction = MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.badgeList_fragment_container,
                                new BadgeListFragment());

                        fragmentTransaction.commit();
                    } catch (NullPointerException np) {
                        Log.i(TAG, np.getMessage());
                    }
                }
                Log.d(TAG, "onPageSelected: " + position);
                navView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navView.getMenu().getItem(position);
                if (position == 0) {
                    try {
                        HomeFragment fragment = (HomeFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem());
                        fragment.updateProgressBar();
                        //fragment.updateSwitch(mService);
                    } catch (NullPointerException npe) {
                        //do nothing
                    }
                } else if(position == 1){
                    try {
                        AchievementsFragment fragment = (AchievementsFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem());
                        fragment.updateDistance();
                    } catch (NullPointerException npe){
                        // do nohing
                    }
                }
            }


            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        });
        
           UserPreferences userPreferences = UserPreferences.getInstance(this);
        if (userPreferences.isGpsConsent()) {
            HttpProvider.activePeriodicSend(this);
            //todo ligne suivante de test
            File folder = new File(this.getFilesDir() + "/Log");
            if(!folder.exists()) {
                folder.mkdir();
            }
            String filename = this.getFilesDir() + "/Log/" +"gps_log.log";
            try {
                FileWriter fw = new FileWriter(filename);
                fw.append("Creation file \n");
                fw.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
            File filelog = HyperLog.getDeviceLogsInFile(this, true);
            try {
                FileInputStream fin = new FileInputStream(filelog);
                String content = MainActivity.convertStreamToString(fin);
                //Make sure you close all streams.
                fin.close();

                File f = new File(filename);
                FileOutputStream fos = new FileOutputStream(f, true);
                fos.write(content.getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // todo fin de ligne de test
        }
        
        setupViewPager(viewPager);
    }
    
        //todo fonction de test
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
    
    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        homeFragment=new HomeFragment();
        dashboardFragment =new AchievementsFragment();
        notificationsFragment = new PreferencesFragment();
        adapter.addFragment(homeFragment);
        adapter.addFragment(dashboardFragment);
        adapter.addFragment(notificationsFragment);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }


    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onGPSEnabled() {
        mService.requestLocationUpdates();

    }

    @Override
    public void messageFromParentFragment(Uri uri) {
        // do nothing
    }

    private void checkPowerSavingMode() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && powerManager.isPowerSaveMode()) {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (powerManager.isIgnoringBatteryOptimizations(getPackageName()))
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                else {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                }
            } else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
            }
            startActivity(intent);
        }
    }

    private void checkConstructorLayer() {
        for (Intent intent : POWERMANAGER_INTENTS) {
            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                startActivity(intent);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(MainActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public ViewPagerAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // do nothing
    }
}
