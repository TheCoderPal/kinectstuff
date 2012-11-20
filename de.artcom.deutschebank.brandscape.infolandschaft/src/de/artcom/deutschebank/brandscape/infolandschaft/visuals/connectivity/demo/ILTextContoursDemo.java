package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.demo;


import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.events.CCKeyEvent;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILContourText;



public class ILTextContoursDemo extends CCApp{
	
	private ILContourText _myContourText;

	public void setup(){
		_myContourText = new ILContourText("San Francisco");
		g.clearColor(0.3f);
		
		addControls("app", "text", _myContourText);
	}
	
	
	public void update(final float theDeltaTime) {
		_myContourText.update(theDeltaTime);
	}
	
	public void draw(){
		g.clear();
		_myContourText.draw(g);
	}
	
	
	@Override
	public void keyPressed(CCKeyEvent theKeyEvent) {
		switch(theKeyEvent.keyCode()) {
		case CCKeyEvent.VK_R:
			_myContourText.reset();
			break;
		}
	}
	
	public static void main(String[] args){
		final CCApplicationManager myManager = new CCApplicationManager(ILTextContoursDemo.class);
		myManager.settings().antialiasing(8);
		myManager.settings().size(1200, 800);
		myManager.settings().antialiasing(8);
		myManager.start();
	}
}
