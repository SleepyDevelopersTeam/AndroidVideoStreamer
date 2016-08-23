package ru.sdevteam.videostreamer;

import android.hardware.Camera;

/**
 * Created by user on 23.08.2016.
 */
public class CameraManager implements Runnable
{
	private Listener l;
	private Thread th;
	private Camera cam;

	public CameraManager(Listener recepient)
	{
		l = recepient;
		th = new Thread(this);
	}

	@Override
	public void run()
	{
		try
		{
			releaseCamera();
			cam = Camera.open(0);
			l.onCameraObtained(cam);
		}
		catch (Exception ex)
		{
			System.out.println("Error opening camera: " + ex.getMessage());
			l.onCameraError(ex);
		}
	}

	public void obtainCamera()
	{
		th.run();
	}

	public void releaseCamera()
	{
		if (cam != null)
			cam.release();
		cam = null;
	}

	public interface Listener
	{
		void onCameraObtained(Camera obtained);
		void onCameraError(Exception ex);
	}
}
