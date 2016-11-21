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
public class EE240Program {

	public static void main( String [] args )
	{
		//32x32 mm grid, 1 division per mm
		PotentialGrid grid = new PotentialGrid( 32, 32, 1 );

		//set all borders to 0V
		grid.setFixedLine( grid.getNumberOfRows(), grid.ORIENTATION_VERTICAL, 0, 0, 0 );
		grid.setFixedLine( grid.getNumberOfRows(), grid.ORIENTATION_VERTICAL, 32, 0, 0 );
		grid.setFixedLine( grid.getNumberOfColumns(), grid.ORIENTATION_HORIZONTAL, 0, 0, 0 );
		grid.setFixedLine( grid.getNumberOfColumns(), grid.ORIENTATION_HORIZONTAL, 0, 32, 0 );
		
		grid.setFixedRectangle( 2, 10, 4, 11, 0 ); //2x10 mm, anchored at (4,11) mm, 0V potential
		grid.setFixedRectangle( 16, 4, 12, 14, 100); //16x4 mm, anchored at (12,14)mm, 100V potential
		grid.setFixedRectangle( 2, 10, 28, 11, 0); //2x10 mm, anchroed at (28,11) mm, 0V potential
		
		grid.setGuess( 50 ); //initial guess of 50V
		grid.calculateSolution( 1, 0.1 ); //acceleration factor of 1, epsilon 0,.1

		System.out.println( grid.toString() );
		
		grid.toFile( "Subdivs1,Acc1,E0.1.csv" );
	}
}
