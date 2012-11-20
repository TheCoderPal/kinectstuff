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

import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.simulation.gpuparticles.CCGPUParticle;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetSetup;

/**
 * @author christianriekoff
 *
 */
public class ILParticleGroup {

	private int _myX;
	private int _myY;
	
	private int _myWidth;
	private int _myHeight;
	
	private int _mySubIndex;
	private int _myMaxParticles;
	
	private ILBubbleManager _myParticles;
	
	private List<CCGPUParticle> _myParticleList = new ArrayList<CCGPUParticle>();
	
	public ILParticleGroup(ILBubbleManager theParticles, int theX, int theY, int theWidth, int theHeight) {
		_myParticles = theParticles;
		_myX = theX;
		_myY = theY;
		_myWidth = theWidth;
		_myHeight = theHeight;
		
		_mySubIndex = 0;
		_myMaxParticles = _myWidth * _myHeight;
	}
	
	public int x() {
		return _myX;
	}
	
	public int y() {
		return _myY;
	}
	
	public int width() {
		return _myWidth;
	}
	
	public int height() {
		return _myHeight;
	}
	
	public void updateTargets(CCGPUTargetSetup theTargetSetup) {
		_myParticles.updateTargets(theTargetSetup, _myX, _myY, _myWidth, _myHeight);
	}
	
	public void allocateParticle(CCVector3f thePosition, CCVector3f theVelocity, float theLifeTime) {
		if(_mySubIndex >= _myMaxParticles)return;
		_mySubIndex++;
		int myX = _mySubIndex % _myWidth + _myX;
		int myY = _mySubIndex / _myWidth + _myY;
		
		int myIndex = myY * _myParticles.particles().width() + myX;
		_myParticleList.add(_myParticles.particles().allocateParticle(myIndex, thePosition, theVelocity, theLifeTime, true));
	}
	
	public void kill() {
		for(CCGPUParticle myParticle:_myParticleList) {
			_myParticles.particles().kill(myParticle);
		}
	}
	
	public void update(final float theDeltaTime) {
		
	}
	
	public int particlesInUse() {
		return _mySubIndex;
	}
	
	public int size() {
		return _myMaxParticles;
	}
}
