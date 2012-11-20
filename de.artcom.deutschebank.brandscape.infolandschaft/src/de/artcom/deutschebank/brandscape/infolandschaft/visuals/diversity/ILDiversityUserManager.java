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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.cv.openni.CCOpenNIUser;
import cc.creativecomputing.cv.openni.CCOpenNIUser.CCUserJointType;
import cc.creativecomputing.cv.openni.util.CCOpenNIInteractionArea.CCOpenNIInteractionAreaListener;
import cc.creativecomputing.cv.openni.util.CCOpenNISkeletonController;
import cc.creativecomputing.events.CCKeyEvent;
import cc.creativecomputing.events.CCKeyListener;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.model.collada.CCColladaSkeletonMesh;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILDiversityUser.ILUserListener;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo.ILStepChecker;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo.ILThrowChecker;

/**
 * Manager for controlling and updating all users
 * @author christianriekoff
 *
 */
public class ILDiversityUserManager implements CCOpenNIInteractionAreaListener, ILUserListener, Iterable<ILDiversityUser>{

	
	
	/**
	 * Maximum possible user so far 5 as this seems to be the maximum number of user by openni
	 */
	public static int MAX_USER = 5;

	/**
	 * Defined array of user important for particle interaction
	 */
	private ILDiversityUser[] _myUserSlots = new ILDiversityUser[MAX_USER];
	
	/**
	 * Collada data for mesh and skeleton
	 */
	protected CCColladaSkeletonMesh _mySkeletonMesh;
	
	private ILSkeletonParticles _myParticles;
	private ILSkeletonParticleTargets _myTargets;
	
	private ILBubbleManager _myBubbleManager;
	
	private int _myXRes = 50;
	private int _myYRes = 40;
	
	private CCGraphics _myGraphics;
	/**
	 * map to have access to the ILUser object based on an openni user id
	 */
	protected Map<Integer, ILDiversityUser> _myUserControllerMap = new HashMap<Integer, ILDiversityUser>();
	
	public ILDiversityUserManager(CCApp theApp, ILBubbleManager theBubbleManager) {
		_mySkeletonMesh = new CCColladaSkeletonMesh(
			"120411_humanoid_01_bakeTR.dae",
			"humanoid-lib",
			"bvh_import/Hips"
		);
		
		_myParticles = new ILSkeletonParticles(theApp, _myXRes * (ILDiversityUserManager.MAX_USER * 2), _myYRes);
		
		_myBubbleManager = theBubbleManager;
		
		_myTargets = new ILSkeletonParticleTargets(
			theApp,
			this,
			_myXRes, _myYRes
		);
		_myParticles.targets(_myTargets.targets());
		theApp.addControls("diversity", "throw", 0, ILThrowChecker.class);
		theApp.addControls("diversity", "step", 0, ILStepChecker.class);
		
		theApp.addKeyListener(new CCKeyListener() {
			
			@Override
			public void keyTyped(CCKeyEvent theKeyEvent) {}
			
			@Override
			public void keyReleased(CCKeyEvent theKeyEvent) {
				for(ILDiversityUser myUser:_myUserSlots) {
					if(myUser == null) continue;
						
					myUser.doStep(myUser.openNIUser().joint(CCUserJointType.LEFT_FOOT));
					return;
				}
			}
			
			@Override
			public void keyPressed(CCKeyEvent theKeyEvent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		_myGraphics = theApp.g;
	}
	
	public ILSkeletonParticleTargets targets() {
		return _myTargets;
	}
	public ILSkeletonParticles particles() {
		return _myParticles;
	}
	
	/**
	 * Returns the user at the given slot might be null if there is no active user
	 * @param theSlot slot for the user
	 * @return user at the given slot
	 */
	public ILDiversityUser user(int theSlot) {
		return _myUserSlots[theSlot];
	}
	
	@Override
	public void onClap(ILDiversityUser theUser, CCVector3f theClapPosition) {
		theUser.looseTargets();
		_myParticles.triggerImpulse(theClapPosition);
//		CCLog.info("CLAP");
	}
	
	@Override
	public void onEnter(CCOpenNIUser theUser) {
		for(int i = 0; i < _myUserSlots.length; i++) {
			if(_myUserSlots[i] == null) {
				ILDiversityUser myUser = new ILDiversityUser(theUser, i,  _myBubbleManager, _myParticles, _mySkeletonMesh.skeleton().clone());
				myUser.events().add(this);
				_myUserControllerMap.put(theUser.id(), myUser);
				_myUserSlots[i] = myUser;
				return;
			}
		}
	}
	
	public void onLeave(CCOpenNIUser theUser) {
		try {
			ILDiversityUser myUser = _myUserControllerMap.remove(theUser.id());
			for(int i = 0; i < _myUserSlots.length; i++) {
				if(_myUserSlots[i] == myUser) {
					_myUserSlots[i] = null;
					return;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates all users and there skeletons
	 * @param theDeltaTime
	 */
	public void update(final float theDeltaTime) {
		for(ILDiversityUser myUser:_myUserControllerMap.values()) {
			myUser.update(theDeltaTime);
		}
		
		_myGraphics.pushMatrix();
		_myGraphics.scale(1f / CCOpenNISkeletonController._cSkeletonScale * 2);
		_myGraphics.camera().updateProjectionInfos();
		for(ILDiversityUser myUser:_myUserControllerMap.values()) {
			myUser.leftHandPosition2D().set(_myGraphics.camera().modelToScreen(myUser.leftHandPosition()));
			myUser.rightHandPosition2D().set(_myGraphics.camera().modelToScreen(myUser.rightHandPosition()));
		}	
		_myGraphics.popMatrix();

		_myTargets.update(theDeltaTime);
		_myParticles.update(theDeltaTime);
	}

	public Iterator<ILDiversityUser> iterator() {
		return _myUserControllerMap.values().iterator();
	}
	
	/**
	 * Returns the collada mesh for user interaction
	 * @return
	 */
	public CCColladaSkeletonMesh skeletonMesh() {
		return _mySkeletonMesh;
	}
}
