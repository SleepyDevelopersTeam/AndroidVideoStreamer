package ru.sdevteam.videostreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends AppCompatActivity
{
	CameraPreview preview;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getSupportActionBar().hide();

		setContentView(R.layout.activity_camera);

		preview = (CameraPreview)findViewById(R.id.cameraPreview);
		if (preview == null)
		{
			// fuck
			System.out.println("[ERROR] Preview is null!");
		}
	}

	public void onExitButtonClick(View sender)
	{
		//((CameraPreview)findViewById(R.id.cameraPreview)).isOpen = false; // returns null? wtf?!
		NetThread.getInstance().closeConnection();
		finish();
	}

	public void onFoneResetButtonClick(View sender)
	{
		NetThread.getInstance().resetFone();
	}
}
