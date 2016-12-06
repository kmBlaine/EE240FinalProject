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
/**
 * Created by agentKmurph on 17-Nov-16.
 */

import java.io.*;

public class PotentialGrid
{
	public static final boolean ORIENTATION_VERTICAL = true;
	public static final boolean ORIENTATION_HORIZONTAL = false;
	
	private static class PotentialPoint
	{
		private double voltage;
		private boolean fixed;

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
	private int granularity;
	private String log;

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
	
	public int getNumberOfRows()
	{
		return grid.length;
	}
	
	public int getNumberOfColumns()
	{
		return grid[0].length;
	}

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

	public void calculateSolution( double acceleration, double epsilon )
	{
		double currentEpsilon = 1;
		int currentIteration = 1;
		int eMaxCoordinateX = 0;
		int eMaxCoordinateY = 0;

		this.log += "Starting calculation...\n" +
				"Acceleration Factor: " + acceleration + "\n" +
				"Target Epsilon: " + epsilon + "\n\n";

		while ( currentEpsilon > epsilon )
		{
			currentEpsilon = 0;

			for ( int row = 1; row < grid.length - 1; row++ )
			{
				for ( int col = 1; col < grid[row].length - 1; col++ )
				{
					double vold = grid[row][col].getVoltage();

					grid[row][col].setVoltage(
							vold +
							0.25 * acceleration * ( grid[row-1][col].getVoltage() +
									 grid[row][col-1].getVoltage() +
									 grid[row+1][col].getVoltage() +
									 grid[row][col+1].getVoltage() -
									 4 * vold
							)
					);

					double pointEpsilon = Math.abs( grid[row][col].getVoltage() - vold );

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
	
	public void toFile( String name )
	{
		FileWriter outputStream;
		FileWriter logStream;
		
		try
		{
			outputStream = new FileWriter( new File(name + ".csv") );
			logStream = new FileWriter( new File(name + ".log") );
			
			for ( int row = grid.length - 1; row >= 0; row-- )
			{
				outputStream.write( "" + grid[row][0].getVoltage() );
				
				for ( int col = 1; col < grid[row].length; col++ )
				{
					outputStream.write( "," + grid[row][col].getVoltage() );
				}
				
				outputStream.write( "\n" );
			}

			logStream.write( log );

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
