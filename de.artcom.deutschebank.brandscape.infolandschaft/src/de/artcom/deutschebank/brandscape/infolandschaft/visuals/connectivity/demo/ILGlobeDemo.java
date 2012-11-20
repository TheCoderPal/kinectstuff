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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.demo;


import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.math.util.CCArcball;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILGlobe;

public class ILGlobeDemo extends CCApp {
	
	private CCArcball _myArcball;
	
	private ILGlobe _myGlobe;
	
	
	
	@Override
	public void setup() {
		
		_myArcball = new CCArcball(this);
		
		_myGlobe = new ILGlobe(g);
		
		addControls("app", "globe", _myGlobe);
	}

	@Override
	public void update(final float theDeltaTime) {
		_myGlobe.update(theDeltaTime);
	}

	@Override
	public void draw() {
		g.clear();
		_myArcball.draw(g);
//		g.scale(5);
		
		_myGlobe.draw(g);
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(ILGlobeDemo.class);
		myManager.settings().size(1500, 900);
		myManager.settings().antialiasing(8);
		myManager.start();
	}
}

