package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.events.CCKeyEvent;
import cc.creativecomputing.graphics.export.CCScreenCapture;
import cc.creativecomputing.timeline.CCUITimelineConnector;
import cc.creativecomputing.timeline.view.swing.SwingTimelineContainer;

public class ILConnectivityApp3 extends CCApp {
	
	
	
	
	private CCUITimelineConnector _myTimelineConnection;
	private SwingTimelineContainer _myTimeline;
	


	@Override
	public void setup() {

//		_myTimeline = new SwingTimelineContainer();
//		_myTimelineConnection = new CCUITimelineConnector(this, _myTimeline);
//		_myTimeline.setSize(1400, 300);
		
		
		
	}

	@Override
	public void update(final float theDeltaTime) {
//		_myTimeline.update(theDeltaTime);
	}

	@Override
	public void draw() {
		
		
		
		
		g.blend();
		g.text(frameRate, -width/2 + 20 , -height/2 + 20);
	}
	
	@Override
	public void keyPressed(CCKeyEvent theKeyEvent) {
		switch(theKeyEvent.keyCode()) {
		case CCKeyEvent.VK_S:
			CCScreenCapture.capture("export1/frame_"+frameCount+".png", width, height);
			break;
		}
	}

	public static void main(String[] args) {
		
		CCApplicationManager myManager = new CCApplicationManager(ILConnectivityApp3.class);
		myManager.settings().size(1920, 1080);
		myManager.settings().antialiasing(8);
		myManager.settings().vsync(false);
		myManager.start();
		
//		CCApplicationManager myManager = new CCApplicationManager(ILConnectivityApp3.class);
//		myManager.settings().size(1920, 1080);
//		myManager.settings().undecorated(true);
//		myManager.settings().location(0,0);
//		myManager.settings().antialiasing(8);
//		myManager.settings().uiTranslation(0, 40);
////		myManager.settings().display(1);
//		myManager.start();
	}
}

