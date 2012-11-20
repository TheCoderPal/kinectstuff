package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.demo;


import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.events.CCKeyEvent;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.export.CCScreenCapture;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.texture.CCTextureIO;
import cc.creativecomputing.math.util.CCArcball;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILBloomPass;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.avatar.ILConnectivityAvatar;

public class ILConnectivityAvatarDemo extends CCApp {

	private CCArcball _myArcball;
	private ILConnectivityAvatar _myAvatar;
	
	private CCTexture2D _myBackgroundTexture;
	private ILBloomPass _myBloomPass;

	public void setup() {
		_myArcball = new CCArcball(this);

		_myBackgroundTexture = new CCTexture2D(CCTextureIO.newTextureData("room/room09.png"));
		
		_myAvatar = new ILConnectivityAvatar(this, _myBackgroundTexture);
		_myBloomPass = new ILBloomPass(this);
	}
	
	@Override
	public void update(float theDeltaTime) {
		_myAvatar.update(theDeltaTime);
	}

	public void draw() {
		
		g.clearColor(0);
		g.clear();
		_myBloomPass.startBloom(g);
		g.clear();
		g.color(255);
		g.image(_myBackgroundTexture,-width/2, -height/2, width, height);
		g.pushMatrix();
		g.noDepthTest();
		_myArcball.draw(g);
		_myAvatar.drawStraight(g);
		g.popMatrix();
		
		g.clearDepthBuffer();
		_myBloomPass.endBloom(g);
		
		_myBloomPass.startBlur(g);
		g.clear();
		
		g.blend();
		g.color(255);
		g.blend(CCBlendMode.ADD);
		g.pushMatrix();
		_myArcball.draw(g);
		_myAvatar.drawBlurred(g);
		g.popMatrix();
		_myBloomPass.endBlur(g);
		
		g.depthTest();
		g.blend();
		g.text(frameRate, -width/2 + 20 , -height/2 + 20);
	}
	
	@Override
	public void keyPressed(CCKeyEvent theKeyEvent) {
		switch(theKeyEvent.keyCode()) {
		case CCKeyEvent.VK_S:
			CCScreenCapture.capture("export_avatar1/frame_"+frameCount+".png", width, height);
			break;
		}
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(ILConnectivityAvatarDemo.class);
		myManager.settings().size(1500, 900);
		myManager.settings().antialiasing(8);
		myManager.start();
	}
}
