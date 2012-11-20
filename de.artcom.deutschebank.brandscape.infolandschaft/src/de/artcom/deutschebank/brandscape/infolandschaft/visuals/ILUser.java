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

import cc.creativecomputing.cv.openni.CCOpenNIUser;
import cc.creativecomputing.cv.openni.util.CCOpenNISkeletonController;
import cc.creativecomputing.model.skeleton.CCSkeleton;

/**
 * Class containing all user related info and functionality like the skeleton and user slot
 * @author christianriekoff
 *
 */
public class ILUser{

	
	/**
	 * Creates a new user based on the openni user the user slot and the collada based skeleton
	 * @param theUser openni user
	 * @param theID slot id 
	 * @param theSkeleton collada based skeleton
	 */
	public ILUser(CCOpenNIUser theUser, int theID, CCSkeleton theSkeleton) {
	}
	
	protected ILUser() {
		
	}
	
	
	
	
}
