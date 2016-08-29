package ru.sdevteam.videostreamer;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by user on 23.08.2016.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, android.hardware.Camera.PreviewCallback
{
	//SurfaceView surface;
	SurfaceHolder holder;
	Camera camera;
	int dataLen;

	public boolean isOpen;

	public CameraPreview(Context ctx, AttributeSet whoFuckingCares)
	{
		super(ctx);

		dataLen = 0;
		isOpen = false;

		//surface = new SurfaceView(ctx);
		//addView(surface);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		try
		{
			camera = Camera.open();
			//camera.setDisplayOrientation();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.println("[CRITICAL] Camera is not available. " + ex.getMessage());
			System.exit(0);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder)
	{
		System.out.println("[EVENT] Surface created");
		try
		{
			camera.setPreviewDisplay(holder);
			camera.setPreviewCallback(this);
			// unsupported
			//Camera.Parameters params = camera.getParameters();
			//params.setPreviewFormat(ImageFormat.RGB_565);
			//camera.setParameters(params);
			camera.startPreview();
		}
		catch (IOException ioex)
		{
			System.out.println("[CRITICAL] Cannot start preview: " + ioex);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2)
	{
		System.out.println("[EVENT] Surface changed");
		if (holder.getSurface() == null){
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			camera.stopPreview();
		} catch (Exception e){
			// ignore: tried to stop a non-existent preview
			System.out.println("[CRITICAL] Camera can't stop preview");
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			camera.setPreviewDisplay(holder);
			camera.setPreviewCallback(this);
			camera.startPreview();

		} catch (Exception e){
			System.out.println("[CRITICAL] Error starting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder)
	{
		System.out.println("[EVENT] Surface destroyed");
		try
		{
			camera.stopPreview();
			camera.setPreviewCallback(null);
			holder.removeCallback(this);
			camera.release();
		}
		catch (Exception ex)
		{
			System.out.println("[ERROR] While releasing camera: " + ex.getMessage());
			ex.printStackTrace();
		}
		finally
		{
			camera.release();
			camera = null;
		}
	}

	@Override
	protected void onLayout(boolean b, int i, int i1, int i2, int i3)
	{

	}

	private byte[] testData;

	@Override
	public void onPreviewFrame(byte[] bytes, Camera camera)
	{
		if (dataLen == 0)
		{
//			dataLen = 90000;
//			isOpen = true;
//			testData = new byte[dataLen];
//			for (int i = 0; i < testData.length; i++)
//			{
//				testData[i] = (byte) (i % 10);
//			}
//			NetThread.getInstance().endHandshake(dataLen);
			dataLen = bytes.length;
			isOpen = true;
			NetThread.getInstance().endHandshake(dataLen);
		}
		else if (isOpen)
		{
//			NetThread.getInstance().sendData(testData);
			NetThread.getInstance().sendData(bytes);
		}
	}
}
