package de.wikilab.loghud;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

public class ConfigurationActivity extends Activity {
	
	HUDService service;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        EditText fontSizeTb = (EditText) findViewById(R.id.fontsize);
        fontSizeTb.setText(String.valueOf(prefs.getInt("fontsize", 10)));


		ToggleButton transparentTb = (ToggleButton) findViewById(R.id.toggleButton3);
		transparentTb.setChecked(prefs.getBoolean("transparent", false));

		ToggleButton scrollModeTb = (ToggleButton) findViewById(R.id.toggleButton2);
		scrollModeTb.setChecked(prefs.getBoolean("scrollmode", false));
        
        ToggleButton tb = (ToggleButton) findViewById(R.id.toggleButton1);
        tb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					saveSettings();
					
					Intent uploadIntent = new Intent(getApplicationContext(), HUDService.class);
					uploadIntent.setAction(HUDService.ACTION_START);
					startService(uploadIntent);
					
				} else {

					Intent uploadIntent = new Intent(getApplicationContext(), HUDService.class);
					uploadIntent.setAction(HUDService.ACTION_STOP);
					startService(uploadIntent);
					
				}
			}
		});
        
    }
    
    public void saveSettings() {
    	SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	
		ToggleButton scrollModeTb = (ToggleButton) findViewById(R.id.toggleButton2);
        prefs.putBoolean("scrollmode", scrollModeTb.isChecked());
    	
		ToggleButton transparentTb = (ToggleButton) findViewById(R.id.toggleButton3);
        prefs.putBoolean("transparent", transparentTb.isChecked());
        
		EditText fontSizeTb = (EditText) findViewById(R.id.fontsize);
        prefs.putInt("fontsize", Integer.valueOf(fontSizeTb.getText().toString()));
        
        prefs.commit();
    }
    
}