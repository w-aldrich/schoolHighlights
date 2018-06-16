package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateServer {

	private static ServerSocketChannel serverSocket;
	private static Certificate serverCertificate;
	private PublicKey serverPublicKey; 
	 
	/**
	 * Create and run a Server. Will listen for client connections until shut down
	 */
	public CreateServer() {
		serverSocket = null;
		Selector socketSelector = null;
		try {
			//create, and bind the server to a port number
			serverSocket = ServerSocketChannel.open().bind(new InetSocketAddress(8080));
			//must be false to accept incoming requests
			serverSocket.configureBlocking(false);
			socketSelector = Selector.open();
			//register server and the selector to accept requests
			serverSocket.register(socketSelector, SelectionKey.OP_ACCEPT);
			//Certificate Factory tells what kind of certificate it is
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			//grab the certificate from the computer
			serverCertificate = cf.generateCertificate(new FileInputStream("serverSelfSignedCert.cert"));
			//create the public key from the certificate
			serverPublicKey = serverCertificate.getPublicKey();
		} catch (IOException e) {
			System.out.println("Resource already open, shut down current resource.");
			System.exit(1);
		} catch (CertificateException e) {
			System.out.println("Loading Server Certificate Failed: closing Resource");
			e.printStackTrace();
			System.exit(1);
		}
		
		//Create a pool to only allow 100 clients at a time
		final ExecutorService pool = Executors.newFixedThreadPool(100);
		
		while (true) {
			try {
				// waits for an event (We are waiting for accept)
				socketSelector.select();
				// creates a "list" (unordered) of all the events that happened
				Set<SelectionKey> keys = socketSelector.selectedKeys();
				// Allows us to go through all of the keys
				Iterator<SelectionKey> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					// if the event was an accept event remove the key and serve the event
					if (key.isAcceptable()) {
						it.remove();
						serveClient(pool, serverSocket);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Accepts client into the pool, establishes a new
	 * ClientConnection and executes the runnable.
	 * @param pool
	 * @param serverSocket
	 */
	private void serveClient(ExecutorService pool, ServerSocketChannel serverSocket) {
		final SocketChannel client;
		try {

			// the server socket accepts the client
			client = serverSocket.accept();
			System.out.println("\nClient Accepted " + client.getRemoteAddress());

			// create a thread for the user, add it to the pool
			ServerClientConnection connection = new ServerClientConnection(client, serverCertificate);
			pool.execute(connection);

		} catch (IOException e) {
			System.out.println("Client accept failed. IO Exception.");
			e.printStackTrace();
		}
	}
	
}
