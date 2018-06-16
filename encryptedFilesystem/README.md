# README FOR THE ENCRYPTED FILE SYSTEM

## To run the program
This program utilizes Python 3 to run
To run the program type the following code.
```
python3 enfcsStarterCode.py EncryptedFiles/ DecryptedFiles/
```
When prompted, the password is "password"

## To see things happen
Open a new terminal, you should be able to see the file "hello" in the EncryptedFiles directory
as well as the "hello" file in the DecryptedFiles directory. The EncryptedFiles directory
contains the encrypted versions of the files, and the DecryptedFiles directory contains the
decrypted files.

## Code to run
### cat
You can "cat" the hello file in either directory. In the DecryptedFiles directory when you "cat"
the file, you should be able to see the word "hello". The code to do this is,
```
cat DecryptedFiles/hello
```
### echo
You can echo into a new file. Try the code
```
echo "Test" > DecryptedFiles/test
```
This will create a new file "test" in both DecryptedFiles and EncryptedFiles. In the decrypted version,
it has "Test" inside of it.

You can also echo into an existing file.
