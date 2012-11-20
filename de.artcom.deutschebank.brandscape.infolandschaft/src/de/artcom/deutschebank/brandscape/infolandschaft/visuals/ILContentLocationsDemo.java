/*  
 * Copyright (c) 2012 Christian Riekoff <info@texone.org>  
 *  
 *  This file is free software: you may copy, redistribute and/or modify it  
 *  under the terms of the GNU General Public License as published by the  
 *  Free Software Foundation, either version 2 of the License, or (at your  
 *  option) any later version.  
 *  
 *  This file is distributed in the hope that it will be useful, but  
 *  WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 *  General Public License for more details.  
 *  
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 */
package de.artcom.deutschebank.brandscape.infolandschaft.visuals;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.events.CCMouseAdapter;
import cc.creativecomputing.events.CCMouseEvent;
import cc.creativecomputing.math.CCVector2f;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILContentLocations.ILContentLocation;

public class ILContentLocationsDemo extends CCApp {
	
	private ILContentLocations _myContentLocations;
	
	private ILContentLocation _myNearestLocation;

	@Override
	public void setup() {
		_myContentLocations = new ILContentLocations(this, "demo_locations.xml");
		
		addMouseListener(new CCMouseAdapter() {
			
			@Override
			public void mousePressed(CCMouseEvent theEvent) {
				switch(theEvent.button()) {
				case LEFT:
					ILContentLocation myLocation = _myContentLocations.nearestFreeLocation(new CCVector2f(theEvent.x(), height - theEvent.y()));
					if(myLocation == null)return;
					myLocation.isTaken(true);
					break;
				case RIGHT:
					_myContentLocations.reset();
					break;
				}
			}
		}); 
	}

	@Override
	public void update(final float theDeltaTime) {
		_myNearestLocation = _myContentLocations.nearestFreeLocation(new CCVector2f(mouseX, height - mouseY));
	}

	@Override
	public void draw() {
		g.clear();
		g.pushMatrix();
		g.translate( -width/2, -height/2);
		if(_myNearestLocation != null) {
			g.line(_myNearestLocation.location(), new CCVector2f(mouseX, height - mouseY));
		}
		_myContentLocations.draw(g);
		g.popMatrix();
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(ILContentLocationsDemo.class);
		myManager.settings().size(500, 500);
		myManager.start();
	}
}

