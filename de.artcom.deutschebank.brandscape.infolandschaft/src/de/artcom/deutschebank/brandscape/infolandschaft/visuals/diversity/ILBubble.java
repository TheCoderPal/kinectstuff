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
import cc.creativecomputing.events.CCListenerManager;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.CCVector4f;
import cc.creativecomputing.simulation.gpuparticles.CCGPUParticleGroup;
import cc.creativecomputing.simulation.gpuparticles.forces.target.CCGPUTargetPointSetSetup;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILContentManager.ILContent;

/**
 * @author christianriekoff
 *
 */
public class ILBubble {
	
	public class ILBubbleState{
		protected float _myTimer = 0;
		
		public void onStart() {
			
		}
		
		public void onUpdate(final float theDeltaTime) {
			
		}
		
		public void update(final float theDeltaTime) {
			_myTimer += theDeltaTime;
			onUpdate(theDeltaTime);
			
			ILBubbleState myNextState = nextState();
			if(myNextState != null) {
				setState(myNextState);
			}
		}
		
		public void onEnd() {
			
		}
		
		public ILBubbleState nextState() {
			return null;
		}
	}
	
	public class ILBubbleOpeningState extends ILBubbleState{
		
		@Override
		public void onStart() {
		}
		
		@Override
		public void onUpdate(float theDeltaTime) {
//			emitParticles();
		}
		
		@Override
		public ILBubbleState nextState() {
			if (progress() > _cOpenProgress) {
//				if(_myIsTargetBubble)
					return _myOpenState;
//				else return _myClosingState;
			}
			if(_myTimer > _cCloseTime) return _myClosingState;
			return null;
		}
		
	}
	
	public class ILBubbleOpenState extends ILBubbleState{
		
		@Override
		public void onStart() {
			_myEvents.proxy().onOpen(ILBubble.this);
			
			if(_myContent == null)return;
			_myContent.position(center2D());
			_myContent.open();
		}
		
		@Override
		public void onUpdate(float theDeltaTime) {
//			if (progress() < 1.0)emitParticles();
		}
		
		@Override
		public ILBubbleState nextState() {
			if(_myContent != null) {
				if(_myTimer > _cCloseTime) return _myClosingState;
			}else {
				if(_myTimer > _cCloseFreeTime) return _myClosingState;
			}
			return null;
		}
	}
	

	
	public class ILBubbleClosingState extends ILBubbleState{
		
		@Override
		public void onStart() {
			close();
		}
		
		@Override
		public void onEnd() {
			_myParticleGroup.kill();
		}
		
		@Override
		public void onUpdate(float theDeltaTime) {
			_myParticleGroup.kill((int)(theDeltaTime / _cKillTime * _myParticleGroup.size() * 1.2f));
		}
		
		@Override
		public ILBubbleState nextState() {
			if(_myTimer > _cKillTime) return _myCloseState;
			return null;
		}
	}
	
	public class ILBubbleCloseState extends ILBubbleState{
		
		@Override
		public void onStart() {
			closed();
		}
	}
	
	
	public static interface ILBubbleListener{
		public void onOpen(ILBubble theBubble);
		
		public void onClose(ILBubble theBubble);
		
		public void onClosed(ILBubble theBubble);
	}
	
	
	@CCControl(name = "close time", min = 0, max = 30)
	private static float _cCloseTime; 
	
	@CCControl(name = "close free time", min = 0, max = 30)
	private static float _cCloseFreeTime; 
	
	@CCControl(name = "kill time", min = 0, max = 30)
	private static float _cKillTime; 
	
	@CCControl(name = "open progress", min = 0, max = 1)
	private static float _cOpenProgress = 0;
	
	@CCControl(name = "life time", min = 0, max = 100)
	private static float _cLifeTime = 0;
	
	@CCControl(name = "random velocity scale", min = 0, max = 1000)
	private static float _cRandomScale = 0;
	
	@CCControl(name = "user velocity scale", min = 0, max = 1)
	private static float _cVelocityScale = 0;
	
	private CCListenerManager<ILBubbleListener> _myEvents = CCListenerManager.create(ILBubbleListener.class);

	private CCGPUParticleGroup _myParticleGroup;
	
	private CCVector3f _myCenter;
	private CCVector2f _my2DCenter;
	
	private ILBubbleState _myCurrentState;
	private ILBubbleState _myUnusedState;
	private ILBubbleOpeningState _myOpeningState;
	private ILBubbleOpenState _myOpenState;
	private ILBubbleClosingState _myClosingState;
	private ILBubbleCloseState _myCloseState;
	
	private ILContent _myContent;
	
	public ILBubble(CCGPUParticleGroup theParticleGroup, ILContent theContent) {
		_myParticleGroup = theParticleGroup;
		_myContent = theContent;
		_myCenter = new CCVector3f();
		_my2DCenter = new CCVector2f();
		
		_myUnusedState = new ILBubbleState();
		_myOpeningState = new ILBubbleOpeningState();
		_myOpenState = new ILBubbleOpenState();
		_myClosingState = new ILBubbleClosingState();
		_myCloseState = new ILBubbleCloseState();
		_myCurrentState = _myUnusedState;
	}
	
	public ILContent content() {
		return _myContent;
	}
	
	public ILBubble(CCGPUParticleGroup theParticleGroup) {
		this(theParticleGroup, null);
	}
	
	public boolean isContentBubble() {
		return _myContent != null;
	}
	
	public void open() {
		setState(_myOpeningState);
	}
	
	public CCListenerManager<ILBubbleListener> events(){
		return _myEvents;
	}
	
	public CCVector3f center() {
		return _myCenter;
	}
	
	public CCVector2f center2D() {
		return _my2DCenter;
	}
	
	public CCGPUParticleGroup particleGroup() {
		return _myParticleGroup;
	}
	
	public float progress() {
		return (float)_myParticleGroup.particlesInUse() / _myParticleGroup.size();
	}
	
	public void setState(ILBubbleState theState) {
		_myCurrentState.onEnd();
		_myCurrentState = theState;
		_myCurrentState._myTimer = 0;
		_myCurrentState.onStart();
	}
	
	public void reset() {
		setState(_myUnusedState);
	}
	
	public void close() {
		CCGPUTargetPointSetSetup myTargetSetup = new CCGPUTargetPointSetSetup();
		myTargetSetup.points().add(new CCVector4f(0f, 0f, 0f, 0f));
		_myEvents.proxy().onClose(this);
		if(_myContent != null)_myContent.close();
	}
	
	public void closed() {
		_myParticleGroup.kill();
		_myEvents.proxy().onClosed(this);
	}

	protected boolean emitParticles(CCVector3f thePosition, CCVector3f theVelocity) {
		boolean myEmitedParticle = false;
		for (int i = 0; i < _myParticleGroup.numberOfParticles(); i++) {
			CCVector3f myVelocity = theVelocity.clone();
			myVelocity.scale(1000 * _cVelocityScale);
			myVelocity.add(CCVecMath.random3f(CCMath.random(_cRandomScale)));
			myEmitedParticle |= _myParticleGroup.allocateParticle(thePosition, myVelocity, CCMath.random(_cLifeTime), true) != null;
		}
		return myEmitedParticle;
	}
	
	public void update(final float theDeltaTime) {
		_myCurrentState.update(theDeltaTime);
	}
	
	
}
