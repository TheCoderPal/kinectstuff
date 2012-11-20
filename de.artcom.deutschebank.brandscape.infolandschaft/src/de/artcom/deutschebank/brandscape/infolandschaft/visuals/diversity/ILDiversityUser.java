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

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.cv.openni.CCOpenNIUser;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserJoint;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserJointType;
import cc.creativecomputing.cv.openni.util.CCOpenNISkeletonController;
import cc.creativecomputing.events.CCListenerManager;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.model.skeleton.CCSkeleton;
import cc.creativecomputing.model.skeleton.CCSkeletonJoint;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILUser;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo.ILStepChecker;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo.ILStepChecker.ILStepListener;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo.ILThrowChecker;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo.ILThrowChecker.ILThrowListener;

/**
 * Class containing all user related info and functionality like the skeleton and user slot
 * @author christianriekoff
 *
 */
public class ILDiversityUser extends ILUser {
	
	public interface ILUserListener{
		public void onClap(ILDiversityUser theUser, CCVector3f thePosition);
	}
	
	@CCControl(name = "clap handdistance", min = 0, max = 100)
	private static float _cClapDistance = 0;
	
	@CCControl(name = "clap time", min = 0, max = 10)
	private static float _cClapTime = 0;
	
	@CCControl(name = "target random", min = 0, max = 1000)
	private static float _cTargetRandom = 0;
	
	@CCControl(name = "draw debug")
	private static boolean _cDrawDebug = false;
	
	private boolean _mySetTargets = true;
	
	private CCListenerManager<ILUserListener> _myEvents = CCListenerManager.create(ILUserListener.class);
	
	private ILThrowChecker _myThrowCheckerLeft;
	private ILThrowChecker _myThrowCheckerRight;
	
	private ILStepChecker _myStepCheckerLeft;
	private ILStepChecker _myStepCheckerRight;
	
	private ILBubbleManager _myBubbleManager;
	private ILSkeletonParticles _myParticles;
	
	private CCVector3f _myLeftHandPosition2D;
	private CCVector3f _myRightHandPosition2D;

	protected int _myID;
	protected CCSkeleton _mySkeleton;
	protected CCOpenNIUser _myOpenNIUser;
	protected CCOpenNISkeletonController _mySkeletonController;
	
	/**
	 * Creates a new user based on the openni user the user slot and the collada based skeleton
	 * @param theUser openni user
	 * @param theID slot id 
	 * @param theSkeleton collada based skeleton
	 */
	public ILDiversityUser(CCOpenNIUser theUser, int theID, ILBubbleManager theBubbleManager, ILSkeletonParticles theParticles, CCSkeleton theSkeleton) {

		_myOpenNIUser = theUser;
		_mySkeletonController = new CCOpenNISkeletonController(theUser, theSkeleton);
		_mySkeleton = _mySkeletonController.skeleton();
		_myID = theID;
		
		ILThrowListener myThrowListener = new ILThrowListener() {
			
			@Override
			public void onThrow(CCUserJoint theJoint) {
				throwBubble(theJoint);
			}
		};

		_myThrowCheckerLeft = new ILThrowChecker(theUser, CCUserJointType.LEFT_HAND);
		_myThrowCheckerLeft.events().add(myThrowListener);
		
		_myThrowCheckerRight = new ILThrowChecker(theUser, CCUserJointType.RIGHT_HAND);
		_myThrowCheckerRight.events().add(myThrowListener);
		
		ILStepListener myStepListener = new ILStepListener() {
			
			@Override
			public void onStep(CCUserJoint theJoint) {
				doStep(theJoint);
			}
		};
		
		_myStepCheckerLeft = new ILStepChecker(theUser, CCUserJointType.LEFT_FOOT);
		_myStepCheckerLeft.events().add(myStepListener);
		
		_myStepCheckerRight = new ILStepChecker(theUser, CCUserJointType.RIGHT_FOOT);
		_myStepCheckerRight.events().add(myStepListener);
		
		_myBubbleManager = theBubbleManager;
		_myParticles = theParticles;
		
		_myLeftHandPosition2D = new CCVector3f();
		_myRightHandPosition2D = new CCVector3f();
	}
	
	protected ILDiversityUser() {
		
	}
	
	public CCVector3f leftHandPosition() {
		return _mySkeleton.joint("LeftHand").position();
	}
	
	public CCVector3f rightHandPosition() {
		return _mySkeleton.joint("RightHand").position();
	}
	
	public CCVector3f leftHandPosition2D() {
		return _myLeftHandPosition2D;
	}
	
	public CCVector3f rightHandPosition2D() {
		return _myRightHandPosition2D;
	}
	
	public CCListenerManager<ILUserListener> events() {
		return _myEvents;
	}
	
	public void doStep(CCUserJoint theJoint) {
		CCSkeletonJoint myJoint = null;
		switch(theJoint.type()) {
		case LEFT_FOOT:
			myJoint = _mySkeleton.joint("LeftFoot");
			break;
		case RIGHT_FOOT:
			myJoint = _mySkeleton.joint("RightFoot");
			break;
		}

		_myParticles.triggerImpulse(myJoint.position().clone());
	}
	
	public void throwBubble(CCUserJoint theJoint) {
		CCSkeletonJoint myJoint = null;
		CCVector3f myPosition2D = null;
		switch(theJoint.type()) {
		case LEFT_HAND:
			myJoint = _mySkeleton.joint("LeftHand");
			myPosition2D = _myLeftHandPosition2D;
			break;
		case RIGHT_HAND:
			myJoint = _mySkeleton.joint("RightHand");
			myPosition2D = _myRightHandPosition2D;
			break;
		}

		_myBubbleManager.emitBubble(myJoint.position().clone(), myJoint.velocity().clone(), myPosition2D);
	}
	
	public CCOpenNIUser openNIUser() {
		return _myOpenNIUser;
	}
	
	private boolean _myIsInClap = false;
	private float _myClapTime = 0;
	
	/**
	 * Updates the user by updating the skeleton controller controlling its skeleton
	 * @param theDeltaTime
	 */
	public void update(final float theDeltaTime) {

		_mySkeletonController.update(theDeltaTime);	
		
		CCVector3f myLeftHandPosition  = _myOpenNIUser.joint(CCUserJointType.LEFT_HAND).position();
		CCVector3f myRightHandPosition  = _myOpenNIUser.joint(CCUserJointType.RIGHT_HAND).position();
		if(myLeftHandPosition.distance(myRightHandPosition)< _cClapDistance) {
			if(!_myIsInClap) {
				CCVector3f myClapPosition = CCVecMath.add( myLeftHandPosition, myRightHandPosition).scale(0.5f);
				_myEvents.proxy().onClap(this,myClapPosition);
			}
			_myIsInClap = true;
		}else {
			_myIsInClap = false;
		}
		
		_myClapTime += theDeltaTime;
		if(!_mySetTargets && _myClapTime > _cClapTime) {
			_mySetTargets = true;
		}
		
		_myThrowCheckerLeft.update(theDeltaTime);
		_myThrowCheckerRight.update(theDeltaTime);
		
		_myStepCheckerLeft.update(theDeltaTime);
		_myStepCheckerRight.update(theDeltaTime);
	}
	
	public boolean setTargets() {
		return _mySetTargets;
	}
	
	public void setTargets(boolean theSetTargetes) {
		_mySetTargets = theSetTargetes;
	}
	
	public void looseTargets() {
		setTargets(false);
		_myClapTime = 0;
	}
	
	public float targetRandom() {
		if(!_mySetTargets)return _myClapTime * _cTargetRandom;
		return 0;
	}
	
	public void drawSkeleton(CCGraphics g) {
		if(!_cDrawDebug)return;
		
		g.color(255, 100);
		g.pointSize(1);
		CCSkeleton mySkeleton = skeleton();
		if (mySkeleton != null)
			mySkeleton.draw(g);
	}
	
	public void drawDebug(CCGraphics g) {
		if(!_cDrawDebug)return;
		
		_myThrowCheckerLeft.drawValues(g, g.width);
		_myThrowCheckerRight.drawValues(g, g.width);
		
		_myStepCheckerLeft.drawValues(g, g.width);
		_myStepCheckerRight.drawValues(g, g.width);
		
		g.pushMatrix();
		g.translate(-g.width/2, -g.height/2);
		g.color(255,0,0);
		g.ellipse(_myLeftHandPosition2D.x, _myLeftHandPosition2D.y, 50);
		g.ellipse(_myRightHandPosition2D.x, _myRightHandPosition2D.y, 50);
		g.popMatrix();
	}
	
	/**
	 * Return user slot between 0 and {@link ILUserManager#MAX_USER}
	 * @return
	 */
	public int id() {
		return _myID;
	}
	
	/**
	 * Returns the skeleton for this user
	 * @return
	 */
	public CCSkeleton skeleton() {
		return _mySkeleton;
	}
	
	/**
	 * Returns the skeleton for this user
	 * @return
	 */
	public CCOpenNISkeletonController skeletonController() {
		return _mySkeletonController;
	}
}
