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
	private class ServerException extends Exception
	{
		ServerException(String msg)
		{
			super(msg);
		}
	}
	// as STDUDTP3K defines
	private static final byte DATA = 0x00;

	private static final byte HELLO_SERVER = 0x1E;
	private static final byte HELLO_CLIENT = 0x1A;

	private static final byte DATA_RECEIVED = 0x2D;

	private static final byte LENGTH_CHANGE = 0x31;
	private static final byte FONE_RESET = 0x3F;
	private static final byte COMMAND_EXECUTED = 0x3E;

	private static final byte GB_SERVER = 0x45;
	private static final byte GB_CLIENT = 0x4C;

	private static final byte ERROR = 0x66;


	private Thread th;
	private Socket socket;
	private DataOutputStream _out;
	private DataInputStream _in;
	private String host;
	private int port;

	private volatile boolean opened;

	private static NetThread instance;

	private NetThread(String host, int port)
	{
		this.host = host;
		this.port = port;
		opened = false;
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

//	public static DataOutputStream out()
//	{
//		return instance._out;
//	}
//	public static DataInputStream in()
//	{
//		return instance._in;
//	}

	@Override
	public void run()
	{
		try
		{
			socket = new Socket(InetAddress.getByName(host), port);
			_out = new DataOutputStream(socket.getOutputStream());
			_in = new DataInputStream(socket.getInputStream());

			opened = true;

			while (true)
			{
				synchronized (mutex)
				{
					while (!deferred)
					{
						try
						{
							mutex.wait();
						}
						catch (InterruptedException ex)
						{
							System.out.println("[ERROR] Interrupted!");
						}
					}
					if (!opened) // connection were closed while we were awaiting
						break;
					// executing deferred cmd
					try
					{
						writeDeferred();
					}
					catch (ServerException ex)
					{
						System.out.println(ex.getMessage());
						throw ex;
					}
					catch (Exception ex)
					{
						System.out.println("[ERROR] While writing deferred!");
						ex.printStackTrace();
						throw ex;
					}
					finally
					{
						clearDeferreds();
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("[CRITICAL] Error: " + e.getMessage());
			e.printStackTrace();
			try
			{
				socket.close();
			}
			catch (IOException ex)
			{
				// nothing
			}
			opened = false;
			//System.exit(0);
		}
	}

	// Main protocol logic
	private void writeDeferred() throws IOException, ServerException
	{
		synchronized (mutex)
		{
			// special case for continuing handshake;
			// after this, server still waits our further commands,
			// and this thread waits for somebody outside
			if (handshakeLengthDeferred)
			{
				_out.writeInt(deferredLength);
				deferred = false;
				handshakeLengthDeferred = false;
				return;
			}

			// everything below in this method should implement SDTUDTP3K!
			// TODO: update protocol specification to hold width and height

			_out.writeByte(deferredCommand);
			switch (deferredCommand)
			{
				case DATA:
					_out.writeShort(imgW);
					_out.writeShort(imgH);
					synchronized (data) // data reference won't change because we're inside synchronized(mutex)
					{
						_out.write(data);
					}
					expectAnswer(DATA_RECEIVED, "Server couldn't receive data!");
					break;

				case LENGTH_CHANGE:
					_out.writeInt(deferredLength);
					// protocol requires not to fail here
					try
					{
						expectAnswer(COMMAND_EXECUTED, "Server failed to change length!");
					}
					catch (ServerException ex)
					{
						System.out.println(ex.getMessage());
					}
					break;

				case GB_SERVER:
					expectAnswer(GB_CLIENT, "Impolite server thinks he should not say goodbye to us!");
					socket.close();
					opened = false;
					break;

				case FONE_RESET:
					expectAnswer(COMMAND_EXECUTED, "Server failed to reset fone!");
					break;

				case HELLO_SERVER:
					expectAnswer(HELLO_CLIENT, "I would never connect to such rude server!");
					break;

				default:
					// anything else is error
					throw new IllegalArgumentException("Invalid command: " + deferredCommand);
					//break;
			}

			deferred = false;
		}
	}
	private void clearDeferreds()
	{
		deferred = false;
		handshakeLengthDeferred = false;
	}
	private void expectAnswer(byte expected, String message) throws ServerException, IOException
	{
		byte result = _in.readByte();
		if (result != expected)
			throw new ServerException(message);
	}

	//
	// Thread Manipulation
	//
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

	//
	// Deferreds
	//
	private volatile byte deferredCommand;
	private volatile boolean deferred, handshakeLengthDeferred;
	private volatile byte[] data;
	private volatile int deferredLength;
	private volatile short imgW, imgH;
	private final Object mutex = new Object();

	private void deferCommand(byte cmd)
	{
		// TODO: try while(this.deferred) mutex.wait();
		synchronized (mutex)
		{
			if (this.deferred)
			{
				System.out.println("[ERROR] Commands are too fast!");
				//mutex.notifyAll();
				return;
			}
			deferred = true;
			deferredCommand = cmd;
			mutex.notifyAll();
		}
	}
	private void deferChangeLength(byte cmd, int len)
	{
		synchronized (mutex)
		{
			if (this.deferred)
			{
				System.out.println("[ERROR] Commands are too fast!");
				//mutex.notifyAll();
				return;
			}
			deferred = true;
			deferredLength = len;
			deferredCommand = cmd;
			mutex.notifyAll();
		}
	}
	private void deferData(short w, short h, byte[] data)
	{
		synchronized (mutex)
		{
			if (this.deferred)
			{
				System.out.println("[ERROR] Commands are too fast!");
				//mutex.notifyAll();
				return;
			}
			deferred = true;
			this.data = data;
			imgW = w; imgH = h;
			deferredCommand = DATA;
			mutex.notifyAll();
		}
	}
	private void deferHandshakeLength(int dataLen)
	{
		synchronized (mutex)
		{
			if (this.deferred)
			{
				System.out.println("[ERROR] Commands are too fast!");
				//mutex.notifyAll();
				return;
			}
			deferred = true;
			deferredLength = dataLen;
			handshakeLengthDeferred = true;
			mutex.notifyAll();
		}
	}

	//
	// Public API
	//
	public void startHandshake()
	{
		deferCommand(HELLO_SERVER);
	}

	public void endHandshake(int dataLen)
	{
		deferHandshakeLength(dataLen);
	}

	public void closeConnection()
	{
		deferCommand(GB_SERVER);
	}

	public void changeDataLength(int dataLen)
	{
		deferChangeLength(LENGTH_CHANGE, dataLen);
	}

	public void resetFone()
	{
		deferCommand(FONE_RESET);
	}

	public void throwError()
	{
		deferCommand(ERROR);
	}

	public void sendData(short w, short h, byte[] data)
	{
		deferData(w, h, data);
	}

}
