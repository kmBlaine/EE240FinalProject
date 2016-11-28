HOW TO USE THIS PROGRAM:

1. Download the repository

2. Open a text console (cmd in Windows, terminal of some kind in Linux/Mac).
   Enter these commands to run:

	  cd [path/to/download/location]/src/
	  java EE240Program
	
3. The program will calculate the numeric solution of our assigned geometry to
   an Epsilon of 0.01 for acceleration factors of 1, 1.5, and 1.7. It will do so
   for mesh granularities of 1mm and 0.5mm. The results will be written to a
   .csv file and a .log file will be created that details the max epsilon per
   step and where it occurred. (These were the requirements of the project)
   

TO RECOMPILE WITH DIFFERENT PARAMETERS:

1. Open EE240Program.java in a text editor of some kind

2. Make necessary changes such as changing output file name, mesh granularity,
   etc. The program handles mesh granularity internally so you do not need to
   change the geometry dimensions.
   
3. Recompile. YOU WILL NEED THE JDK FOR THIS, NOT JUST THE JRE!
   In Linux/Mac just type:

      javac EE240Program
	
   Windows will be a bit trickier because reasons. Do this:

	  cd C:\Program Files\Java\jdk[some version number]\bin\
	  javac [path\to\download\location]\src\EE240Program.java
	
   You may also need to compile the PotentialGrid.java file. Try this if the
   compiler screams at you.
