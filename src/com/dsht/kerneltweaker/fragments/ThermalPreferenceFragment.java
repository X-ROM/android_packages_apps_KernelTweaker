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

public class ThermalPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {

	private CustomCheckBoxPreference mEnabled;
	private CustomPreference mShutdown;
	private CustomPreference mThermalPoll;
	private CustomPreference mLowlimit;
	private CustomPreference mLowlimitClear;
	private CustomPreference mMidlimit;
	private CustomPreference mMidlimitClear;
	private CustomPreference mMaxlimit;
	private CustomPreference mMaxlimitClear;
	private CustomListPreference mLowFreq;
	private CustomListPreference mMidFreq;
	private CustomListPreference mMaxFreq;
	private Context mContext;
	private PreferenceScreen mRoot;
        private SharedPreferences mPrefs;

        private static final String category = "thermal";
	private static final String THERMAL_FILE = "/sys/kernel/msm_thermal/conf";
	private static final String ENABLED_FILE = "/sys/module/msm_thermal/parameters/enabled";
	private static final String SHUTDOWN_FILE = "/sys/kernel/msm_thermal/conf/shutdown_temp";
	private static final String CHECK_FILE = "/sys/kernel/msm_thermal/conf/poll_ms";
	private static final String LOW_LIMIT_FILE = "/sys/kernel/msm_thermal/conf/allowed_low_high";
	private static final String LOW_LIMIT_CLEAR_FILE = "/sys/kernel/msm_thermal/conf/allowed_low_low";
	private static final String MID_LIMIT_FILE = "/sys/kernel/msm_thermal/conf/allowed_mid_high";
	private static final String MID_LIMIT_CLEAR_FILE = "/sys/kernel/msm_thermal/conf/allowed_mid_low";
	private static final String MAX_LIMIT_FILE = "/sys/kernel/msm_thermal/conf/allowed_max_high";
	private static final String MAX_LIMIT_CLEAR_FILE = "/sys/kernel/msm_thermal/conf/allowed_max_low";
	private static final String LOW_FREQ_FILE = "/sys/kernel/msm_thermal/conf/allowed_low_freq";
	private static final String MID_FREQ_FILE = "/sys/kernel/msm_thermal/conf/allowed_mid_freq";
	private static final String MAX_FREQ_FILE = "/sys/kernel/msm_thermal/conf/allowed_max_freq";

	private String color;
	private DatabaseHandler db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_msm_thermal);
		mRoot = (PreferenceScreen) findPreference("key_root");
		mContext = getActivity();
		setRetainInstance(true);

		Helpers.setPermissions(ENABLED_FILE);
		Helpers.setPermissions(SHUTDOWN_FILE);
		Helpers.setPermissions(LOW_LIMIT_FILE);
		Helpers.setPermissions(LOW_LIMIT_CLEAR_FILE);
		Helpers.setPermissions(MID_LIMIT_FILE);
		Helpers.setPermissions(MID_LIMIT_CLEAR_FILE);
		Helpers.setPermissions(MAX_LIMIT_FILE);
		Helpers.setPermissions(MAX_LIMIT_CLEAR_FILE);
		Helpers.setPermissions(LOW_FREQ_FILE);
		Helpers.setPermissions(MID_FREQ_FILE);
		Helpers.setPermissions(MAX_FREQ_FILE);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mEnabled = (CustomCheckBoxPreference) findPreference("key_enabled");
		mShutdown = (CustomPreference) findPreference("key_shutdown_temp");
		mThermalPoll = (CustomPreference) findPreference("key_thermal_check");
		mLowlimit = (CustomPreference) findPreference("key_low_limit");
		mLowlimitClear = (CustomPreference) findPreference("key_low_limit_clear");
		mMidlimit = (CustomPreference) findPreference("key_mid_limit");
                mMidlimitClear = (CustomPreference) findPreference("key_mid_limit_clear");
		mMaxlimit = (CustomPreference) findPreference("key_max_limit");
                mMaxlimitClear = (CustomPreference) findPreference("key_max_limit_clear");
		mLowFreq = (CustomListPreference) findPreference("key_low_freq");
		mMidFreq = (CustomListPreference) findPreference("key_mid_freq");
                mMaxFreq = (CustomListPreference) findPreference("key_max_freq");
		db = MainActivity.db;

		mEnabled.setKey(ENABLED_FILE);
		mShutdown.setKey(SHUTDOWN_FILE);
		mThermalPoll.setKey(CHECK_FILE);
		mLowlimit.setKey(LOW_LIMIT_FILE);
		mLowlimitClear.setKey(LOW_LIMIT_CLEAR_FILE);
		mMidlimit.setKey(MID_LIMIT_FILE);
		mMidlimitClear.setKey(MID_LIMIT_CLEAR_FILE);
		mMaxlimit.setKey(MAX_LIMIT_FILE);
		mMaxlimitClear.setKey(MAX_LIMIT_CLEAR_FILE);
		mLowFreq.setKey(LOW_FREQ_FILE);
		mMidFreq.setKey(MID_FREQ_FILE);
		mMaxFreq.setKey(MAX_FREQ_FILE);

		mEnabled.setCategory(category);
		mShutdown.setCategory(category);
		mThermalPoll.setCategory(category);
		mLowlimit.setCategory(category);
		mLowlimitClear.setCategory(category);
		mMidlimit.setCategory(category);
		mMidlimitClear.setCategory(category);
		mMaxlimit.setCategory(category);
		mMaxlimitClear.setCategory(category);
		mLowFreq.setCategory(category);
		mMidFreq.setCategory(category);
		mMaxFreq.setCategory(category);

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

		mEnabled.setTitleColor(color);
		mShutdown.setTitleColor(color);
		mThermalPoll.setTitleColor(color);
		mLowlimit.setTitleColor(color);
		mLowlimitClear.setTitleColor(color);
		mMidlimit.setTitleColor(color);
		mMidlimitClear.setTitleColor(color);
		mMaxlimit.setTitleColor(color);
		mMaxlimitClear.setTitleColor(color);
		mLowFreq.setTitleColor(color);
		mMidFreq.setTitleColor(color);
		mMaxFreq.setTitleColor(color);

		String[] frequencies = Helpers.getFrequencies();
		String[] names = Helpers.getFrequenciesNames();

		mShutdown.setSummary(Helpers.readOneLine(SHUTDOWN_FILE));
		mThermalPoll.setSummary(Helpers.readOneLine(CHECK_FILE));
		mLowlimit.setSummary(Helpers.readOneLine(LOW_LIMIT_FILE));
		mLowlimitClear.setSummary(Helpers.readOneLine(LOW_LIMIT_CLEAR_FILE));
		mMidlimit.setSummary(Helpers.readOneLine(MID_LIMIT_FILE));
		mMidlimitClear.setSummary(Helpers.readOneLine(MID_LIMIT_CLEAR_FILE));
		mMaxlimit.setSummary(Helpers.readOneLine(MAX_LIMIT_FILE));
		mMaxlimitClear.setSummary(Helpers.readOneLine(MAX_LIMIT_CLEAR_FILE));

		mLowFreq.setEntries(names);
		mLowFreq.setEntryValues(frequencies);
		mMidFreq.setEntries(names);
		mMidFreq.setEntryValues(frequencies);
		mMaxFreq.setEntries(names);
		mMaxFreq.setEntryValues(frequencies);

		mShutdown.setOnPreferenceClickListener(this);
		mThermalPoll.setOnPreferenceClickListener(this);
		mLowlimit.setOnPreferenceClickListener(this);
		mLowlimitClear.setOnPreferenceClickListener(this);
		mMidlimit.setOnPreferenceClickListener(this);
		mMidlimitClear.setOnPreferenceClickListener(this);
		mMaxlimit.setOnPreferenceClickListener(this);
		mMaxlimitClear.setOnPreferenceClickListener(this);
		mLowFreq.setOnPreferenceChangeListener(this);
		mMidFreq.setOnPreferenceChangeListener(this);
		mMaxFreq.setOnPreferenceChangeListener(this);

		mEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo Y > "+ENABLED_FILE;
					value = "Y";
				} else {
					cmd = "echo N > "+ENABLED_FILE;
					value = "N";
				}
				CMDProcessor.runSuCommand(cmd);
				updateListDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		String enabledState = Helpers.getFileContent(new File(ENABLED_FILE));
		if(enabledState.equals("Y")) {
			mEnabled.setChecked(true);
			mEnabled.setValue("Y");
		}else if(enabledState.equals("N")) {
			mEnabled.setChecked(false);
			mEnabled.setValue("N");
		}

		if(new File(LOW_FREQ_FILE).exists()) {
			mLowFreq.setSummary(Helpers.readOneLine(LOW_FREQ_FILE));
			mLowFreq.setValue(mLowFreq.getSummary().toString());
		}
		if(new File(MID_FREQ_FILE).exists()) {
			mMidFreq.setSummary(Helpers.readOneLine(MID_FREQ_FILE));
			mMidFreq.setValue(mMidFreq.getSummary().toString());
		}
		if(new File(MAX_FREQ_FILE).exists()) {
			mMaxFreq.setSummary(Helpers.readOneLine(MAX_FREQ_FILE));
			mMaxFreq.setValue(mMaxFreq.getSummary().toString());
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
		if(pref == mLowFreq) {
			String value = (String) newValue;
			mLowFreq.setSummary(value);
			mLowFreq.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+LOW_FREQ_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mMidFreq) {
			String value = (String) newValue;
			mMidFreq.setSummary(value);
			mMidFreq.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+MID_FREQ_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mMaxFreq) {
			String value = (String) newValue;
			mMaxFreq.setSummary(value);
			mMaxFreq.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+MAX_FREQ_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(final Preference pref) {
		// TODO Auto-generated method stub
		if(pref == mShutdown) {
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
		} else if(pref == mThermalPoll) {
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
		} else if(pref == mLowlimit) {
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
		} else if(pref == mLowlimitClear) {
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
		} else if(pref == mMidlimit) {
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
		} else if(pref == mMidlimitClear) {
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
		} else if(pref == mMaxlimit) {
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
		} else if(pref == mMaxlimitClear) {
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
				window.setLayout(600, LayoutParams.WRAP_CONTENT);
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
