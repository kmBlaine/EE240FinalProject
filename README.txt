HOW TO USE THIS PROGRAM:

1. Download the repository

2. Open a text console (cmd in Windows, terminal of some kind in Linux/Mac). Enter these commands
   to run:
	$ cd [path/to/download/location]/out/Production/EE240FinalProject/
	$ java EE240FinalProject
	
3. The program will create a .csv file with the results. By default, mesh granularity is 1div/mm
   initial guess of 50V, calculates to epsilon of 0.1. Geometry is programmed. To change these 
   values, edit EE240FinalProject.java and recompile.
   
TO RECOMPILE WITH DIFFERENT PARAMETERS:

1. Open EE240FinalProject.java in a text editor of some kind

2. Make necessary changes such as changing output file name, mesh granularity, etc. The program
   handles mesh granularity internally so you do not need to change the geometry dimensions.
   
3. Recompile. YOU WILL NEED THE JDK FOR THIS, NOT JUST THE JRE!
   In Linux/Mac just type:
	$ javac EE240FinalProject
	
   Windows will be a bit trickier because reasons. Do this:
	$ cd C:\Program Files\Java\jdk[some version number]\bin\
	$ javac [path/to/download/location]/src/EE240FinalProject
	
   You may also need to compile the PotentialGrid.java file. Try this if the compiler screams at
   you.
   
4. Copy the files [path/to/download/location]/out/Production/EE240FinalProject/ or just run in place