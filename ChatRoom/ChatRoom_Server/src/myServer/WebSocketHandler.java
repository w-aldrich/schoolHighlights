package myServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class WebSocketHandler{
	private Connection clientConnection;
	private Message message;

	/*
	 * This class handles webSocket events. Keeps a loop open to listen for events
	 *
	 * @param Socket client
	 */
	WebSocketHandler(Connection clientConnectionInput, Pipe pipe) {
		this.clientConnection = clientConnectionInput;
		Selector webSelector = null;
		try {
			//open the selector
			webSelector = Selector.open();
			//configure socketChannel to blocking to false
			clientConnection.getSocketChannel().configureBlocking(false);
			//register the socketChannel
			clientConnection.getSocketChannel().register(webSelector, SelectionKey.OP_READ);

			pipe.source().configureBlocking(false);
			pipe.source().register(webSelector, SelectionKey.OP_READ);
			boolean connectionOpen = true;

			while (connectionOpen) {
				try {
					//set up selector for the WebSockets
					webSelector.select();
					Set<SelectionKey> keys = webSelector.selectedKeys();
					Iterator<SelectionKey> it = keys.iterator();
					//read through all of the keys
					if(it.hasNext()) {
						SelectionKey key = it.next();
						if (key.isReadable()) {
							//must cancel the SocketChannel key for every readable event.
							clientConnection.getSocketChannel().keyFor(webSelector).cancel();
							clientConnection.getSocketChannel().configureBlocking(true);
							//if it comes from a pipe source write the contents
							if (key.channel() == pipe.source()) {
								//cancel the pipe key
								pipe.source().keyFor(webSelector).cancel();
								//configure the blocking to to true for the pipe
								//this allows the contents to actually be readable.
								pipe.source().configureBlocking(true);
								//create an input stream that will allow the contents to be readable
								InputStream pipeStream = Channels.newInputStream(pipe.source());
								//The ObjectInputStream allows the input stream to be read as an object
								ObjectInputStream ois = new ObjectInputStream(pipeStream);
								//convert the object to a string
								String decryptedString = (String) ois.readObject();
								//send message over the webSocket
								webSocketResponse(decryptedString);
								//set the blocking back to false to continue listening for messages
								pipe.source().configureBlocking(false);
								//let the selector start listening for events again
								webSelector.selectNow();
								//re register the pipe to the selector
								pipe.source().register(webSelector, SelectionKey.OP_READ);
							} else if (key.channel() == clientConnection.getSocketChannel()) {
								connectionOpen = readData(clientConnection.getSocketChannel());
							}
						//Check if the socketChannel is open, if it is configure blocking and registration for it.
						}if (clientConnection.getSocketChannel().isOpen()) {
							clientConnection.getSocketChannel().configureBlocking(false);
							webSelector.selectNow();
							clientConnection.getSocketChannel().register(webSelector, SelectionKey.OP_READ);
						}
						else {
							clientConnection.getServer().removeRoom(clientConnection.getRoom().nameOfRoom);
							clientConnection.getRoom().removeUser(clientConnection);
						}
					}
				} catch (IOException e) {
					System.out.println("BAD CLIENT STREAM from thread: " + clientConnection.getName());
					connectionOpen = false;
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					System.out.println("The ObjectInputStream has something wrong with it.");
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			System.out.println("Something went wrong with the WebSelector");
			e1.printStackTrace();
		}
	}



	private boolean readData(SocketChannel client) throws IOException {
		boolean connectionOpen;
		// data input stream can read bytes easily, that is why we use it
		DataInputStream dis = new DataInputStream(client.socket().getInputStream());
		byte[] b = new byte[2];
		// readFully will read however many bytes the array you send it is.
		try {
			dis.readFully(b);
		} catch (EOFException e) {
			// This usually happens with Safari clients.
			System.out.println("EOFException (usually from Safari) Closed Client: " + clientConnection.getName());
			client.close();
			return false;
		}

		// This is the first part of the first byte. Following the webSocket protocol
		// this should be 1000
		// Shift it over and mask it to check and make sure the value is 8
		byte frrr = (byte) ((b[0] >> 4) & 0xF);

		// If frrr is 8, decode message. If not, close the socket and connectionOpen is
		// false
		if (frrr == 8) {
			connectionOpen = decodeMessage(b, dis, client);
		} else {
			client.close();
			System.out.println("Closed Client: " + clientConnection.getName() + " due to bad FRRR: " + frrr);
			connectionOpen = false;
		}
		return connectionOpen;
	}

	/*
	 * This will confirm that all of the information is correct from the
	 * webSocketRequest header If the information is incorrect will return false,
	 * closing the connectionOpen loop.
	 *
	 * @param byte[] of the header, DataInputStream connected to the client socket,
	 * Socket of the client
	 *
	 * @throws IOException //This was to avoid repeated try catch loops in this
	 * function as well as webSocket Request function.
	 *
	 * @returns boolean
	 */
	private boolean decodeMessage(byte[] byteArr, DataInputStream dis, SocketChannel client) throws IOException {

		byte opcode = (byte) (byteArr[0] & 0x0f);

		// if the opcode = 1 it is a text file, this is the only file we want to deal
		// with.
		if (opcode == 1) {
			byte potentialLength = byteArr[1];
			byte mask = (byte) ((potentialLength >> 7) & 0x01);

			// if the mask =1 it is encoded. These are the only files we want to deal with
			if (mask == 1) {
				potentialLength = (byte) ((potentialLength) & 0x7f);

				return webSocketRequest(potentialLength, dis, client);

			} else {
				client.close();
				System.out.println(
						"Closed Client: " + clientConnection.getName() + " due to no incorrect mask: " + mask);
				return false;
			}
		}

		// If it gets this far close the client and return false to discontinue the
		// while loop.
		client.close();
		System.out.println("Closed Client: " + clientConnection.getName() + " due to opcode: " + opcode);
		return false;

	}

	/*
	 * This processes and decodes the webSocket request. Sends to webSocketResponse
	 * to send message
	 *
	 * @param the byte that holds the length, DataInputStream connected to your
	 * client Socket, Socket of the client
	 */
	private boolean webSocketRequest(byte potentialLength, DataInputStream dis, SocketChannel client)
			throws IOException {
		// extra byte if needed
		byte[] extra;
		// byte array that holds the webSocket key
		byte[] key = new byte[4];
		// the actual length of the message
		int actualLength = 0;

		// if the length is less than 126 the value of the byte first passed in is the
		// actual length
		if (potentialLength < 126) {
			actualLength = potentialLength;
		} else if (potentialLength == 126) {
			extra = new byte[2];
			dis.readFully(extra);
			actualLength = new BigInteger(extra).intValue();
		} else {
			client.close();
			return false;
		}

		if(actualLength < 0) {
			actualLength = 0;
		}

		// read the key into the byte array
		dis.readFully(key);



		// make a byte array of the correct size
		byte[] message = new byte[actualLength];

		// read the message
		dis.readFully(message);

		// decode the message
		for (int i = 0; i < message.length; i++) {
			message[i] ^= key[i % 4];
		}

		// String decoded = new String(message);
		String decoded = new String(message);

		//Deal with JSON formatting
		this.message = new Message(decoded, clientConnection);

		return true;

	}

	/*
	 * This is a response to the request. Will return an echo of the message
	 * received.
	 *
	 * @param String of decoded message, Socket client
	 *
	 * @throws IOException
	 */
	synchronized void webSocketResponse(String decoded) throws IOException {
		// a byte array for the header
		byte[] header = new byte[2];
		header[0] = (byte) 0b10000001;
		// set the appropriate header response
		if (decoded.length() < 126)
			header[1] = (byte) decoded.length();
		else
			header[1] = 126;

		// extra bytes if needed runs even if not needed
		byte[] two = new byte[2];
		two[0] = (byte) (decoded.length() / 256);
		two[1] = (byte) (decoded.length() % 256);

		// Actually sending the file starts here
		DataOutputStream dos = new DataOutputStream(clientConnection.getSocketChannel().socket().getOutputStream());

		// write the header
		dos.write(header);
		// option to add to the header if need be
		if (decoded.length() >= 126)
			dos.write(two);
		// write the message
		dos.write(decoded.getBytes());
		// send the message
		dos.flush();
	}

	public Message returnMessage() {
		return message;
	}

}
