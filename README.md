# School Projects

The following are highlighted school projects. Inside each file there is an individual README explaining in more detail each program. The following are short descriptions.

## AsteroidsGame

This is a game created during my first semester of the [Master of Software Development (MSD)](https://msd.utah.edu/) program. It utilizes [SFML](https://www.sfml-dev.org/) for the graphics. This was completed as a pared programming assignment with [Ted Pochmara](https://github.com/TedPoch).

## ChatRoom

Created a multithreaded web server that implements HTTP and Web socket protocols on top of TCP sockets. Utilizes JSON formatting for communication between server and both web client and Android client.

## MySSL

Implemented a SSL Handshake between client and server utilizing self signed certificates for authentication. Once a secure connection is established, Server sends an encrypted version of a specified file to the Client then closes the connection. Utilizes RSA encryption for the transfer of nonces and the encryption for the file.

## encryptedFilesystem
Utilized FUSE to write a pass through file system in user space. Encrypted files contain 16 bytes of a random salt along with the encrypted version of the file. When script is running allows for a directory of unencrypted information for user to look at.
