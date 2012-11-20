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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.events.CCMouseEvent;
import cc.creativecomputing.events.CCMouseListener;
import cc.creativecomputing.events.CCMouseMotionListener;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.math.CCVector3f;

/**
 * @author christianriekoff
 * 
 */
public class ILMouseSource implements CCMouseListener, CCMouseMotionListener, ILBubbleSource{

	private int _myWidth;
	private int _myHeight;

	private CCVector3f _myMousePosition;
	private CCVector3f _myMouseVelocity;
	@CCControl(name = "scale", min = 0, max = 1)
	private static float _cScale = 0;
	@CCControl(name = "z", min = -1000, max = 1000)
	private static float _cZ = 0;
	@CCControl(name = "y", min = -1000, max = 1000)
	private static float _cY = 0;

	/**
	 * @param theUser
	 * @param theID
	 * @param theSkeleton
	 * @param theTargets
	 */
	public ILMouseSource(CCApp theApp) {
		_myWidth = theApp.width;
		_myHeight = theApp.height;
		_myMousePosition = new CCVector3f();
		_myMouseVelocity = new CCVector3f();

		theApp.addMouseListener(this);
		theApp.addMouseMotionListener(this);
	}

	@Override
	public List<ILBubbleSourceEmitter> emitters() {
		List<ILBubbleSourceEmitter> myResult = new ArrayList<ILBubbleSourceEmitter>();
		myResult.add(new ILBubbleSourceEmitter(_myMousePosition, _myMouseVelocity));
		return myResult;
	}

	public void drawHandSpace(CCGraphics g) {
		g.color(0);
		g.ellipse(_myMousePosition, 30);
	}

	private CCVector2f _myPreviousPosition;

	@Override
	public void mousePressed(CCMouseEvent theEvent) {
		_myPreviousPosition = new CCVector2f(theEvent.x() - _myWidth / 2, _myHeight / 2 - theEvent.y());
	}

	@Override
	public void mouseReleased(CCMouseEvent theEvent) {
		_myMouseVelocity.set(0, 0, 0);
	}

	@Override
	public void mouseClicked(CCMouseEvent theEvent) {}

	@Override
	public void mouseEntered(CCMouseEvent theEvent) {}

	@Override
	public void mouseExited(CCMouseEvent theEvent) {}

	@Override
	public void mouseDragged(CCMouseEvent theMouseEvent) {
		CCVector3f myPos = new CCVector3f(theMouseEvent.x() - _myWidth / 2, _myHeight / 2 - theMouseEvent.y(), 0);
		_myMousePosition.set(myPos.scale(_cScale).add(0, _cY, _cZ));
		_myMouseVelocity.set(_myMousePosition.x - _myPreviousPosition.x, _myMousePosition.y - _myPreviousPosition.y, 0);
		_myPreviousPosition.set(_myMousePosition.x, _myMousePosition.y);
	}

	@Override
	public void mouseMoved(CCMouseEvent theMouseEvent) {
		CCVector3f myPos = new CCVector3f(theMouseEvent.x() - _myWidth / 2, _myHeight / 2 - theMouseEvent.y(), 0);
		_myMousePosition.set(myPos.scale(_cScale).add(0, _cY, _cZ));
	}

}
