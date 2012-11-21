package de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.cv.openni.util.CCOpenNIInteractionArea;
import cc.creativecomputing.cv.openni.util.CCOpenNISkeletonController;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.texture.CCTextureIO;
import cc.creativecomputing.graphics.CCGraphics;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILApp;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILTheme;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILContentManager.ILContent;

public class ILDiversityTheme extends ILTheme {

	@CCControl(name = "mirror", min = 0, max = 1)
	private float _cMirror = 0;
	@CCControl(name = "particle alpha", min = 0, max = 1, external = true)
	private float _cParticleAlpha = 0;
	@CCControl(name = "back brightness", min = 0, max = 1, external = true)
	private float _cBackBrightness = 0;

	@CCControl(name = "mirror correction", min = 0, max = 3)
	private float _cMirrorCorrection = 0;
	
	@CCControl(name = "reset particles", external = true)
	private boolean _cResetParticles = false;
	

	private ILBubbleManager _myBubbleManager;
	private ILMouseSource _myMouseSource;
	private ILDiversityUserManager _myUserManager;
	private ILContentManager _myContentManager;
	
	private CCTexture2D _myBackgroundTexture;

	public ILDiversityTheme(CCApp theApp, CCOpenNIInteractionArea theArea) {
		_myBackgroundTexture = new CCTexture2D(CCTextureIO.newTextureData("120522_diversity_bg.png"));
		theApp.addControls("diversity ui", "theme", this);


		_myContentManager = new ILContentManager();
		theApp.addControls("diversity ui", "content", 1, ILContent.class);
		theApp.addControls("diversity ui", "content man", 1, _myContentManager);
		theApp.addControls("diversity ui", "tips", 1, _myContentManager.tips());
		theApp.addControls("diversity ui", "headline", 1, _myContentManager.headline());

		_myBubbleManager = new ILBubbleManager(theApp, _myContentManager);

		_myMouseSource = new ILMouseSource(theApp);
		theApp.addControls("app", "bubble mouse", 3, _myMouseSource);
//		_myBubbleManager.addSource(_myMouseSource);
		theApp.addControls("app", "text", 3, ILText.class);

		_myUserManager = new ILDiversityUserManager(theApp, _myBubbleManager);
		theArea.events().add(_myUserManager);
		theApp.addControls("diversity", "user", 3, ILDiversityUser.class);
		theApp.addControls("diversity", "skeleton particles", 1, _myUserManager.particles());
	}
	
	private boolean _myLastResetParticles = false;

	@Override
	public void update(float theDeltaTime) {
		_myContentManager.update(theDeltaTime);
		
		if(_cResetParticles && !_myLastResetParticles) {
			_myUserManager.particles().reset();
		}
		_myLastResetParticles = _cResetParticles;
		
		if(_cParticleAlpha > 0) {
			_myUserManager.update(theDeltaTime);
			_myBubbleManager.update(theDeltaTime);
		}

	}

	@CCControl(name = "back", min = -1000, max = 1000)
	private float _cBack = 0;
	

	public void draw(CCGraphics g) {
		g.color(_cBackBrightness);
		g.image(_myBackgroundTexture, -g.width/2, -g.height/2, g.width, g.height);
		
		g.clearDepthBuffer();

		g.noDepthTest();
		

		g.pushMatrix();
		g.scale(ILApp.SCALE);
		g.scale(1f / CCOpenNISkeletonController._cSkeletonScale * 2);
		g.blend(CCBlendMode.BLEND);

		g.pushMatrix();
		g.scale(1, -1, 1);
		g.translate(0, -2 * _myUserManager.particles().floorY() + _cMirrorCorrection * _myUserManager.particles().pointSize(), 0);

		g.color(1f, _cMirror * _cParticleAlpha);
		if(_cParticleAlpha > 0)_myUserManager.particles().draw(g);
		g.popMatrix();

		g.color(1f, _cParticleAlpha);
		g.blend(CCBlendMode.BLEND);
		if(_cParticleAlpha > 0)_myUserManager.particles().draw(g);
		
		for (ILDiversityUser myUser : _myUserManager) {
			myUser.drawSkeleton(g);
		}
		g.color(1f, _cParticleAlpha);
		g.blend(CCBlendMode.BLEND);
		if(_cParticleAlpha > 0)_myBubbleManager.draw(g);
		g.depthTest();
		g.blend();

		g.color(255);
		g.line(-1000, _cBack, _myUserManager.particles().backZ(), 1000, _cBack, _myUserManager.particles().backZ());
		g.popMatrix();
	}
	
	public void drawContent(CCGraphics g) {
		g.pushMatrix();
		g.scale(ILApp.SCALE);
		g.clearDepthBuffer();
		g.blend();
		g.color(255);
		_myContentManager.draw(g);
		
		for (ILDiversityUser myUser : _myUserManager) {
			g.color(255, 100);
			g.pointSize(1);
			
			myUser.drawDebug(g);
		}
		
		g.color(255);
		g.noBlend();
//		_myUserManager.targets().targets().beginDraw();
//		g.clearColor(0,0);
//		g.clear();
//		_myUserManager.targets().targets().endDraw();
//		g.image(_myUserManager.targets().targets(),0,0);
		
		_myBubbleManager.drawContent(g);
		g.popMatrix();
		
		
	}
}
