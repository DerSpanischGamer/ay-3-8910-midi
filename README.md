# ay-3-8910-midi
## An interface in Python and C for the AY-3-8910 (or YM2149) that uses an Arduino (or ATmega328-P)

To be able to send midi files, first they must be converted to .csv with this program : https://www.fourmilab.ch/webtools/midicsv/#Download

(or any other that transforms .mid to .csv)

## File explanation

### Folder *AY3910RegWrite (original file for 1 chip connected to an Arduino)*

As its name indicates, in this folder, there's the code to for just 1 IC connected to an Arduino.
[Conexiones.png](https://github.com/DerSpanischGamer/ay-3-8910-midi/blob/master/Conexiones.png)

### Folder *[https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/AY3910RegWrite-library](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Amadeus)*

Code that supports 2 chips simultaneously. Is simplified version of the preivous code and made easier to read thanks to the fact that it has been made into a [library](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Amadeus).

### Folder *Amadeus*

Is the library that makes everything cleaner and easier to read.

The only functions to keep in mind are:

Import the library
`#include "Amadeus.h"`

Create and instance of the library
`Amadeus amadeus = Amadeus(); // Init Amadeus clss`

Start the clock and configure the pins
`amadeus.begin();`

Write the value VAL to register REG of chip C
`amadeus.out(C, REG, VAL);`

The rest of the functions are private as they are only used to carry out the functions described above. If you are curioused you can take a look at the code.

### Folder *[Canciones](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Canciones)*

Not much to say. Stores some songs for which I have searched the .mid file adn I have transformed into .csv to be able to play them.

### Folder *[Programas](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Programas)*

Boy oh boy, is here where the fun begins. First, teh files .exe should be ignore (for the most part).

- **Controlador Registros Dual YM2149.exe** : is a program that I am developping to control all the registers in a more visual way. IT ISN'T FINISHED AND IT SURELY HAS ERRORS.

- **Midicsv.exe** : Transforms a .mid file into a .csv. To use it you need cmd and the command is `Midicsv.exe "FILE.mid" "OUTPUT.csv"`. FILE is the file you want to transform and OUTPUT is the name of the .csv you desire to have.

- **bin2hex.exe** : Should be ignored, I don't even know what it does...

- **hexdump.exe** : Useful to make a hexdump in cmd.

Alright, we are at the .py files that are interesting:

- **amadeusguardar.py** : useful to save a song that is in .csv from a .mid file into a .amds file (it is a .csv file but I have put my own extension so I don't get confused and to make it cooler hehehe).

The way it stores data is the following:

'INFOS,BPM,CHIP

TIMESTAMP,VAL,VAL,VAL...

TIMESTAMP,VAL,VAL,VAL...

...'

where INFOS is a string that never changes, BPM is the BPM of the song and CHIP is the number of chips that are needed to play the song.

.amds only stores the timestamps where there's a change in any of the registers. That's why TIMESTAMP doesn't increment from 0 to the end of the file, but there are jumps. This means that the notes are held for all that time.

Use: `python amadeusguardar.py "FILE.csv" "OUTPUT.amds"`

Note: you can open .amds files with Notepad++, Libreoffice Calc or Excel.

- **amadeussender_legacy.py** : used to be able to use both ICs of an [Amadeus board] (https://www.youtube.com/watch?v=V24AyQ2n8vY) or an Arduino wired the same way. The difference between this file and **amadeussender.py** is that the latter can manage **6** channels simultaneously, instead of 3.

To use: 'python amadeussender_legacy.py "FILE.csv" "PORT"'

IMPORTANT: the data sent by this program are in this order:

* CHIP: (0 -> FIRST, 2 -> SECOND)

* REGISTER: the register that we wish to modify (0 -> 13) (there are registers 14 and 15 but are of no use).

* VALUE: the value that we want to write to the register (0 -> 256) (Note: some registers will ignore over a certain value).

So, if we send the bytes [0x00, 0x01, 0x02] we will be writing the value 0x02 to the register 0x01 of the chip 0x00 (first).

To be able to make it work, we have to upload to the board the correct software.

- **amadeussender.py** : an improved version of **anadeussender.py** that detects different tracks of a midi file, and allows up to 6 channels simultaneously. It does this by looking through all the file looking for notes, then sorts them by their timestamp, and then sending them like **amadeussender_legacy.py**.

How to use: 'python amadeussender.py "FILE.csv" "PORT" "VOLUME=15"'

The default volume is 15. You don't have to write VOLUME=, just the value between 0 and 15 (included) that you want the board to play. Recommendation: 10-12 is not very loud and not very faint.

- **csv-midi.py ** : Under developpement.

- **midisender.py** : does the same as amadeussender.py, except that it only works with a max. of 3 channels. How it works is left as an exercise to the reader.

- **frequency_calculator.py** : Calculates the value that we have to put in the registers of the YM2149/AY-3-8910 to obtain all the possible frequencies. This is necessary because the program **amadeussender.py** and **amadeusguardar.py** needs the frequency for each note.

frequency_calculator.py prints a string with the shape of a python array that then can be directly pasted in a script.

To use: 'python frequency_calculator.py [FREQUENCY IN Hz (OPTIONAL)]'

Ex.: 'python frequency_calculator.py'
or
'python frequency_calculator.py'

=> The first executes the program for the default frequency of 2MHz, meanwhile the 2nd will execute the program for a frequency of 1MHz.

### Folder *[VideoMaker](https://github.com/DerSpanischGamer/ay-3-8910-midi/tree/master/Programas/VideoMaker)* (dentro de la carpeta *Programas*)

Contains the program **cancionVideo.py**. This program is out of date because it uses the old .amds format.

It takes a .amds file and transforms it into a series of frames that then can be made into a video using the program ffmpeg. I leave the command to be used in the file comando.txt

###CREDITS:
 - CODE TO SEND ORDERS TO AN AY-3-8910 OR YM2149 CONNECTED TO AN ARDUINO: https://github.com/986-Studio/AY-3-3910-Player/blob/master/AY3910RegWrite/AY3910RegWrite.ino (ARDUINO SIDE)

 - CODE OF THE GENIOUS THAT MADE THE FT2-CLONE TRACKER FROM WHICH I AM BUILDING MY OWN TRACKER FOR THE AMADEUS BOARD: https://github.com/8bitbubsy/ft2-clone