package ru.sdevteam.videostreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ConnectionActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connection);
		Button button = (Button) findViewById(R.id.connectButton);

		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				connect();
			}
		});
	}

	private void connect()
	{
		EditText addressBox = (EditText) findViewById(R.id.addressTextBox);
		EditText portBox = (EditText) findViewById(R.id.portTextBox);
		TextView error = (TextView) findViewById(R.id.errorLabel);
		ProgressBar bar = (ProgressBar) findViewById(R.id.connectionProgressBar);
		bar.setVisibility(View.VISIBLE);
		try
		{
			String host = addressBox.getText().toString();
			int port = Integer.parseInt(portBox.getText().toString());


			NetThread.init(host, port);
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			error.setText("Error: " + e.getMessage());
			bar.setVisibility(View.INVISIBLE);
			System.exit(0);
		}

		Intent intent = new Intent(this, CameraActivity.class);
		startActivity(intent);
	}
}
