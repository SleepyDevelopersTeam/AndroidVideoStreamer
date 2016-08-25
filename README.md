# AndroidVideoStreamer #
Simple android app that can connect to a specified address and stream data to it

## Connection protocol ##

Application uses ultra-modern communication protocol SDTUDTP3K (SDevTeam Ultra Data Transfer Protocol 3000).
Here is its specification:

### Commands ###

Server and client exchange with raw binary data (not strings!).
Commands may be of 2 types: **control command** and **data command**.
**Control command** is one single byte of data with following values (**INPORTANT!** Following constants are of type signed byte, `sbyte`, `char` in C++ or `byte` in Java):

(**client** can send following commands):

- `0x1E`: **"Hello server"** message;
- `0x31`: client is to change **data command** length (*unused now*) (**IMPORTANT!** This command is followed with 4 bytes of length!);
- `0x3F`: **"Fone reset"** command;
- `0x45`: **"Goodbye server"** message;

(**server** can send following commands):

- `0x1A`: **"Hello client"** message;
- `0x2D`: **"Data received"** server response;
- `0x3E`: **"Command executed"** message;
- `0x4C`: **"Goodbye client"** message;

(both **server** and **client** can send following commands):

- `0x66`: some error occured;


**Data command** is a bulk array of image data bytes, following the single byte with value `0x00`
(to differ **data command** from **control command**).
This type of command can be sent only from client side.

### Communication ###

1. Handshaking:
  1. **Client** (who starts the connection) sends **"Hello server"** message (`0x1E`).
  2. **Server** sends **"Hello client"** message (`0x1A`).
  3. **Client** sends `4` bytes that represent **data command** data length (_without_ considering `0x00` byte!).

2. After handshaking, data exchange starts:
  1. **Client** sends **data command**.
  2. **Server** sends **"Data received"** (`0x2D`) message.
  3. Can be repeated till the end of days.

3. If **client** answers with not a **data command** (first byte is not zero), it is a **control command**:
  3. **Client** sends a command (see list above).
  3. **Server** executes the command and sends **"Command executed"** message (`0x3E`) if succeeded.
  3. **Server** sends error message (`0x66`) if some error has occured while executing the command. Communication **SHOULD NOT** stop in this case.

4. Closing the connection:
  4. **Client** is *always* the initiator of closing the connection.
  4. **Client** sends **"Goodbye server"** message (`0x45`).
  4. **Server** sends **"Goodbye client"** message (`0x4C`).
  4. **Server** closes the connection (socket).

5. Error handling:
  5. If **server** has some internal error (important: **not** during execution of client command (3.2)), it sends error message `0x66`.
  5. If **client** has some internal error, it sends error message `0x66`.
  5. Crashed device doesn't have to close socket or send any other data. The other device should properly handle this situation.
  5. If **client** crashes, server returns to "wait mode", awaiting a client to connect.
  5. If **server** crashes, client returns to "connect" screen and should be ready for further attempts to connect to server.

### Data Format ###

Image is sent as raw byte sequence in format `RGB_565`.
