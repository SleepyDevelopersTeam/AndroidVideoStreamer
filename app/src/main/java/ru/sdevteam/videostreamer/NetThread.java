package ru.sdevteam.videostreamer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by user on 25.08.2016.
 */
public class NetThread implements Runnable
{
	private Thread th;
	private Socket socket;
	private DataOutputStream _out;
	private DataInputStream _in;
	private String host;
	private int port;

	private static NetThread instance;

	private NetThread(String host, int port)
	{
		this.host = host;
		this.port = port;
		th = new Thread(this);
	}

	public static void init(String host, int port)
	{
		if (instance == null)
		{
			instance = new NetThread(host, port);
			instance.start();
		}
	}
	public static NetThread getInstance()
	{
		return instance;
	}

	public static DataOutputStream out()
	{
		return instance._out;
	}
	public static DataInputStream in()
	{
		return instance._in;
	}

	@Override
	public void run()
	{
		try
		{
			socket = new Socket(InetAddress.getByName(host), port);
			_out = new DataOutputStream(socket.getOutputStream());
			_in = new DataInputStream(socket.getInputStream());
			_out.writeUTF("HELLO SERVER");
			_out.flush();

			String response = _in.readUTF();
			System.out.println("[SERVER]" + response);

			Thread.sleep(30);

			_out.writeUTF("Fuck you again, server!");
			_out.flush();
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void start()
	{
		th.start();
	}

	public void stop()
	{
		try
		{
			socket.close();
		}
		catch (IOException ex)
		{
			// ignore
		}
		boolean stopped = false;
		while (!stopped)
		{
			try
			{
				th.join();
				stopped = true;
			}
			catch (InterruptedException ex)
			{
				// fuck! again...
				stopped = false;
			}
		}
	}
}
