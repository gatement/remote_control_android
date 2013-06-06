package net.johnsonlau.remotecontrol.activity;

import net.johnsonlau.remotecontrol.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends Activity {

	private Button mBackButton;

	// @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		initMembers();
		bindEvents();
	}

	// == initialization methods
	// ===============================================================

	private void initMembers() {
		mBackButton = (Button) findViewById(R.id.about_back);
	}

	private void bindEvents() {
		bindBackButtonClick();
	}

	private void bindBackButtonClick() {
		this.mBackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				goToMainActivity();
			}
		});
	}

	// == helpers
	// ============================================================================

	private void goToMainActivity() {
		setResult(RESULT_OK);
		finish();
	}
}