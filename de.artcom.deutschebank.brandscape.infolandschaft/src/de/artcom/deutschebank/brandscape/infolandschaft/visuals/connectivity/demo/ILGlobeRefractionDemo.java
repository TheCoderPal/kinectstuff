package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.demo;

import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.events.CCKeyEvent;
import cc.creativecomputing.events.CCMouseAdapter;
import cc.creativecomputing.events.CCMouseEvent;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.export.CCScreenCapture;
import cc.creativecomputing.graphics.shader.imaging.CCGPUSeperateGaussianBlur;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.texture.CCTextureData;
import cc.creativecomputing.graphics.texture.CCTextureIO;
import cc.creativecomputing.math.util.CCArcball;
import cc.creativecomputing.timeline.CCUITimelineConnector;
import cc.creativecomputing.timeline.view.swing.SwingTimelineContainer;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILBloomPass;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILInteractionManager;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILInteractionManager.ILInteractionSource;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.globe.ILRefractedGlobe;

public class ILGlobeRefractionDemo extends CCApp {
	
	private CCArcball _myArcball;
	
	private CCTexture2D _myGlobeBackgroundTexture;
	private CCTexture2D _myGlobeForegroundTexture;

	private ILRefractedGlobe _myGlobe;
	private ILInteractionManager _myInteractionManager;
	
	private CCUITimelineConnector _myTimelineConnection;
	private SwingTimelineContainer _myTimeline;
	
	private ILInteractionSource _myMouseSource;
	

	private ILBloomPass _myBloomPass;
	
	public final static float MAXIMUM_BLUR_RADIUS = 150;
	
	@CCControl(name = "blur radius", min = 0, max = MAXIMUM_BLUR_RADIUS)
	private float _cBlurRadius = MAXIMUM_BLUR_RADIUS;
	
	private CCGPUSeperateGaussianBlur _myBlur;
	
	private List<CCTextureData> _myRoomTextures;
	
	private CCTexture2D _myBackgroundTexture;

	@Override
	public void setup() {

//		_myTimeline = new SwingTimelineContainer();
//		_myTimelineConnection = new CCUITimelineConnector(this, _myTimeline);
//		_myTimeline.setSize(1400, 300);
		_myRoomTextures = CCTextureIO.newTextureDatas("room");
		
		_myBackgroundTexture = new CCTexture2D(CCTextureIO.newTextureData("room/room09.png"));
		_myGlobeBackgroundTexture = new CCTexture2D(CCTextureIO.newTextureData("room/room09c.png"));
		_myGlobeForegroundTexture = new CCTexture2D(CCTextureIO.newTextureData("room/room09d.png"));
		_myArcball = new CCArcball(this);
		
		_myMouseSource = new ILInteractionManager.ILInteractionMouseSource(this);
		
		addMouseListener(new CCMouseAdapter() {
			
			@Override
			public void mousePressed(CCMouseEvent theEvent) {
				_myInteractionManager.addSource(_myMouseSource);
			}
			
			
			@Override
			public void mouseReleased(CCMouseEvent theEvent) {
				_myInteractionManager.removeSource(_myMouseSource);
			}
		});
		_myGlobe = new ILRefractedGlobe(this, _myGlobeBackgroundTexture, _myGlobeForegroundTexture);
		_myInteractionManager = new ILInteractionManager(this, _myGlobe);

		_myBloomPass = new ILBloomPass(this);
		

		_myBlur = new CCGPUSeperateGaussianBlur(20, width, height);
		addControls("blur", "blur", this);
	}

	@Override
	public void update(final float theDeltaTime) {
//		_myTimeline.update(theDeltaTime);
		_myGlobe.update(theDeltaTime);
		_myInteractionManager.update(theDeltaTime);
		_myBlur.radius(_cBlurRadius);
	}

	@Override
	public void draw() {
		_myBloomPass.startBloom(g);
		g.clear();
		g.color(255);
		g.image(_myBackgroundTexture,-width/2, -height/2, width, height);
		g.pushMatrix();
//		_myArcball.draw(g);
		_myGlobe.draw(g);
		g.popMatrix();
		
		g.clearDepthBuffer();
		_myBloomPass.endBloom(g);
		
		_myBloomPass.startBlur(g);
		g.clear();
		
		g.blend();
		_myGlobe.drawStreams(g);
		g.color(255);
		g.blend(CCBlendMode.ADD);
		_myInteractionManager.drawBlurred(g);
		_myBloomPass.endBlur(g);
		
		g.blend();
		g.text(frameRate, -width/2 + 20 , -height/2 + 20);
	}
	
	private int _myTextureIndex1 = 0;
	private int _myTextureIndex2 = 0;
	private int _myTextureIndex3 = 0;
	
	@Override
	public void keyPressed(CCKeyEvent theKeyEvent) {
		switch(theKeyEvent.keyCode()) {
		case CCKeyEvent.VK_RIGHT:
			CCTextureData myData = _myRoomTextures.get(_myTextureIndex1);
			_myGlobeBackgroundTexture.data(myData);
			_myTextureIndex1++;
			_myTextureIndex1 %= _myRoomTextures.size();
			break;
		case CCKeyEvent.VK_LEFT:
				myData = _myRoomTextures.get(_myTextureIndex2);
				_myGlobeForegroundTexture.data(myData);
				_myTextureIndex2++;
				_myTextureIndex2 %= _myRoomTextures.size();
				break;
		case CCKeyEvent.VK_UP:
			myData = _myRoomTextures.get(_myTextureIndex3);
			_myBackgroundTexture.data(myData);
			_myTextureIndex3++;
			_myTextureIndex3 %= _myRoomTextures.size();
			break;
		case CCKeyEvent.VK_S:
			CCScreenCapture.capture("export4/frame_"+frameCount+".png", width, height);
			break;
		}
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(ILGlobeRefractionDemo.class);
		myManager.settings().size(1920, 1080);
		myManager.settings().antialiasing(8);
		myManager.start();
		
//		CCApplicationManager myManager = new CCApplicationManager(ILGlobeRefractionDemo.class);
//		myManager.settings().size(1920, 1080);
//		myManager.settings().undecorated(true);
//		myManager.settings().location(0,40);
//		myManager.settings().antialiasing(8);
//		myManager.settings().display(1);
//		myManager.start();
	}
}

