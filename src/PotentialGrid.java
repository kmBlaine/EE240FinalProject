/*
<EE240FinalProject - calculates electric fields numerically using SOR method>
    Copyright (C) 2016  - Blaine Murphy (github/kmBlaine)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* File: PotentialGrid.java
 * 
 * Purpose:
 * This file is the backend for the main method of the program. It automates the
 * creation, modificatoin, and storage of the mesh allowing the user to make simple, human 
 * readable calls to it to set certain regions as fixed, perform calculations, and
 * export data to disk. Handles dimensions, mesh resolution, etc internally.
 */

import java.io.*;

public class PotentialGrid
{
	//static variables used when defining fixed lines of charge
	//intended for keeping setFixedLine() calls consistent and readable
	public static final boolean ORIENTATION_VERTICAL = true;
	public static final boolean ORIENTATION_HORIZONTAL = false;
	
	/* PotentialPoint
	 * 
	 * Purpose: data structure for representing a point of electric potential
	 *   on the grid. Each point caries a potential and an access control flag
	 *   when the access control flag is set, the potential cannot be changed
	 */
	private static class PotentialPoint
	{
		private double voltage;
		private boolean fixed;

		//points will be initialized with 0V potential and modifiable
		public PotentialPoint()
		{
			voltage = 0;
			fixed = false;
		}

		public double getVoltage()
		{
			return voltage;
		}

		public boolean isFixed()
		{
			return fixed;
		}
		
		/* setVoltage
		 *
		 * Purpose: specialized mutator that only allows changes to the point's
		 *   potential when the fixed flag is not set
		 * 
		 * Parameters:
		 *   double voltage - new voltage for the point
		 * 
		 * Returns: nothing
		 */
		public void setVoltage( double voltage )
		{
			if ( !fixed )
			{
				this.voltage = voltage;
			}
		}

		public void setFixed( boolean fixed )
		{
			this.fixed = fixed;
		}
	}

	private PotentialPoint [][] grid;
	private int granularity; //stores the resolution of the grid
	private String log; //string to record calculation metadata into

	/* PotentialGrid
	 * 
	 * Purpose: constructor for the grid. Given an X and Y dimension in millimeters
	 *   creates a new grid with the specified granularity in divsions per mm.
	 *   handles all grid calculations internally. Records changes in log and
	 *   initializes all points as unfixed and 0V potential
	 * 
	 * Parameters:
	 *   int dimX - X dimension of grid in millimeters
	 *   int dimY - Y dimension of grid in millimeters
	 *   int granularity - resolution of the grid in divisons per mm
	 *
	 * Returns: nothing
	 */
	public PotentialGrid( int dimX, int dimY, int granularity )
	{
		log = "Created new grid:" +
				"\n\t" + dimX + "mm X " + dimY + "mm, " +
				"\n\t" + granularity + " divisions per mm\n\n";
		dimX++;
		dimY++;
		grid = new PotentialPoint[dimY * granularity - granularity + 1][dimX * granularity - granularity + 1];
		this.granularity = granularity;

		for ( int row = 0; row < grid.length; row++ )
		{
			for ( int col = 0; col < grid[row].length; col++ )
			{
				grid[row][col] = new PotentialPoint();
			}
		}
	}
	
	//function for getting the total number of mesh rows
	public int getNumberOfRows()
	{
		return grid.length;
	}
	
	//function for getting the total number of mesh columns
	public int getNumberOfColumns()
	{
		return grid[0].length;
	}

	/* setFixedLine
	 * 
	 * Purpose: sets a line of specified size in the mesh to be unchangable
	 *   with the given potential. builds a line in the positive X and Y
	 *   directions from a starting point at (x, y)
	 *
	 * Parameters:
	 *   int size - size of the line in raw mesh units
	 *   int startX - x coordinate of starting point of line. given in millimeters
	 *   int startY - Y coordinate of starting point of line. given in millimeters
	 *   double voltage - fixed voltage to set the line to
	 *
	 * Returns: nothing
	 */
	public void setFixedLine( int size, boolean orientation, int startX, int startY, double voltage )
	{
		startX *= granularity;
		startY *= granularity;
		
		int lineStart = 0;
		
		if ( orientation == ORIENTATION_VERTICAL )
		{
			lineStart = startY;
		}
		else
		{
			lineStart = startX;
		}
		
		for ( int pos = lineStart; pos < ( size + lineStart ); pos++ )
		{
			if ( orientation == ORIENTATION_VERTICAL )
			{
				grid[pos][startX].setFixed( false );
				grid[pos][startX].setVoltage( voltage );
				grid[pos][startX].setFixed( true );
			}
			else
			{
				grid[startY][pos].setFixed( false );
				grid[startY][pos].setVoltage( voltage );
				grid[startY][pos].setFixed( true );
			}
		}
	}
	
	/* setFixedRectangle
	 *
	 * Purpose: creates a rectangle on the interior of the mesh that has a
	 *   fixed potential. CAUTION: function is destructive; does not care if
	 *   the point has previous been set to be fixed.
	 *
	 * Parameters:
	 *   int width - width (X dimension) of the region in millimeters
	 *   int height - height (Y dimension) of the region in millimeters
	 *   int startX - X coordinate of the bottom left corner
	 *   int startY - Y coordinate of the bottom left corner
	 *   double voltage - fixed voltage to set region to
	 *
	 * Returns: nothing
	 */
	public void setFixedRectangle( int width, int height, int startX, int startY, double voltage )
	{
		width = ++width * granularity;
		height = ++height * granularity;
		startX *= granularity;
		startY *= granularity;

		for ( int row = startY; row < ( startY + height ); row++ )
		{
			for ( int col = startX; col < ( startX + width ); col++ )
			{
				grid[row][col].setFixed( false );
				grid[row][col].setVoltage( voltage );
				grid[row][col].setFixed( true );
			}
		}
	}

	//sets all modifiable points to an initial guess of specified voltage
	//WARNING: FUNCTION IS DESTRUCTIVE AND WILL OVERWRITE PREVIOUS RESULTS!
	public void setGuess( double guess )
	{
		for ( int row = 0; row < grid.length; row++ )
		{
			for ( int col = 0; col < grid[row].length; col++ )
			{
				grid[row][col].setVoltage( guess );
			}
		}
	}

	/* calculateSolution
	 *
	 * Purpose: finds the numerical solution to the electric potential at every point
	 *
	 * Parameters: 
	 *   double acceleration - acceleration factor to calculate with. 
	 *                         WARNING: unpredictable results for accleration outside range [1,2]
	 *   double epsilon - maximum allowable difference in potential between the nth and (n+1)th iterations
	 *
	 * Returns: nothing
	 */
	public void calculateSolution( double acceleration, double epsilon )
	{
		double currentEpsilon = 1; //max epsilon this iteration. set to 1 to allow entry into loop
		int currentIteration = 1; //tracks the current iteration
		int eMaxCoordinateX = 0; //raw mesh column that Emax occured at
		int eMaxCoordinateY = 0; //raw mesh row that Emax occured at

		//log the calculation parameters
		this.log += "Starting calculation...\n" +
				"Acceleration Factor: " + acceleration + "\n" +
				"Target Epsilon: " + epsilon + "\n\n";

		//while final epsilon has not been achieved
		while ( currentEpsilon > epsilon )
		{
			//reset max epsilon to zero at beginning of every iteration
			//else program will loop infinitely because Emax gets progressively smaller
			currentEpsilon = 0;

			for ( int row = 1; row < grid.length - 1; row++ )
			{
				for ( int col = 1; col < grid[row].length - 1; col++ )
				{
					double vold = grid[row][col].getVoltage();

					//changes to fixed points will automatically (and silently) be rejected
					grid[row][col].setVoltage(
							vold +
							0.25 * acceleration * ( grid[row-1][col].getVoltage() +
									 grid[row][col-1].getVoltage() +
									 grid[row+1][col].getVoltage() +
									 grid[row][col+1].getVoltage() -
									 4 * vold
							)
					);

					//calculate change in potential from the nth iteration to the (n+1)th iteration
					double pointEpsilon = Math.abs( grid[row][col].getVoltage() - vold );

					//if it is the new Emax for this iteration, record results
					if ( pointEpsilon > currentEpsilon )
					{
						currentEpsilon = pointEpsilon;
						eMaxCoordinateX = col;
						eMaxCoordinateY = row;
					}
				}
			}


			this.log += "Iteration " + currentIteration + " Results:\n" +
					"\tMax Epsilon: " + currentEpsilon +
					"\n\tAchieved @\n" +
					"\t\tr" + eMaxCoordinateY + ", c" + eMaxCoordinateX + " - Grid Absolute\n" +
					"\t\t(" + ( (double)(eMaxCoordinateX) / granularity ) + "mm, " + ( (double)(eMaxCoordinateY) / granularity ) + "mm ) - Cartesian\n\n";

			currentIteration++;
		}

		//log finishing results
		this.log += "Finished calculation in " + --currentIteration + " iterations.\n" +
				"Final Epsilon: " + currentEpsilon + "\n\n";
	}

	@Override
	public String toString()
	{
		String output = "";

		for ( int row = grid.length - 1; row > -1; row-- )
		{
			output += grid[row][0].getVoltage();

			for ( int col = 1; col < grid[row].length; col++ )
			{
				output += ",  " + grid[row][col].getVoltage();
			}

			output += "\n";
		}

		return output;
	}
	
	/* toFile
	 * 
	 * Purpose: exports the current state of the grid and log to files on disk
	 *
	 * Parameters:
	 *   String name - raw name of the files to export. function automatically 
	 *                 appends an appropriate extension for the grid and log file.
	 *
	 * Returns: nothing
	 */
	public void toFile( String name )
	{
		FileWriter outputStream;
		FileWriter logStream;
		
		//IO errors may occur when writing to files
		//alert user if they do
		try
		{
			outputStream = new FileWriter( new File(name + ".csv") );
			logStream = new FileWriter( new File(name + ".log") );
			
			//build grid into a .csv file
			for ( int row = grid.length - 1; row >= 0; row-- )
			{
				outputStream.write( "" + grid[row][0].getVoltage() );
				
				for ( int col = 1; col < grid[row].length; col++ )
				{
					outputStream.write( "," + grid[row][col].getVoltage() );
				}
				
				outputStream.write( "\n" );
			}

			//write log to file
			logStream.write( log );

			//save files to disk
			outputStream.close();
			logStream.close();
		}
		catch ( Exception FileError )
		{
			System.out.println( FileError );
		}

		log = "Log reset. Grid:" +
				"\n\t" + (grid[0].length / granularity) + "mm X " + (grid.length / granularity) + "mm, " +
				"\n\t" + granularity + " divisions per mm\n\n";
	}
}
