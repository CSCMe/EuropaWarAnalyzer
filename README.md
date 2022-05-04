# EU4 war analyzer

Based on the Victoria II war analyzer: 
* [Forum post](https://forum.paradoxplaza.com/forum/index.php?threads/tool-victoria-ii-save-game-war-analyzer.689055/)
* [Github](https://github.com/TKasekamp/VickyWarAnalyzer/)
## Download links

* Current version download: [v1.0.0a](https://github.com/CSCMe/EuropaWarAnalyzer/releases/tag/v1.0.0a)
* You can find the releases in the releases tab or the dist folder. 

## What is it
The analyzer reads the save game produced by EU4 (it's a strategy game, look it up) and presents all the wars in a family-friendly way. The program retrieves all the data that can be retrieved from the save file, such as total losses in a war, all the battles, wargoals and the war participants.

Most of the counties have a flag with them. Colonial nations and some others won't have one though.

This analyzer is NOT a fully-fledged save game analyzer. It does one thing and does it reasonably well.

### Instructions
1. Make sure you have Java 8 installed on your computer
2. Run the jar file
3. Specify the save game. Usually the save games are in `C:\Users\USERNAME\Documents\Paradox Interactive\Europa Universalis IV\save games\`
4. Optionally you can point to the EU4 install directory. The analyzer will retrieve the country names from there. 
5. Click "Read file" and see how terrible your wars have been. 
6. The analyzer will create a file called "paths.txt" in the jar directory. This stores the path to the last used save game directory.

### Screenshots
All wars tab:
![alt text](https://i.imgur.com/GhLNoyi.png "All wars tab")
War details tab:
![alt text](https://i.imgur.com/D79WeBC.png "War details tab")
Battle details tab:
![alt text](https://i.imgur.com/6MX2Jdk.png "Battle details tab")
Wargoals tab:
![alt text](https://i.imgur.com/vchWN5z.png "Wargoals tab")


## Bugs and limitations
###Limitations
* The analyzer will produce many errors when it can't find the flag or the name for a country. Just ignore it.

* When the player country has fought no wars then it's name will be a TAG even if localisation is used.

* The analyzer will only show country names and flags for vanilla countries that have participated in at least 1 war.

* Colonial Nations that got annexed won't have a localised name

* The Personal Union casus belli is a bit funky (that's just how the game works)

###Bugs

* Please report any bugs you find

## Tools
* Java 8
* JavaFX
* Maven 
* Maven JavaFX plugin https://github.com/zonski/javafx-maven-plugin

### Build process
If you really want to build your own version of this then go right ahead. I'm going to assume you know what Maven is and how to install it. You need to have Java 8.

1. Download this repository
2. Run `mvn install` in the base directory. Everything should install without problems.
3. Run `mvn jfx:jar` to compile it into a jar. The result an be found in `target/jfx/app`.
4. Use `mvn eclipse:eclipse` to generate the project files for Eclipse. You might also need the m2e Eclipse plugin.

### Code 
The UI was designed to work with Java 7, but apparently migrating to Java 8 made some parts look weird.
Due to this, some words will be hidden, and some tables will have empty columns.

TKasekamp 

## About
[Vicky War Analyzer](https://github.com/TKasekamp/VickyWarAnalyzer) was Tkasekamp first big Java project.
He started working on it in April 2013 and released it in the Paradox forums where ~ 1500 people downloaded it!
In April 2015 he made it work with Java 8.

I adapted it to work with EU4 saves, but most of his work remains unchanged.

The save game is read in by a hand-made parser. It works  ¯\_(ツ)_/¯
