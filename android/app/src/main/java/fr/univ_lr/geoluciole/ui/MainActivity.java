/*
 * Copyright (c) 2020, Martin Allusse and Alexandre Baret and Jessy Barritault and Florian
 * Bertonnier and Lisa Fougeron and François Gréau and Thibaud Lambert and Antoine
 * Orgerit and Laurent Rayez
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *  Neither the name of the copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fr.univ_lr.geoluciole.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import fr.univ_lr.geoluciole.R;
import fr.univ_lr.geoluciole.location.LocationUpdatesService;
import fr.univ_lr.geoluciole.model.UserPreferences;
import fr.univ_lr.geoluciole.permissions.Permission;
import fr.univ_lr.geoluciole.ui.achievements.AchievementsFragment;
import fr.univ_lr.geoluciole.ui.achievements.BadgeListFragment;
import fr.univ_lr.geoluciole.ui.home.HomeFragment;
import fr.univ_lr.geoluciole.ui.preferences.PreferencesFragment;
import fr.univ_lr.geoluciole.utils.adapter.ViewPagerAdapter;


public class MainActivity extends LocationActivity implements AchievementsFragment.OnFragmentInteractionListener, BadgeListFragment.OnFragmentInteractionListener {
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

    // position page parametres
    private static final int ITEM_PARAM_PAGE = 2;

    // service permettant la récupération des données GPS
    private LocationUpdatesService mService = null;
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            HomeFragment homeFragment = getHomeFragment();
            if (homeFragment != null) {
                homeFragment.updateSwitch();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserPreferences userPreferences = UserPreferences.getInstance(this);
        Intent intent = getIntent();
        boolean refused_consent = intent.getBooleanExtra("refused_consent", false);

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
                        HomeFragment fragment = getHomeFragment();
                        if (fragment != null) {
                            fragment.updateProgressBar();
                            fragment.updateSwitch();
                        }
                        break;
                    case R.id.navigation_achievements:
                        viewPager.setCurrentItem(1);
                        AchievementsFragment childfragment = (AchievementsFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(1);
                        childfragment.updateDistance();
                        childfragment.updateView();
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
                    HomeFragment fragment = getHomeFragment();
                    if (fragment != null) {
                        fragment.updateProgressBar();
                        fragment.updateLastBadgeView();
                    }
                }
                if (position == 1) {
                    try {
                        AchievementsFragment fragment = (AchievementsFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(1);
                        fragment.updateDistance();
                        fragment.updateView();
                    } catch (NullPointerException np) {
                        Log.i(TAG, np.getMessage());
                    }
                }
                Log.d(TAG, "onPageSelected: " + position);
                navView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navView.getMenu().getItem(position);
            }


            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        });

        if (userPreferences.isGpsConsent()) {
            //checkPowerSavingMode();
            if (!userPreferences.isManagerPermissionConstructorShow()) {
                checkConstructorLayer();
            }

            if (!userPreferences.isManagerPermissionBatteryShow()) {
                checkPowerSavingMode();
            }
        }

        setupViewPager(viewPager);

        // si on refuse le consentement, on est redirigé vers la vue params
        if (refused_consent) {
            changePage(ITEM_PARAM_PAGE);
        }
    }

    /**
     * Permet de changer la vue
     * @param item
     */
    private void changePage(int item) {
        int old_pos = viewPager.getCurrentItem();
        viewPager.setCurrentItem(item);
        prevMenuItem = navView.getMenu().getItem(old_pos);
        prevMenuItem.setChecked(false);
        navView.getMenu().getItem(item).setChecked(true);
    }

    /**
     * Check les permissions
     */
    private void checkPermission() {
        ArrayList<Permission> unauthorizedPermissions = retrieveUnauthorizedPermissions();
        if (!unauthorizedPermissions.isEmpty()) {
            requestPermissions(unauthorizedPermissions);
            if (!unauthorizedPermissions.contains(Permission.FINE_LOCATION_PERMISSION)) {
                enableGPSIfNeeded();
            }
        } else {
            enableGPSIfNeeded();
        }
    }

    /**
     * Set up du view pager
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        // liste fragments utilisés par le viewPager
        HomeFragment homeFragment = new HomeFragment();
        AchievementsFragment dashboardFragment = new AchievementsFragment();
        PreferencesFragment notificationsFragment = new PreferencesFragment();
        adapter.addFragment(homeFragment);
        adapter.addFragment(dashboardFragment);
        adapter.addFragment(notificationsFragment);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (UserPreferences.getInstance(this).isGpsConsent()) {
            // checking permissions
            checkPermission();

            // Bind to the service. If the service is in foreground mode, this signals to the service
            // that since this activity is in the foreground, the service can exit foreground mode.
            bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
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
        // Mise à jour des autorisations de récupération des données GPS
        UserPreferences userPreferences = UserPreferences.getInstance(this);
        if (!userPreferences.isGpsAutorize()) {
            userPreferences.setSendData(true);
            userPreferences.setGpsAutorize(true);
            userPreferences.store(this);
        }

        // Acutalisation du switch + démarage des services
        HomeFragment homeFragment = getHomeFragment();
        if (homeFragment != null) {
            homeFragment.updateSwitch();
        }
    }

    @Override
    public void messageFromParentFragment(Uri uri) {
        // do nothing
    }

    /**
     * Récupère le fragment HomeFragment.
     * Attention le retour peut être null, vérifier avant.
     *
     * @return HomeFragment || null
     */
    private HomeFragment getHomeFragment() {
        try {
            HomeFragment fragment = (HomeFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(0);
            return fragment;
        } catch (NullPointerException npe) {
            //do nothing
        }

        return null;
    }

    /**
     * Get le binder pour binder le service avec l'activité
     * @return
     */
    public LocationUpdatesService getmService() {
        return mService;
    }

    /**
     * Check permission lié à la batterie
     */
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
            UserPreferences userPreferences = UserPreferences.getInstance(this);
            userPreferences.setManagerPermissionBatteryShow(true);
            userPreferences.store(this);
            startActivity(intent);
        }
    }

    /**
     *  check les options du constructeur
     */
    private void checkConstructorLayer() {
        for (Intent intent : POWERMANAGER_INTENTS) {
            ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null) {
                showDialogConstructor(intent);
                break;
            }
        }
    }

    /**
     * Montre un fenêtre de dialogue.
     * @param intent
     */
    private void showDialogConstructor(final Intent intent) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title_constructor_settings)
                .setMessage(R.string.alert_content_constructor_settings)
                .setPositiveButton(R.string.action_validate, null) //Set to null. We override the onclick
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button buttonValidate = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                buttonValidate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UserPreferences userPreferences = UserPreferences.getInstance(MainActivity.this);
                        userPreferences.setManagerPermissionConstructorShow(true);
                        userPreferences.store(MainActivity.this);
                        try {
                            startActivity(intent);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        } catch (SecurityException se) {
                            // do nothing
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // do nothing
    }
}
