package com.dsht.kerneltweaker.fragments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dsht.kerneltweaker.CustomCheckBoxPreference;
import com.dsht.kerneltweaker.CustomListPreference;
import com.dsht.kerneltweaker.CustomPreference;
import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.ListViewMultiChoiceModeListener;
import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;
import com.dsht.settings.SettingsFragment;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ListView;

public class KernelPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {

	private CustomCheckBoxPreference mKernelFsync;
	private CustomCheckBoxPreference mKernelDynFsync;
	private CustomCheckBoxPreference mKernelFcharge;
	private CustomCheckBoxPreference mTouch2wake;
	private CustomPreference mT2wDelay;
	private CustomCheckBoxPreference mDoubleTap;
	private CustomCheckBoxPreference mDoubleTap2;
	private CustomCheckBoxPreference mSweep2wake;
	private CustomCheckBoxPreference mPowerSuspend;
	private CustomPreference mWakeTimeout;
	private CustomCheckBoxPreference mIntelliPlug;
	private CustomCheckBoxPreference mEcoMode;
	private CustomPreference mVibration;
	private CustomPreference mAdvancedScheduler;
	private CustomListPreference mCpuScheduler;
	private CustomListPreference mCpuReadAhead;
	private PreferenceCategory mSoundCategory;
	private CustomPreference mSoundInfo;
	private SharedPreferences mPrefs;
	private PreferenceCategory mKernelCategory;
	private PreferenceCategory mTouchCategory;
	private Context mContext; 
	private PreferenceScreen mRootScreen;
	private PreferenceCategory mSchedCategory;
	private static final String FSYNC_FILE = "/sys/module/sync/parameters/fsync_enabled";
	private static final String SCHEDULER_FILE = "/sys/block/mmcblk0/queue/scheduler";
	private static final String READ_AHEAD_FILE = "/sys/block/mmcblk0/queue/read_ahead_kb";
	private static final String FCHARGE_FILE = "/sys/kernel/fast_charge/force_fast_charge";
	private static final String TEMP_FILE = "/sys/module/msm_thermal/parameters/temp_threshold";

	private static final String SOUNDCONTROL_FILE = "/sys/kernel/sound_control_3";
	private static final String HEADSET_BOOST_FILE = "/sys/devices/virtual/misc/soundcontrol/headset_boost";
	private static final String MIC_BOOST_FILE = "/sys/devices/virtual/misc/soundcontrol/mic_boost";
	private static final String SPEAKER_BOOST_FILE = "/sys/devices/virtual/misc/soundcontrol/speaker_boost";
	private static final String VOLUME_BOOST_FILE = "/sys/devices/virtual/misc/soundcontrol/volume_boost";
	private static final String category = "kernel";
	private static final String TCP_OPTIONS = "sysctl net.ipv4.tcp_available_congestion_control";
	private static final String TCP_CURRENT = "sysctl net.ipv4.tcp_congestion_control";
	private static final String T2W_FILE = "/sys/devices/virtual/misc/touchwake/enabled";
	private static final String T2W_DELAY_FILE = "/sys/devices/virtual/misc/touchwake/delay";
	private static final String DT2W_FILE = "/sys/android_touch/doubletap2wake";
	private static final String DT2W2_FILE = "/sys/devices/virtual/input/lge_touch/dt_wake_enabled";
	private static final String S2W_FILE = "/sys/android_touch/sweep2wake";
	private static final String PWKS_FILE = "/sys/module/qpnp_power_on/parameters/pwrkey_suspend";
	private static final String WAKE_TIMEOUT_FILE = "/sys/android_touch/wake_timeout";
	private static final String INTELLIPLUG_FILE = "/sys/module/intelli_plug/parameters/intelli_plug_active";
	private static final String ECOMODE_FILE = "/sys/module/intelli_plug/parameters/eco_mode_active";
	private static final String DYNFSYNC_FILE = "/sys/kernel/dyn_fsync/Dyn_fsync_active";
	private static final String FAUXSOUND_FILE = "/sys/kernel/sound_control_3";
	private static final String VIBRATION_FILE = "/sys/class/timed_output/vibrator/amp";
	
	
	private String color;
	private DatabaseHandler db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_kernel);
		mContext = getActivity();

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle();
		}

		Helpers.setPermissions(T2W_FILE);
		Helpers.setPermissions(T2W_DELAY_FILE);		
		Helpers.setPermissions(DT2W_FILE);
		Helpers.setPermissions(DT2W2_FILE);
		Helpers.setPermissions(DYNFSYNC_FILE);
		Helpers.setPermissions(ECOMODE_FILE);
		Helpers.setPermissions(FAUXSOUND_FILE);
		Helpers.setPermissions(FCHARGE_FILE);
		Helpers.setPermissions(FSYNC_FILE);
		Helpers.setPermissions(HEADSET_BOOST_FILE);
		Helpers.setPermissions(INTELLIPLUG_FILE);
		Helpers.setPermissions(MIC_BOOST_FILE);
		Helpers.setPermissions(READ_AHEAD_FILE);
		Helpers.setPermissions(S2W_FILE);
		Helpers.setPermissions(PWKS_FILE);
                Helpers.setPermissions(WAKE_TIMEOUT_FILE);
		Helpers.setPermissions(SCHEDULER_FILE);
		Helpers.setPermissions(SPEAKER_BOOST_FILE);
		Helpers.setPermissions(TEMP_FILE);
		Helpers.setPermissions(VIBRATION_FILE);
		Helpers.setPermissions(VOLUME_BOOST_FILE);
		Helpers.setPermissions(TCP_CURRENT);
		Helpers.setPermissions(TCP_OPTIONS);
		

		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mSoundCategory = (PreferenceCategory) findPreference("key_sound_category");
		mSchedCategory = (PreferenceCategory) findPreference("key_sched_cat");
		mKernelFsync = (CustomCheckBoxPreference) findPreference("key_fsync_switch");
		mKernelDynFsync = (CustomCheckBoxPreference) findPreference("key_dynfsync_switch");
		mKernelFcharge = (CustomCheckBoxPreference) findPreference("key_fcharge_switch");
		mCpuScheduler = (CustomListPreference) findPreference("key_cpu_sched");
		mAdvancedScheduler = (CustomPreference) findPreference("key_advanced_scheduler");
		mCpuReadAhead = (CustomListPreference) findPreference("key_cpu_readahead");
		mSoundInfo = (CustomPreference) findPreference("key_kernel_info");
		mKernelCategory = (PreferenceCategory) findPreference("key_kernel_tweaks");
		mRootScreen = (PreferenceScreen) findPreference("key_pref_screen");
		mTouchCategory = (PreferenceCategory) findPreference("key_touch_cat");
		mTouch2wake = (CustomCheckBoxPreference) findPreference("key_t2w_switch");
		mT2wDelay = (CustomPreference) findPreference("key_t2w_delay_switch");
		mDoubleTap = (CustomCheckBoxPreference) findPreference("key_dt2w_switch");
		mDoubleTap2 = (CustomCheckBoxPreference) findPreference("key_dt2w2_switch");
		mSweep2wake = (CustomCheckBoxPreference) findPreference("key_s2w_switch");
		mPowerSuspend = (CustomCheckBoxPreference) findPreference("key_pwks_switch");
		mWakeTimeout = (CustomPreference) findPreference("key_wake_timeout_switch");
		mIntelliPlug = (CustomCheckBoxPreference) findPreference("key_intelliplug_switch");
		mEcoMode = (CustomCheckBoxPreference) findPreference("key_ecomode_switch");
		mVibration = (CustomPreference) findPreference("key_vibration");
		db = MainActivity.db;

		mKernelFsync.setKey(FSYNC_FILE);
		mKernelDynFsync.setKey(DYNFSYNC_FILE);
		mKernelFcharge.setKey(FCHARGE_FILE);
		mCpuScheduler.setKey(SCHEDULER_FILE);
		mCpuReadAhead.setKey(READ_AHEAD_FILE);
		mTouch2wake.setKey(T2W_FILE);
		mT2wDelay.setKey(T2W_DELAY_FILE);
		mDoubleTap.setKey(DT2W_FILE);
		mDoubleTap2.setKey(DT2W2_FILE);
		mSweep2wake.setKey(S2W_FILE);
		mPowerSuspend.setKey(PWKS_FILE);
		mWakeTimeout.setKey(WAKE_TIMEOUT_FILE);
		mIntelliPlug.setKey(INTELLIPLUG_FILE);
		mEcoMode.setKey(ECOMODE_FILE);
		mVibration.setKey(VIBRATION_FILE);

		mCpuScheduler.setKey(SCHEDULER_FILE);
		mCpuReadAhead.setKey(READ_AHEAD_FILE);

		mCpuScheduler.setCategory(category);
		mCpuReadAhead.setCategory(category);
		mKernelFsync.setCategory(category);
		mKernelDynFsync.setCategory(category);
		mKernelFcharge.setCategory(category);
		mTouch2wake.setCategory(category);
		mT2wDelay.setCategory(category);
		mDoubleTap.setCategory(category);
		mDoubleTap2.setCategory(category);
		mSweep2wake.setCategory(category);
		mPowerSuspend.setCategory(category);
		mWakeTimeout.setCategory(category);
		mIntelliPlug.setCategory(category);
		mEcoMode.setCategory(category);
		mVibration.setCategory(category);

		String[] schedulers = Helpers.getAvailableSchedulers();
		String[] readAheadKb = {"128","256","384","512","640","768","896","1024","1152",
				"1280","1408","1536","1664","1792","1920","2048", "2176", "2304", "2432", "2560", 
				"2688", "2816", "2944", "3072", "3200", "3328", "3456", "3584", "3712", "3840", "3968", "4096"};

		color = "";

		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			color = "#"+Integer.toHexString(col);
		}else if(mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_KERNEL, Color.parseColor("#ff0099cc"));
			color = "#"+Integer.toHexString(col);
		} 
		else {
			color = getResources().getStringArray(R.array.menu_colors)[5];
		}

		mKernelFsync.setTitleColor(color);
		mKernelDynFsync.setTitleColor(color);
		mKernelFcharge.setTitleColor(color);
		mCpuScheduler.setTitleColor(color);
		mAdvancedScheduler.setTitleColor(color);
		mCpuReadAhead.setTitleColor(color);
		mSoundInfo.setTitleColor("#ff4444");
		mSoundInfo.setSummaryColor("#ff4444");
		mSoundInfo.excludeFromDialog(true);
		mSoundInfo.hideBoot(true);
		mAdvancedScheduler.excludeFromDialog(true);
		mAdvancedScheduler.hideBoot(true);
		mTouch2wake.setTitleColor(color);
		mT2wDelay.setTitleColor(color);
		mDoubleTap.setTitleColor(color);
		mDoubleTap2.setTitleColor(color);
		mSweep2wake.setTitleColor(color);
		mPowerSuspend.setTitleColor(color);
		mWakeTimeout.setTitleColor(color);
		mIntelliPlug.setTitleColor(color);
		mEcoMode.setTitleColor(color);
		mVibration.setTitleColor(color);

		mCpuScheduler.setEntries(schedulers);
		mCpuScheduler.setEntryValues(schedulers);
		mCpuReadAhead.setEntries(readAheadKb);
		mCpuReadAhead.setEntryValues(readAheadKb);

		mCpuScheduler.setSummary(Helpers.getCurrentScheduler());
		mCpuReadAhead.setSummary(Helpers.getFileContent(new File(READ_AHEAD_FILE)));
                mT2wDelay.setSummary(Helpers.getFileContent(new File(T2W_DELAY_FILE)));
		mWakeTimeout.setSummary(Helpers.getFileContent(new File(T2W_DELAY_FILE)));

		mAdvancedScheduler.setOnPreferenceClickListener(this);
		mCpuScheduler.setOnPreferenceChangeListener(this);
		mCpuReadAhead.setOnPreferenceChangeListener(this);
		mT2wDelay.setOnPreferenceClickListener(this);
		mWakeTimeout.setOnPreferenceClickListener(this);
		mVibration.setOnPreferenceClickListener(this);

		mKernelFsync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo Y > "+FSYNC_FILE;
					value = "Y";
				} else {
					cmd = "echo N > "+FSYNC_FILE;
					value = "N";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				
				return true;
			}
		});

		mKernelDynFsync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+DYNFSYNC_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+DYNFSYNC_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mTouch2wake.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+T2W_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+T2W_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mDoubleTap.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+DT2W_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+DT2W_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mDoubleTap2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+DT2W2_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+DT2W2_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mSweep2wake.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+S2W_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+S2W_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mPowerSuspend.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo Y > "+PWKS_FILE;
					value = "Y";
				} else {
					cmd = "echo N > "+PWKS_FILE;
					value = "N";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mIntelliPlug.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+INTELLIPLUG_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+INTELLIPLUG_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mEcoMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences.Editor editor = mPrefs.edit();
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+ECOMODE_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+ECOMODE_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				editor.commit();
				return true;
			}
		});

		mKernelFcharge.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+FCHARGE_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+FCHARGE_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		if(!new File(FSYNC_FILE).exists()) {
			mKernelCategory.removePreference(mKernelFsync);
		} else {
			String fsyncState = Helpers.getFileContent(new File(FSYNC_FILE));
			if(fsyncState.equals("Y")) {
				mKernelFsync.setChecked(true);
				mKernelFsync.setValue("Y");
			}else if(fsyncState.equals("N")) {
				mKernelFsync.setChecked(false);
				mKernelFsync.setValue("N");
			}
		}

		if(!new File(DYNFSYNC_FILE).exists()) {
			mKernelCategory.removePreference(mKernelDynFsync);
		} else {
			String fsyncState = Helpers.getFileContent(new File(DYNFSYNC_FILE));
			if(fsyncState.equals("1")) {
				mKernelDynFsync.setChecked(true);
				mKernelDynFsync.setValue("1");
			}else if(fsyncState.equals("0")) {
				mKernelDynFsync.setChecked(false);
				mKernelDynFsync.setValue("0");
			}
		}

		if(!new File(T2W_FILE).exists()) {
			mTouchCategory.removePreference(mTouch2wake);
                        mTouchCategory.removePreference(mT2wDelay);
		} else {
			String t2wState = Helpers.getFileContent(new File(T2W_FILE));
			if(t2wState.equals("1")) {
				mTouch2wake.setChecked(true);
				mTouch2wake.setValue("1");
			}else if(t2wState.equals("0")) {
				mTouch2wake.setChecked(false);
				mTouch2wake.setValue("0");
			}
		}

		if(!new File(DT2W_FILE).exists()) {
			mTouchCategory.removePreference(mDoubleTap);
		} else {
			String dtState = Helpers.getFileContent(new File(DT2W_FILE));
			if(dtState.equals("1")) {
				mDoubleTap.setChecked(true);
				mDoubleTap.setValue("1");
			}else if(dtState.equals("0")) {
				mDoubleTap.setChecked(false);
				mDoubleTap.setValue("0");
			}
		}

		if(!new File(DT2W2_FILE).exists()) {
			mTouchCategory.removePreference(mDoubleTap2);
		} else {
			String dtState = Helpers.getFileContent(new File(DT2W2_FILE));
			if(dtState.equals("1")) {
				mDoubleTap2.setChecked(true);
				mDoubleTap2.setValue("1");
			}else if(dtState.equals("0")) {
				mDoubleTap2.setChecked(false);
				mDoubleTap2.setValue("0");
			}
		}

		if(!new File(S2W_FILE).exists()) {
			mTouchCategory.removePreference(mSweep2wake);
		} else {
			String s2wState = Helpers.getFileContent(new File(S2W_FILE));
			if(s2wState.equals("1")) {
				mSweep2wake.setChecked(true);
				mSweep2wake.setValue("1");
			}else if(s2wState.equals("0")) {
				mSweep2wake.setChecked(false);
				mSweep2wake.setValue("0");
			}
		}

		if(!new File(PWKS_FILE).exists()) {
			mTouchCategory.removePreference(mPowerSuspend);
		} else {
			String pwksState = Helpers.getFileContent(new File(PWKS_FILE));
			if(pwksState.equals("Y")) {
				mPowerSuspend.setChecked(true);
				mPowerSuspend.setValue("Y");
			}else if(pwksState.equals("N")) {
				mPowerSuspend.setChecked(false);
				mPowerSuspend.setValue("N");
			}
		}

		if(new File(WAKE_TIMEOUT_FILE).exists()){
			mWakeTimeout.setSummary(Helpers.getFileContent(new File(WAKE_TIMEOUT_FILE)));
		}else {
			mTouchCategory.removePreference(mWakeTimeout);
		}

		if(!new File(FCHARGE_FILE).exists()) {
			mKernelCategory.removePreference(mKernelFcharge);
		} else {
			String fchargeState = Helpers.getFileContent(new File(FCHARGE_FILE));
			if(fchargeState.equals("0")) {
				mKernelFcharge.setChecked(false);
				mKernelFcharge.setValue("0");
			} else if(fchargeState.equals("1")) {
				mKernelFcharge.setChecked(true);
				mKernelFcharge.setValue("1");
			}
		}

		if(!new File(INTELLIPLUG_FILE).exists()) {
			mKernelCategory.removePreference(mIntelliPlug);
		} else {
			String fchargeState = Helpers.getFileContent(new File(INTELLIPLUG_FILE));
			if(fchargeState.equals("0")) {
				mIntelliPlug.setChecked(false);
				mIntelliPlug.setValue("0");
			} else if(fchargeState.equals("1")) {
				mIntelliPlug.setChecked(true);
				mIntelliPlug.setValue("1");
			}
		}

		if(!new File(ECOMODE_FILE).exists()) {
			mKernelCategory.removePreference(mEcoMode);
		} else {
			String fchargeState = Helpers.getFileContent(new File(ECOMODE_FILE));
			if(fchargeState.equals("0")) {
				mEcoMode.setChecked(false);
				mEcoMode.setValue("0");
			} else if(fchargeState.equals("1")) {
				mEcoMode.setChecked(true);
				mEcoMode.setValue("1");
			}
		}

		if(mKernelCategory.getPreferenceCount() == 0) {
			mRootScreen.removePreference(mKernelCategory);
		}

		File f = new File(HEADSET_BOOST_FILE);
		File f1 = new File(MIC_BOOST_FILE);
		File f2 = new File(SPEAKER_BOOST_FILE);
		File f3 = new File(VOLUME_BOOST_FILE);
		File f4 = new File(TEMP_FILE);
		if(f.exists())
			createPreference(mSoundCategory,f, color, true);
		if(f1.exists())
			createPreference(mSoundCategory,f1, color, true);
		if(f2.exists())
			createPreference(mSoundCategory,f2, color, true);
		if(f3.exists())
			createPreference(mSoundCategory,f3, color, true);
		if(f4.exists()) {
			createPreference(mKernelCategory,f4, color, true);
		}
		if(new File(VIBRATION_FILE).exists()){
			mVibration.setSummary(Helpers.getFileContent(new File(VIBRATION_FILE)));
		}else {
			mKernelCategory.removePreference(mVibration);
		}
		/*
		if(new File(FAUXSOUND_FILE).exists()) {
			File[] files = new File(FAUXSOUND_FILE).listFiles();
			Arrays.sort(files);
			for(File file: files) {
				if(!file.getName().contains("version")) {
					createPreference(mSoundCategory,file, color, false);
				}
			}
		}
		*/
		if(mSoundCategory.getPreferenceCount() == 1) {
			mRootScreen.removePreference(mSoundCategory);
		}

		if(RootTools.isBusyboxAvailable()) {
			String[] availTCP = Helpers.readCommandStrdOut(TCP_OPTIONS, false).replaceAll("net.ipv4.tcp_available_congestion_control = ", "").replaceAll("\n", "").split(" ");
			CustomListPreference pref = new CustomListPreference(mContext, category);
			pref.setTitle("TCP Congestion control");
			pref.setTitleColor(color);
			pref.setEntries(availTCP);
			pref.setEntryValues(availTCP);
			pref.setSummary(Helpers.readCommandStrdOut(TCP_CURRENT, false).replaceAll("net.ipv4.tcp_congestion_control = ","").replaceAll("\n", ""));
			String key = "sysctl -w net.ipv4.tcp_congestion_control=" +pref.getSummary();
			pref.setKey(key);
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					// TODO Auto-generated method stub
					String value = (String) newValue;
					String command = "sysctl -w net.ipv4.tcp_congestion_control=" +value;
					pref.setKey(command);
					pref.setSummary(value);
					CMDProcessor.runSuCommand(command);
					return true;
				}

			});

			mSchedCategory.addPreference(pref);
		}
		setRetainInstance(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.layout_list, container,false);

		return v;
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		// TODO Auto-generated method stub
		if(pref == mCpuScheduler) {
			String value = (String) newValue;
			mCpuScheduler.setSummary(value);
			mCpuScheduler.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+SCHEDULER_FILE);
			updateDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mCpuReadAhead) {
			String value = (String) newValue;
			mCpuReadAhead.setSummary(value);
			mCpuReadAhead.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+READ_AHEAD_FILE);
			updateDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(final Preference pref) {
		// TODO Auto-generated method stub
		if(pref == mAdvancedScheduler) {
			Fragment f = new CpuSchedulerPreferenceFragment();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			// This adds the newly created Preference fragment to my main layout, shown below
			ft.replace(R.id.activity_container,f);
			// By hiding the main fragment, transparency isn't an issue
			ft.addToBackStack("TAG");
			ft.commit();
		}
                if(pref == mT2wDelay) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.dialog_layout, null, false);
			final EditText et = (EditText) v.findViewById(R.id.et);
			String val = pref.getSummary().toString();
			et.setText(val);
			et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
			et.setGravity(Gravity.CENTER_HORIZONTAL);
			List<DataItem> items = db.getAllItems();
			builder.setView(v);
			builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String value = et.getText().toString();
					pref.setSummary(value);
					CMDProcessor.runSuCommand("echo \""+value+"\" > "+pref.getKey());
					updateDb(pref, value, ((CustomPreference) pref).isBootChecked());
				}
			} );
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
			Window window = dialog.getWindow();
			window.setLayout(600, LayoutParams.WRAP_CONTENT);
		}
                if(pref == mWakeTimeout) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.dialog_layout, null, false);
			final EditText et = (EditText) v.findViewById(R.id.et);
			String val = pref.getSummary().toString();
			et.setText(val);
			et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
			et.setGravity(Gravity.CENTER_HORIZONTAL);
			List<DataItem> items = db.getAllItems();
			builder.setView(v);
			builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String value = et.getText().toString();
					pref.setSummary(value);
					CMDProcessor.runSuCommand("echo \""+value+"\" > "+pref.getKey());
					updateDb(pref, value, ((CustomPreference) pref).isBootChecked());
				}
			} );
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
			Window window = dialog.getWindow();
			window.setLayout(600, LayoutParams.WRAP_CONTENT);
		}
		if(pref == mVibration) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.dialog_layout, null, false);
			final EditText et = (EditText) v.findViewById(R.id.et);
			String val = pref.getSummary().toString();
			et.setText(val);
			et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
			et.setGravity(Gravity.CENTER_HORIZONTAL);
			List<DataItem> items = db.getAllItems();
			builder.setView(v);
			builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String value = et.getText().toString();
					pref.setSummary(value);
					CMDProcessor.runSuCommand("echo \""+value+"\" > "+pref.getKey());
					updateDb(pref, value, ((CustomPreference) pref).isBootChecked());
				}
			} );
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
			Window window = dialog.getWindow();
			window.setLayout(600, LayoutParams.WRAP_CONTENT);
		}

		return false;
	}

	private void createPreference(PreferenceCategory mCategory, File file, String color,final boolean numbers) {
		String fileName = file.getName();
		String filePath = file.getAbsolutePath();
		final String fileContent = Helpers.getFileContent(file);
		final CustomPreference pref = new CustomPreference(mContext, false, category);
		pref.setTitle(fileName);
		pref.setTitleColor(color);
		pref.setSummary(fileContent);
		pref.setKey(filePath);
		Log.d("CONTENT", fileContent);
		mCategory.addPreference(pref);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(final Preference p) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				LayoutInflater inflater = getActivity().getLayoutInflater();
				View v = inflater.inflate(R.layout.dialog_layout, null, false);
				final EditText et = (EditText) v.findViewById(R.id.et);
				String val = p.getSummary().toString();
				et.setText(val);
				if(numbers) {
					et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
				}
				et.setGravity(Gravity.CENTER_HORIZONTAL);
				List<DataItem> items = db.getAllItems();
				builder.setView(v);
				builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String value = et.getText().toString();
						p.setSummary(value);
						Log.d("TEST", "echo \""+value+"\" > "+ p.getKey());
						CMDProcessor.runSuCommand("echo \""+value+"\" > "+p.getKey());
						updateDb(p, value, pref.isBootChecked());
					}
				} );
				AlertDialog dialog = builder.create();
				dialog.show();
				dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
				Window window = dialog.getWindow();
				window.setLayout(600, LayoutParams.WRAP_CONTENT);
				return true;
			}

		});
	}



	private void updateDb(final Preference p, final String value,final boolean isChecked) {

		class LongOperation extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... params) {

				if(isChecked) {
					List<DataItem> items = db.getAllItems();
					for(DataItem item : items) {
						if(item.getName().equals("'"+p.getKey()+"'")) {
							db.deleteItemByName("'"+p.getKey()+"'");
						}
					}
					db.addItem(new DataItem("'"+p.getKey()+"'", value, p.getTitle().toString(), category));
				} else {
					if(db.getContactsCount() != 0) {
						db.deleteItemByName("'"+p.getKey()+"'");
					}
				}

				return "Executed";
			}
			@Override
			protected void onPostExecute(String result) {

			}
		}
		new LongOperation().execute();
	}

}
