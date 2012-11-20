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

public class ILThrowSpeedCheckker {

	public static interface ILThrowListener {
		public void onThrow(CCUserJoint theJoint);
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

	@CCControl(name = "value scale", min = 0, max = 1)
	static float _cScale = 1;

	@CCControl(name = "motion scale", min = 0, max = 10)
	static float _cMotionScale = 1;

	@CCControl(name = "acc scale", min = 0, max = 10)
	static float _cAccScale = 1;

	@CCControl(name = "angle scale", min = 0, max = 10)
	static float _cAngleScale = 1;

	private CCVector3f _myLastAcceleration;
	private float _myLastDot;
	private float _myLastDiff;

	private CCOpenNIUser _myUser;
	private CCUserJointType _myJointType;

	private float _myRadius = 0;
	private float _myTimeSinceLastThrow = 0;

	private List<Float> _myValues = new ArrayList<Float>();
	
	private List<Float> _myMotions = new ArrayList<Float>();
	private List<Float> _myAccelerations = new ArrayList<Float>();
	private List<Float> _myAngles = new ArrayList<Float>();
	
	private CCVector3f _myLastPosition = null;
	private CCVector3f _myLastVelocity = null;
	
	private float _myLastAngle = 0;

	private CCListenerManager<ILThrowListener> _myEvents = CCListenerManager.create(ILThrowListener.class);
	
	public ILThrowSpeedCheckker(CCOpenNIUser theUser, CCUserJointType theJointType) {
		_myUser = theUser;
		_myJointType = theJointType;
	}

	public CCListenerManager<ILThrowListener> events() {
		return _myEvents;
	}
	
	private boolean _myIsRising = false;
	private boolean _myHasThrow = false;
	private float _myLastExtrema = 0;
	private float _myRiseTime = 0;

	public void update(float theDeltaTime) {

		CCUserJoint myJoint = _myUser.joint(_myJointType);
		
		if(_myLastPosition == null) {
			_myLastPosition = myJoint.position().clone();
			return;
		}
		
		if(_myLastPosition.equals(myJoint.position())) {
			return;
		}
		
		CCVector3f myVelocity = CCVecMath.subtract(myJoint.position(), _myLastPosition);
		_myLastPosition = myJoint.position().clone();
		
		if (_myMotions.size() > 1000)
			_myMotions.remove(0);
		_myMotions.add(myVelocity.length());
		
		if(_myLastVelocity == null) {
			_myLastVelocity = myVelocity;
			return;
		}

		CCVector3f myAcceleration = CCVecMath.subtract(myVelocity, _myLastVelocity);
		_myLastVelocity = myVelocity.clone();
		
		if (_myAccelerations.size() > 1000)
			_myAccelerations.remove(0);
		_myAccelerations.add(myAcceleration.length());
		
		CCVector3f mySpeed = myJoint.velocity().clone().normalize();
		if (_myLastAcceleration == null) {
			_myLastAcceleration = mySpeed;
			return;
		}
		
		CCVector3f myLeftShoulder = _myUser.joint(CCUserJointType.LEFT_SHOULDER).position().clone();
		CCVector3f myLeftElbow = _myUser.joint(CCUserJointType.LEFT_ELBOW).position().clone();
		CCVector3f myLeftHand = _myUser.joint(CCUserJointType.LEFT_HAND).position().clone();

		if (_myAngles.size() > 1000)
			_myAngles.remove(0);
		
		float myAngle = CCVecMath.angle(CCVecMath.subtract(myLeftElbow, myLeftHand).normalize(), CCVecMath.subtract(myLeftElbow, myLeftShoulder).normalize());
		
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
		
		if(!_myHasThrow && myChange > _cAngleChangeThresh && myAngle > _cAngleMinThresh && _myRiseTime < _cAngleMaxTime) {
			_myAngles.add(300f);
			_myHasThrow = true;
			_myEvents.proxy().onThrow(myJoint);
		}else {
			_myAngles.add(myAngle);
		}
		
		_myTimeSinceLastThrow += theDeltaTime;

		float myDot = _myLastVelocity.dot(myVelocity);
		float myDiff = _myLastDot - myDot;
		float myDiff2 = CCMath.abs(_myLastDiff - myDiff);

		_myRadius = CCMath.max(_myRadius - theDeltaTime, 0);

		if (myDiff2 > _cThreshold && _myTimeSinceLastThrow > _cThrowBreak) {
			_myRadius = 1;
			_myTimeSinceLastThrow = 0;
//			_myEvents.proxy().onThrow(myJoint);
		}

		_myLastDiff = myDiff;
		if (_myValues.size() > 1000)
			_myValues.remove(0);
		_myValues.add(myDiff2);
		
		
		
		_myLastAcceleration = mySpeed;
	}

	public void drawValues(CCGraphics g, int theWidth) {
		float myX = 0;
		g.color(255,0,0);
		g.beginShape(CCDrawMode.LINE_STRIP);
		for (float myValue : values()) {
			g.vertex(myX * 1 - theWidth / 2, myValue * _cScale);
			myX++;
		}
		g.endShape();

		myX = 0;
		g.color(0,255,0);
		g.beginShape(CCDrawMode.LINE_STRIP);
		for (float myValue : _myMotions) {
			g.vertex(myX * 1 - theWidth / 2, myValue * _cMotionScale);
			myX++;
		}
		g.endShape();
		
		myX = 0;
		g.color(0,0,255);
		g.beginShape(CCDrawMode.LINE_STRIP);
		for (float myValue : _myAccelerations) {
			g.vertex(myX * 1 - theWidth / 2, myValue * _cAccScale);
			myX++;
		}
		g.endShape();
		
		myX = 0;
		g.color(0,255,255);
		g.beginShape(CCDrawMode.LINE_STRIP);
		for (float myValue : _myAngles) {
			g.vertex(myX * 1 - theWidth / 2, myValue * _cAngleScale);
			myX++;
		}
		g.endShape();
		g.line(-theWidth / 2, _cAngleMinThresh * _cAngleScale, theWidth / 2, _cAngleMinThresh * _cAngleScale);
		
		

		g.line(-theWidth / 2, _cThreshold * _cScale, theWidth / 2, _cThreshold * _cScale);
		
		g.color(255);
		g.line(-theWidth / 2, 0, theWidth / 2, 0);

	}

	public void draw(CCGraphics g) {
		g.ellipse(_myUser.joint(_myJointType).position(), _myRadius * 100);
	}

	List<Float> values() {
		return _myValues;
	}
}