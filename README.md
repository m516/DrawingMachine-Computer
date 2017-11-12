If you are reading this text, congratulations! You are the first person to visit this repository! I might not have a lot of stars or downloads or forks, but I have written this code and published it so that you can see, edit, use, reverse-engineer, or critique my code.  Please feel free to contact me at any time

# DrawingMachine-Computer

This repository contains the original source code for the Java application that runs on a computer.
The program provides a GUI to allow users to open images of (nearly) every type, convert them to
a single path (for black and white prints) or multiple paths (2-8 colors), and send the points on 
each path to an arduino that serves as a printer.  Sample Arduino programs can be found 
[here](https://github.com/ThePowerRule/DrawingMachine-Arduino)

Due to homework, college applications, and the like, I will not have much time
to work on this repo. And the time I invest in this project will be spent on bug-fixing, 
tweaking, and improving the application rather than providing detailed documentation because the 
computer app seems pretty intuitive.

I am currently working on this application in Eclipse on an old desktop running Windows 7.  If you 
would like to work on the source code, import the project with the Eclipse extension, or use a 
separate Git client (e.g. SmartGit, Github Desktop, etc.) and import the source code as an Eclipse 
project.

Questions?  Comments?  Concerns?  Shoot me an email at mm44928@wdmcs.org

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

- [ ] Write a new program (in Python?) to print generated csv.  This way anyone with Python (e.g. 
Raspberry Pi) can print with a connection to Arduino

## Notes
- The binary is now a single executable JAR

- DrawingMachine has been tested on multiple Windows machines, and the application works fine.  However, it does not start up on a Raspberry Pi running a flavor of Debian.  Machines running on flavors of OS X and Linux will not print to an Arduino because the application searches for the Arduino exclusively on the USB port COM3.

Please email me if you have any questions or comments.  My email is mm44928@wdmcs.org.
