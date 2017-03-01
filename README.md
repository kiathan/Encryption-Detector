# Encryption-Detector

This is a simple Java Application that reads and process files, and determine if the file is encrypted or not.

It returns a confidence level of 0-99% if the file is encrypted.

A file can turn out to be compressed instead of encrypted, hence a confidence level of less than 75% could be a compressed file instead of an encrypted file.

It uses the Monte Carlo Pi and Chi-Square Distribution to determine these confidence level.


To run:

`java EncryptionDetector FILE [FILE...]`
