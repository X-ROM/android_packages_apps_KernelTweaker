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

public class MpdecisionPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {

	private CustomPreference mMpdecAdvanced;
	private PreferenceCategory mMpdecisionCategory;
	private CustomCheckBoxPreference mEnabled;
	private CustomListPreference mMaxcpu;
	private CustomListPreference mMincpu;
	private CustomCheckBoxPreference mScrOffSingleCore;
	private CustomCheckBoxPreference mTouchBoost;
	private CustomPreference mBoostTime;
	private Context mContext;
	private PreferenceScreen mRoot;
        private SharedPreferences mPrefs;

        private static final String category = "mpdecision";
	private static final String MPDECISION_FILE = "/sys/kernel/msm_mpdecision/conf";
	private static final String ENABLED_FILE = "/sys/kernel/msm_mpdecision/conf/enabled";
	private static final String MAX_CPU_FILE = "/sys/kernel/msm_mpdecision/conf/max_cpus";
	private static final String MIN_CPU_FILE = "/sys/kernel/msm_mpdecision/conf/min_cpus";
	private static final String SCR_OFF_SINGLE_FILE = "/sys/kernel/msm_mpdecision/conf/scroff_single_core";
	private static final String TOUCH_BOOST_FILE = "/sys/kernel/msm_mpdecision/conf/boost_enabled";
	private static final String BOOST_TIME_FILE = "/sys/kernel/msm_mpdecision/conf/boost_time";

	private String color;
	private DatabaseHandler db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_mpdecision);
		mRoot = (PreferenceScreen) findPreference("key_root");
		mMpdecisionCategory = (PreferenceCategory) findPreference("key_mpdecision_category");
		mContext = getActivity();
		setRetainInstance(true);

		Helpers.setPermissions(MPDECISION_FILE);
		Helpers.setPermissions(ENABLED_FILE);
		Helpers.setPermissions(MAX_CPU_FILE);
		Helpers.setPermissions(MIN_CPU_FILE);
		Helpers.setPermissions(SCR_OFF_SINGLE_FILE);
		Helpers.setPermissions(TOUCH_BOOST_FILE);
		Helpers.setPermissions(BOOST_TIME_FILE);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mMpdecAdvanced = (CustomPreference) findPreference("key_mpdec_advanced");
		mEnabled = (CustomCheckBoxPreference) findPreference("key_enabled");
		mMaxcpu = (CustomListPreference) findPreference("key_max_cpu");
		mMincpu = (CustomListPreference) findPreference("key_min_cpu");
		mScrOffSingleCore = (CustomCheckBoxPreference) findPreference("key_scroff_single_core");
		mTouchBoost = (CustomCheckBoxPreference) findPreference("key_touch_boost");
                mBoostTime = (CustomPreference) findPreference("key_boost_time");
		db = MainActivity.db;

		mMpdecAdvanced.setKey(MPDECISION_FILE);
		mEnabled.setKey(ENABLED_FILE);
		mMaxcpu.setKey(MAX_CPU_FILE);
		mMincpu.setKey(MIN_CPU_FILE);
		mScrOffSingleCore.setKey(SCR_OFF_SINGLE_FILE);
		mTouchBoost.setKey(TOUCH_BOOST_FILE);
		mBoostTime.setKey(BOOST_TIME_FILE);

		mMpdecAdvanced.setCategory(category);
		mEnabled.setCategory(category);
		mMaxcpu.setCategory(category);
		mMincpu.setCategory(category);
		mScrOffSingleCore.setCategory(category);
		mTouchBoost.setCategory(category);
		mBoostTime.setCategory(category);

		String[] maxCpu = {"1","2","3","4"};
		String[] minCpu = {"1","2","3","4"};

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

		mMpdecAdvanced.setTitleColor(color);
		mEnabled.setTitleColor(color);
		mMaxcpu.setTitleColor(color);
		mMincpu.setTitleColor(color);
		mScrOffSingleCore.setTitleColor(color);
		mTouchBoost.setTitleColor(color);
		mBoostTime.setTitleColor(color);

		mMaxcpu.setEntries(maxCpu);
		mMaxcpu.setEntryValues(maxCpu);
		mMincpu.setEntries(minCpu);
		mMincpu.setEntryValues(minCpu);

		mMaxcpu.setSummary(Helpers.readOneLine(MAX_CPU_FILE));
		mMaxcpu.setValue(mMaxcpu.getSummary().toString());
		mMincpu.setSummary(Helpers.readOneLine(MIN_CPU_FILE));
		mMincpu.setValue(mMincpu.getSummary().toString());
		mBoostTime.setSummary(Helpers.readOneLine(BOOST_TIME_FILE));

		mMaxcpu.setOnPreferenceChangeListener(this);
		mMincpu.setOnPreferenceChangeListener(this);
		mBoostTime.setOnPreferenceClickListener(this);
		mMpdecAdvanced.setOnPreferenceClickListener(this);

		mMpdecAdvanced.hideBoot(true);

		mEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+ENABLED_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+ENABLED_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateListDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mScrOffSingleCore.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+SCR_OFF_SINGLE_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+SCR_OFF_SINGLE_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateListDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mTouchBoost.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+TOUCH_BOOST_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+TOUCH_BOOST_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateListDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());

				return true;
			}
		});

		String enabledState = Helpers.getFileContent(new File(ENABLED_FILE));
		if(enabledState.equals("1")) {
			mEnabled.setChecked(true);
			mEnabled.setValue("1");
		}else if(enabledState.equals("0")) {
			mEnabled.setChecked(false);
			mEnabled.setValue("0");
		}

		String scroffState = Helpers.getFileContent(new File(SCR_OFF_SINGLE_FILE));
		if(scroffState.equals("1")) {
			mScrOffSingleCore.setChecked(true);
			mScrOffSingleCore.setValue("1");
		}else if(scroffState.equals("0")) {
			mScrOffSingleCore.setChecked(false);
			mScrOffSingleCore.setValue("0");
		}

		String tboostState = Helpers.getFileContent(new File(TOUCH_BOOST_FILE));
		if(tboostState.equals("1")) {
			mTouchBoost.setChecked(true);
			mTouchBoost.setValue("1");
		}else if(tboostState.equals("0")) {
			mTouchBoost.setChecked(false);
			mTouchBoost.setValue("0");
		}

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
		if(pref == mMaxcpu) {
			String value = (String) newValue;
			mMaxcpu.setSummary(value);
			mMaxcpu.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+MAX_CPU_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mMincpu) {
			String value = (String) newValue;
			mMincpu.setSummary(value);
			mMincpu.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+MIN_CPU_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(final Preference pref) {
		// TODO Auto-generated method stub
		Fragment f = null;
		if(pref == mMpdecAdvanced) {
			f = new MpdecAdvancedPreferenceFragment();

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		// This adds the newly created Preference fragment to my main layout, shown below
		ft.replace(R.id.activity_container,f);
		// By hiding the main fragment, transparency isn't an issue
		ft.addToBackStack("TAG");
		ft.commit();
		}

                if(pref == mBoostTime) {
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
					updateListDb(pref, value, ((CustomPreference) pref).isBootChecked());
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

	private void createPreference(PreferenceCategory mCategory, File file, String color) {
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
				et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
				et.setGravity(Gravity.CENTER_HORIZONTAL);
				builder.setView(v);
				builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String value = et.getText().toString();
						p.setSummary(value);
						Log.d("TEST", "echo "+value+" > "+ p.getKey());
						CMDProcessor.runSuCommand("echo "+value+" > "+p.getKey());
						updateListDb(pref, value, pref.isBootChecked());
					}
				} );
				AlertDialog dialog = builder.create();
				dialog.show();
				dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
				Window window = dialog.getWindow();
				window.setLayout(800, LayoutParams.WRAP_CONTENT);
				return true;
			}

		});
	}

	private void updateListDb(final Preference p, final String value, final boolean isChecked) {

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
