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

import cc.creativecomputing.cv.openni.CCOpenNIUser;
import cc.creativecomputing.cv.openni.util.CCOpenNISkeletonController;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.model.skeleton.CCSkeleton;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILInteractionManager.ILInteractionSource;

/**
 * @author christianriekoff
 *
 */
public class ILConnectivityUser extends CCOpenNISkeletonController{

	private CCVector2f _myLeftHandPosition;
	private CCVector2f _myRightHandPosition;
	
	private ILInteractionSource _myLeftHandSource;
	private ILInteractionSource _myRightHandSource;
	
	public ILConnectivityUser(CCOpenNIUser theUser, CCSkeleton theSkeleton) {
		super(theUser, theSkeleton);
		_myLeftHandPosition = new CCVector2f();
		_myRightHandPosition = new CCVector2f();
		
		_myLeftHandSource = new ILInteractionSource(_myLeftHandPosition);
		_myRightHandSource = new ILInteractionSource(_myRightHandPosition);
	}
	
	public CCVector2f leftHandPosition() {
		return _myLeftHandPosition;
	}
	
	public CCVector2f rightHandPosition() {
		return _myRightHandPosition;
	}
	
	public ILInteractionSource leftHandSource(){
		return _myLeftHandSource;
	}
	
	public ILInteractionSource rightHandSource(){
		return _myRightHandSource;
	}
}
