# DrawingMachine-Computer

This repository contains the original source code for the Java application that runs on a computer.
The program provides a GUI to allow users to open images of (nearly) any type, convert them to
a single path (for black and white prints) or multiple paths (2-8 colors), and send the points on 
each path to an arduino that serves as a printer.  

Sample Arduino programs can be found [here](https://github.com/QuarksAndLeptons/DrawingMachine-Arduino).

A Python script for printing from a CSV file can be found [here](https://github.com/QuarksAndLeptons/DrawingMachine-Computer-Lite).

## Bugs

- [ ] JavaFX services for designing and printing the image do not reset after prints,
so the program will only print one time.

- - NOTE: CSV's can be exported multiple times, but direct prints cannot.

- [ ] Search for Arduino serial connection on devices other than COM3

- [ ] Allow users to cancel prints with a cancel button

- [ ] Terminate all threads with close button (after dialog warning)

- [X] Put executable JAR in release


## To Do:
- [ ] Configuration window

- [X] Export generated path to csv

- [ ] Print from generated csv file

- [X] Write a new program (in Python?) to print generated csv.  This way anyone with Python (e.g. 
Raspberry Pi) can print with a connection to Arduino

- [ ] The JavaFX library is required for building the project from source code. It would be nice if that was
included in the source code

- [ ] Upgrade to Java 9+

## Notes
- The binary is now a single executable JAR.  It will only work on Windows machines with 64-bit architecture since it contains several JavaFX classes.

- If you are using a Mac or Linux machine, simply open the source code in Ecipse or Netbeans and replace the JavaFX library. You will need to change the USB address in the ArduinoSerialComm class from "COM3" to the address of your Arduino, such as "/dev/tty/ACM0" on Debian or Ubuntu.
