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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity;

import java.util.HashMap;
import java.util.Map;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.cv.openni.CCOpenNI;
import cc.creativecomputing.cv.openni.CCOpenNIUser;
import cc.creativecomputing.cv.openni.CCOpenNIUserGenerator;
import cc.creativecomputing.cv.openni.util.CCAbstractColladaOpenNIUserManager;
import cc.creativecomputing.cv.openni.util.CCOpenNIInteractionArea;
import cc.creativecomputing.cv.openni.util.CCOpenNIInteractionArea.CCOpenNIInteractionAreaListener;
import cc.creativecomputing.cv.openni.util.CCOpenNISkeletonController;
import cc.creativecomputing.cv.openni.util.CCOpenNISkeletonController.CCSkeletonJointType;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.model.collada.CCColladaSkeletonMesh;
import cc.creativecomputing.model.skeleton.CCSkeleton;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.avatar.ILConnectivityAvatar;

/**
 * @author christianriekoff
 *
 */
public class ILConnectivityUserManager implements CCOpenNIInteractionAreaListener{
	
	@CCControl(name = "hand extension", min = 1, max = 2)
	private float _cHandExtension = 0;
	
	@CCControl(name = "alpha", min = 0, max = 1)
	protected float _cAlpha = 0;
	
	@CCControl(name = "draw debug")
	protected boolean _cDrawDebug = true;
	
	private ILConnectivityAvatar _myAvatar;
	
	private CCGraphics _myGraphics;
	

	protected Map<Integer, ILConnectivityUser> _myUserMap = new HashMap<Integer, ILConnectivityUser>();
	
	private ILInteractionManager _myInteractionManager;
	protected CCColladaSkeletonMesh _mySkeletonMesh;
	
	public ILConnectivityUserManager(CCApp theApp, CCTexture2D theBackground, ILInteractionManager theInteractionManager) {
		_mySkeletonMesh = new CCColladaSkeletonMesh(
			"120411_humanoid_01_bakeTR.dae",
			"humanoid-lib",
			"bvh_import/Hips"
		);
		
		_myAvatar = new ILConnectivityAvatar(theApp, theBackground, _mySkeletonMesh);

		theApp.addControls("connectivity", "avatar manager", 4, this);
			
		_myGraphics = theApp.g;
		
		_myInteractionManager = theInteractionManager;
	}
	
	public ILConnectivityUser createSkeletonController(CCOpenNIUser theUser, CCSkeleton theSkeleton) {
		ILConnectivityUser myUser = new ILConnectivityUser(theUser, theSkeleton);
		_myInteractionManager.addSource(myUser.leftHandSource());
		_myInteractionManager.addSource(myUser.rightHandSource());
		return myUser;
	}
	
	public void removeController(ILConnectivityUser theUser){
		_myInteractionManager.removeSource(theUser.leftHandSource());
		_myInteractionManager.removeSource(theUser.rightHandSource());
	}
	
	@Override
	public void onEnter(CCOpenNIUser theUser) {
		_myUserMap.put(theUser.id(), createSkeletonController(theUser, _mySkeletonMesh.skeleton().clone()));
	}
	
	/* (non-Javadoc)
	 * @see cc.creativecomputing.cv.openni.util.CCOpenNIInteractionArea.CCOpenNIInteractionAreaListener#onLeave(cc.creativecomputing.cv.openni.CCOpenNIUser)
	 */
	@Override
	public void onLeave(CCOpenNIUser theUser) {
		if(theUser == null)return; 
		if(_myUserMap.containsKey(theUser.id())) {
			ILConnectivityUser myUser = _myUserMap.remove(theUser.id());
			removeController(myUser);
		}
	}
	
	/**
	 * Updates all users and there skeletons
	 * @param theDeltaTime
	 */
	public void update(final float theDeltaTime) {
		for (CCOpenNISkeletonController myController : _myUserMap.values()) {
			myController.skeleton().bindSkinMatrix().reset();
			myController.update(theDeltaTime);
		}
		
		_myAvatar.update(theDeltaTime);
		for (ILConnectivityUser myController : _myUserMap.values()) {
			_myGraphics.pushMatrix();
			_myGraphics.scale(1f / myController.scale());
			_myGraphics.scale(1f, 1f, 1f);
			_myGraphics.camera().updateProjectionInfos();
			
			CCVector3f myLeftHand3D = CCVecMath.blend(
				_cHandExtension, 
				myController.joint(CCSkeletonJointType.LEFT_FORE_ARM).position(), 
				myController.joint(CCSkeletonJointType.LEFT_HAND).position()
			);
			
			CCVector3f myRightHand3D = CCVecMath.blend(
				_cHandExtension, 
				myController.joint(CCSkeletonJointType.RIGHT_FORE_ARM).position(), 
				myController.joint(CCSkeletonJointType.RIGHT_HAND).position()
			);
			
			CCVector3f myLeftHandPosition = _myGraphics.camera().modelToScreen(myLeftHand3D);
			myController.leftHandPosition().set(myLeftHandPosition.x, myLeftHandPosition.y);
			CCVector3f myRightHandPosition = _myGraphics.camera().modelToScreen(myRightHand3D);
			myController.rightHandPosition().set(myRightHandPosition.x, myRightHandPosition.y);
			_myGraphics.popMatrix();
		}
		
	}
	
	public void alpha(float theAlpha){
		_myAvatar.alpha(theAlpha);
	}
	
	public void drawController(CCGraphics theG, CCOpenNISkeletonController theController) {
		drawController(theG, theController, false);
	}
	
	public void drawController(CCGraphics g, CCOpenNISkeletonController theController, boolean theDrawBlurred) {
		g.noDepthTest();
		g.pushMatrix();
		g.scale(1f / theController.scale());
		g.scale(1f, 1f, 1f);
		g.color(1f);
//		theController.skeleton().draw(g);
//		theController.skeleton().drawOrientations(g, 10);
		g.pointSize(1);
//		g.color(1f, _cAlpha);

//		_myRefractionShader.start();
//		_mySkeletonMesh.draw(g, theController.skeleton());
//		_myRefractionShader.end();
		if(theDrawBlurred)_myAvatar.drawBlurred(g, theController.skeleton());
		else _myAvatar.drawStraight(g, theController.skeleton());
		g.popMatrix();
	}
	
	public void drawControllerDebug(CCGraphics g, CCOpenNISkeletonController theController) {
		g.pushMatrix();
		g.scale(1f / theController.scale());
		g.scale(1f, 1f, -1f);
		g.color(1f);
		theController.skeleton().draw(g);
		theController.skeleton().drawOrientations(g, 10);
		g.pointSize(1);
//		g.color(1f, _cAlpha);

//		_myRefractionShader.start();
//		_mySkeletonMesh.draw(g, theController.skeleton());
//		_myRefractionShader.end();
//		_myAvatar.draw(g, theController.skeleton());
		g.popMatrix();
	}
	
	public void draw(CCGraphics g, boolean theDrawBlurred) {
		if(_cDrawDebug) {
	
			g.color(125, 0, 0);
			g.strokeWeight(1);
			g.line(0, 0, 0, 200, 0, 0);
			g.color(0, 125, 0);
			g.line(0, 0, 0, 0, 0, -200);
			g.color(0, 0, 125);
			g.line(0, 0, 0, 0, -200, 0);
	
			g.blend();
	
	//		_mySkeletonMesh.skeleton().draw(g);
	//		_mySkeletonMesh.skeleton().drawOrientations(g, 10);
			g.pointSize(1);
			g.color(1f, _cAlpha);
	//		_mySkeletonMesh.draw(g, _mySkeletonMesh.skeleton());
	
			for (CCOpenNISkeletonController myController : _myUserMap.values()) {
				drawControllerDebug(g, myController);
			}
		}else {
			g.blend();
			g.color(1f, _cAlpha);
			for (CCOpenNISkeletonController myController : _myUserMap.values()) {
				drawController(g, myController, theDrawBlurred);
			}
		}
	}
	
	
	
	public void drawBlurred(CCGraphics g) {
		draw(g, true);
	}
	
	public void drawStraight(CCGraphics g) {
		draw(g, false);
	}
}
