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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.cv.openni.CCOpenNIUser;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserJoint;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserJointType;
import cc.creativecomputing.events.CCListenerManager;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector3f;

public class ILStepChecker {

	public static interface ILStepListener {
		public void onStep(CCUserJoint theJoint);
	}

	@CCControl(name = "threshold", min = 0, max = 10000)
	static float _cThreshold = 0;

	@CCControl(name = "angle change thresh", min = 0, max = 50)
	static float _cAngleChangeThresh = 1;

	@CCControl(name = "angle min thresh", min = 0, max = 180)
	static float _cAngleMinThresh = 1;

	@CCControl(name = "angle max time", min = 0, max = 1)
	static float _cAngleMaxTime = 1;

	@CCControl(name = "throw break", min = 0, max = 2)
	private static float _cThrowBreak = 0;

	@CCControl(name = "angle scale", min = 0, max = 10)
	static float _cAngleScale = 1;

	private CCOpenNIUser _myUser;
	private CCUserJointType _myJointType;

	private float _myRadius = 0;
	private float _myTimeSinceLastThrow = 0;

	private List<Float> _myAngles = new ArrayList<Float>();
	

	
	private boolean _myIsRising = false;
	private boolean _myHasThrow = false;
	private float _myLastExtrema = 0;
	private float _myRiseTime = 0;
	private float _myLastAngle = 0;

	private CCListenerManager<ILStepListener> _myEvents = CCListenerManager.create(ILStepListener.class);
	
	public ILStepChecker(CCOpenNIUser theUser, CCUserJointType theJointType) {
		_myUser = theUser;
		_myJointType = theJointType;
	}

	public CCListenerManager<ILStepListener> events() {
		return _myEvents;
	}

	public void update(float theDeltaTime) {

		CCUserJoint myJoint = _myUser.joint(_myJointType);
		
		CCVector3f myShoulder;
		CCVector3f myElbow;
		CCVector3f myHand;
		
		if(_myJointType == CCUserJointType.LEFT_FOOT) {
			myShoulder = _myUser.joint(CCUserJointType.LEFT_HIP).position().clone();
			myElbow = _myUser.joint(CCUserJointType.LEFT_KNEE).position().clone();
			myHand = _myUser.joint(CCUserJointType.LEFT_FOOT).position().clone();
		}else {
			myShoulder = _myUser.joint(CCUserJointType.RIGHT_HIP).position().clone();
			myElbow = _myUser.joint(CCUserJointType.RIGHT_KNEE).position().clone();
			myHand = _myUser.joint(CCUserJointType.RIGHT_FOOT).position().clone();
		}

		if (_myAngles.size() > 1000)
			_myAngles.remove(0);
		
		float myAngle = CCMath.degrees(CCVecMath.angle(CCVecMath.subtract(myElbow, myHand).normalize(), CCVecMath.subtract(myElbow, myShoulder).normalize()));
		
		if(_myLastAngle == 0) {
			_myLastAngle = myAngle;
			_myLastExtrema = myAngle;
			return;
		}
		
		float myChange = 0;
		_myRiseTime += theDeltaTime;
		
		if(_myIsRising) {
			myChange = (_myLastAngle - _myLastExtrema);
			if(_myLastAngle > myAngle) {
				_myIsRising = false;
				_myLastExtrema = _myLastAngle;
			}else {
				
			}
		}else {
			if(_myLastAngle < myAngle) {
				_myIsRising = true;
				 _myHasThrow = false;
				_myRiseTime = 0;
				_myLastExtrema = _myLastAngle;
			}else {
				
			}
		}

		_myLastAngle = myAngle;
		
		if(!_myHasThrow && myChange > _cAngleChangeThresh && myAngle > _cAngleMinThresh && _myRiseTime < _cAngleMaxTime) { // && _myTimeSinceLastThrow > _cThrowBreak
			_myAngles.add(300f);
			_myHasThrow = true;
			_myEvents.proxy().onStep(myJoint);
		}else {
			_myAngles.add(myAngle);
		}
		
		_myTimeSinceLastThrow += theDeltaTime;
	}

	public void drawValues(CCGraphics g, int theWidth) {
		float myX = 0;
		g.color(255,0,0);
		
		myX = 0;
		g.color(0,255,255);
		g.beginShape(CCDrawMode.LINE_STRIP);
		for (float myValue : _myAngles) {
			g.vertex(myX * 1 - theWidth / 2, myValue * _cAngleScale);
			myX++;
		}
		g.endShape();
		g.line(-theWidth / 2, _cAngleMinThresh * _cAngleScale, theWidth / 2, _cAngleMinThresh * _cAngleScale);
		
		g.color(255);
		g.line(-theWidth / 2, 0, theWidth / 2, 0);

	}

	public void draw(CCGraphics g) {
		g.ellipse(_myUser.joint(_myJointType).position(), _myRadius * 100);
	}
}