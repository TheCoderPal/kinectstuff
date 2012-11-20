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

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.cv.openni.util.CCOpenNIInteractionArea;
import cc.creativecomputing.events.CCMouseAdapter;
import cc.creativecomputing.events.CCMouseEvent;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.texture.CCTextureIO;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILApp;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILTheme;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILInteractionManager.ILInteractionSource;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.globe.ILRefractedGlobe;

/**
 * @author christianriekoff
 *
 */
public class ILConnectivityTheme extends ILTheme{
	
	private CCTexture2D _myBackgroundTexture;
	private CCTexture2D _myGlobeBackgroundTexture;
	private CCTexture2D _myGlobeForegroundTexture;

	private ILRefractedGlobe _myGlobe;
	private ILInteractionManager _myInteractionManager;
	
	private ILInteractionSource _myMouseSource;
	private ILConnectivityUserManager _myAvatarManager;
	

	private ILBloomPass _myBloomPass;
	
	@CCControl(name = "background alpha", min = 0, max = 1, external = true)
	private float _cBackgroundAlpha;

	public ILConnectivityTheme(CCApp theApp, CCOpenNIInteractionArea theArea) {
		theApp.addControls("connectivity", "app", this);
		
		_myBackgroundTexture = new CCTexture2D(CCTextureIO.newTextureData("room/background.jpg"));
		_myGlobeBackgroundTexture = new CCTexture2D(CCTextureIO.newTextureData("room/room09c.png"));
		_myGlobeForegroundTexture = new CCTexture2D(CCTextureIO.newTextureData("room/room09d.png"));

		_myMouseSource = new ILInteractionManager.ILInteractionMouseSource(theApp);
		theApp.addMouseListener(new CCMouseAdapter() {
			
			@Override
			public void mousePressed(CCMouseEvent theEvent) {
				_myInteractionManager.addSource(_myMouseSource);
			}
			
			
			@Override
			public void mouseReleased(CCMouseEvent theEvent) {
				_myInteractionManager.removeSource(_myMouseSource);
			}
		});
		_myGlobe = new ILRefractedGlobe(theApp, _myGlobeBackgroundTexture, _myGlobeForegroundTexture);
		_myInteractionManager = new ILInteractionManager(theApp, _myGlobe);
		_myAvatarManager = new ILConnectivityUserManager(theApp, _myGlobeBackgroundTexture, _myInteractionManager);
		theArea.events().add(_myAvatarManager);

		_myBloomPass = new ILBloomPass(theApp);
	}
	
	@Override
	public void update(final float theDeltaTime) {
		if(_cBackgroundAlpha == 0)return;
//		_myTimeline.update(theDeltaTime);
		_myGlobe.update(theDeltaTime);
		_myAvatarManager.update(theDeltaTime);
		_myInteractionManager.update(theDeltaTime);
	}
	
	@Override
	public void draw(CCGraphics g) {

		if(_cBackgroundAlpha == 0)return;
		_myBloomPass.startBloom(g);
		g.color(1f, _cBackgroundAlpha);
		g.image(_myBackgroundTexture,-g.width/2, -g.height/2, g.width, g.height);
		
		g.pushMatrix();
		g.scale(ILApp.SCALE);
		g.depthTest();
		_myGlobe.alpha(_cBackgroundAlpha);
		_myGlobe.draw(g);
		_myAvatarManager.alpha(_cBackgroundAlpha);
		_myAvatarManager.drawStraight(g);
		g.popMatrix();
		
		g.clearDepthBuffer();
		_myBloomPass.endBloom(g);
		
		_myBloomPass.startBlur(g);
		g.clear();
		
		g.blend();
		g.pushMatrix();
		g.scale(ILApp.SCALE);
		_myGlobe.drawStreams(g);
		g.color(255);
		g.blend(CCBlendMode.ADD);
		_myInteractionManager.drawBlurred(g);
		if(_myAvatarManager != null)_myAvatarManager.drawBlurred(g);
		g.blend(CCBlendMode.ADD);
		g.popMatrix();
		_myBloomPass.endBlur(g);
		
		g.blend();
		g.pushMatrix();
		g.scale(ILApp.SCALE);
		_myInteractionManager.draw(g);

		g.popMatrix();
	}
}
