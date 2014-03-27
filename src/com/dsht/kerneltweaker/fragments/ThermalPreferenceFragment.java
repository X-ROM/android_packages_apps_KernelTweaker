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
import android.content.res.Resources;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class ThermalPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener {

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

	private int mSeekbarProgress;
	private EditText settingText;
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
		Helpers.setPermissions(CHECK_FILE);

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
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mShutdown) {
            String title = getString(R.string.thermal_shutdown);
            int currentProgress = Integer.parseInt(Helpers.readOneLine(SHUTDOWN_FILE));
            openDialog(currentProgress, title, 0, 100, preference,
                    SHUTDOWN_FILE, SHUTDOWN_FILE);
            return true;
        } else if (preference == mLowlimit) {
            String title = getString(R.string.thermal_low_limit);
            int currentProgress = Integer.parseInt(Helpers.readOneLine(LOW_LIMIT_FILE));
            openDialog(currentProgress, title, 0, 100, preference,
                    LOW_LIMIT_FILE, LOW_LIMIT_FILE);
            return true;
        } else if (preference == mLowlimitClear) {
            String title = getString(R.string.thermal_low_limit_clear);
            int currentProgress = Integer.parseInt(Helpers.readOneLine(LOW_LIMIT_CLEAR_FILE));
            openDialog(currentProgress, title, 0, 100, preference,
                    LOW_LIMIT_CLEAR_FILE, LOW_LIMIT_CLEAR_FILE);
            return true;
        } else if (preference == mMidlimit) {
            String title = getString(R.string.thermal_mid_limit);
            int currentProgress = Integer.parseInt(Helpers.readOneLine(MID_LIMIT_FILE));
            openDialog(currentProgress, title, 0, 100, preference,
                    MID_LIMIT_FILE, MID_LIMIT_FILE);
            return true;
        } else if (preference == mMidlimitClear) {
            String title = getString(R.string.thermal_mid_limit_clear);
            int currentProgress = Integer.parseInt(Helpers.readOneLine(MID_LIMIT_CLEAR_FILE));
            openDialog(currentProgress, title, 0, 100, preference,
                    MID_LIMIT_CLEAR_FILE, MID_LIMIT_CLEAR_FILE);
            return true;
        } else if (preference == mMaxlimit) {
            String title = getString(R.string.thermal_max_limit);
            int currentProgress = Integer.parseInt(Helpers.readOneLine(MAX_LIMIT_FILE));
            openDialog(currentProgress, title, 0, 100, preference,
                    MAX_LIMIT_FILE, MAX_LIMIT_FILE);
            return true;
        } else if (preference == mMaxlimitClear) {
            String title = getString(R.string.thermal_max_limit_clear);
            int currentProgress = Integer.parseInt(Helpers.readOneLine(MAX_LIMIT_CLEAR_FILE));
            openDialog(currentProgress, title, 0, 100, preference,
                    MAX_LIMIT_CLEAR_FILE, MAX_LIMIT_CLEAR_FILE);
            return true;
	} else if (preference == mThermalPoll) {
            String title = getString(R.string.thermal_check);
            int currentProgress = Integer.parseInt(Helpers.readOneLine(CHECK_FILE));
            openDialog(currentProgress, title, 0, 500, preference,
                    CHECK_FILE, CHECK_FILE);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void openDialog(int currentProgress, String title, final int min, final int max,
                           final Preference pref, final String path, final String key) {
        Resources res = mContext.getResources();
        String cancel = res.getString(R.string.cancel);
        String ok = res.getString(R.string.ok);
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);

        final SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);

        seekbar.setMax(max);
        seekbar.setProgress(currentProgress);

        settingText = (EditText) alphaDialog.findViewById(R.id.setting_text);
        settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    int val = Integer.parseInt(settingText.getText().toString());
                    seekbar.setProgress(val);
                    return true;
                }
                return false;
            }
        });
        settingText.setText(Integer.toString(currentProgress));
        settingText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val > max) {
                        s.replace(0, s.length(), Integer.toString(max));
                        val = max;
                    }
                    seekbar.setProgress(val);
                } catch (NumberFormatException ex) {
                }
            }
        });

        SeekBar.OnSeekBarChangeListener seekBarChangeListener =
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                        mSeekbarProgress = seekbar.getProgress();
                        if (fromUser) {
                            settingText.setText(Integer.toString(mSeekbarProgress));
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekbar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekbar) {
                    }
                };
        seekbar.setOnSeekBarChangeListener(seekBarChangeListener);

        new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setView(alphaDialog)
                .setNegativeButton(cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // nothing
                            }
                        })
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int val = Integer.parseInt(settingText.getText().toString());
                        if (val < min) {
                            val = min;
                        }
                        seekbar.setProgress(val);
                        int newProgress = seekbar.getProgress();
                        pref.setSummary(Integer.toString(newProgress));
                        if (Helpers.isSystemApp(getActivity())) {
                            Helpers.writeOneLine(path, Integer.toString(newProgress));
                        } else {
                            CMDProcessor.runSuCommand("busybox echo " + newProgress + " > " + path);
                        }
                        updateListDb(pref, Integer.toString(newProgress), ((CustomPreference)pref).isBootChecked());
                        final SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putInt(key, newProgress);
                        editor.commit();
                    }
                }).create().show();
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
