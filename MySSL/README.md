# mySSLServer 
This is a multithreaded server utilizing a Fixed Thread Pool (size 100) to serve its clients. The server was created in Java as its language, utilizing the Eclipse IDE.
Completes and verifies the handshake from the client. If the Handshake cannot complete or is wrong, shuts down the client as a safety precaution and waits for another Client.

# mySSLClient
This is the client that connects to the server. This was created in Java as its language, utlizing the Eclipse IDE. Completes and verifies the handshake from the server. If the handshake cannot complete or is wrong, disconnects from the Server as a safety precaution.

### The protocol for both Server and Client is as follows
___
| Client | Server |
|--------|--------|
| Self Signed Certificate -> | |
| | <- Self Signed Certificate, RC+{R1} |
| RS+{R2} -> | |
| | <- Hash{RC+, RS+, R1, R2, S}, SERVER |
|Hash{RC+, RS+, R1, R2, S, (Hash From Server)}, CLIENT ->| |
| |<- File|
  
---- Connection closed ------  
  
RC+ is the public key from the client R1 is a nonce. RC+ encrypts R1 using RSA encryption.  
RS+ is the public key from the server R2 is a nonce. RS+ encrypts R2 using RSA encryption.   
The Hash used is a SHA-1 hash.  
S is the master secret key. It is R1 XOR R2.  
SERVER and CLIENT are literal strings of the words "SERVER" and "CLIENT"
The server then sends a file to the client   
___
### The File protocol is 
AuthenticationKey{ File Length, File Type }   
Sequence #, EncryptionKey{ data }, hash { Sequence #, data}    
(The File Type is the file extension. Eg. .txt, .png, .pdf)   
