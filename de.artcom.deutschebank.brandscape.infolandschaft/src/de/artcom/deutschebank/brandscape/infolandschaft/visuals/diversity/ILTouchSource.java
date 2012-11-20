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

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.input.touch.CCTouch;
import cc.creativecomputing.math.CCVector3f;

/**
 * @author christianriekoff
 * 
 */
public class ILTouchSource implements ILBubbleSource {

	@CCControl(name = "scale", min = 0, max = 1)
	private static float _cScale = 0;
	@CCControl(name = "z", min = -1000, max = 1000)
	private static float _cZ = 0;
	@CCControl(name = "y", min = -1000, max = 1000)
	private static float _cY = 0;
	@CCControl(name = "x", min = -1000, max = 1000)
	private static float _cX = 0;

	private CCTouch _myTouch;

	/**
	 * @param theUser
	 * @param theID
	 * @param theSkeleton
	 * @param theTargets
	 */
	public ILTouchSource(CCTouch theTouch) {
		_myTouch = theTouch;
	}

	@Override
	public List<ILBubbleSourceEmitter> emitters() {
		List<ILBubbleSourceEmitter> myResult = new ArrayList<ILBubbleSourceEmitter>();
		myResult.add(new ILBubbleSourceEmitter(
			new CCVector3f(_myTouch.position()).scale(_cScale).add(_cX, _cY, _cZ), 
			new CCVector3f(_myTouch.velocity())
		));
		return myResult;
	}

	public void drawHandSpace(CCGraphics g) {
		g.color(0);
		g.ellipse(new CCVector3f(_myTouch.position()).scale(_cScale).add(_cX, _cY, _cZ), 30);
	}

}
