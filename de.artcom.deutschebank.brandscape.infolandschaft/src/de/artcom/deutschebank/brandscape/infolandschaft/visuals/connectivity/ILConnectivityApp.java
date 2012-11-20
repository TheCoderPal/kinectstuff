package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity;

import cc.creativecomputing.CCApplicationManager;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILAbstractApp;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.ILApp;

public class ILConnectivityApp extends ILAbstractApp {
	
	@Override
	public void initThemes() {
		addTheme(new ILConnectivityTheme(this, _myInteractionArea));
	}
	
	

	public static void main(String[] args) {
		/*
		int width = (int)(ILApp.WIDTH * ILApp.SCALE);
		int height = (int)(ILApp.HEIGHT * ILApp.SCALE);
		CCApplicationManager myManager = new CCApplicationManager(ILConnectivityApp.class);
		myManager.settings().size(width, height);
		myManager.settings().location(0, 0);
		// myManager.settings().undecorated(true);
		// myManager.settings().display(1);
		// myManager.settings().uiTranslation(120, 130);
		// myManager.settings().displayMode(CCDisplayMode.FULLSCREEN);
		myManager.settings().antialiasing(8);
		myManager.start();*/
		
		CCApplicationManager myManager = new CCApplicationManager(ILConnectivityApp.class);
		myManager.settings().size(1920, 1079);
		myManager.settings().undecorated(true);
		myManager.settings().location(0,0);
		myManager.settings().antialiasing(8);
		myManager.settings().uiTranslation(0, 40);
////		myManager.settings().display(1);
		myManager.start();
	}
}
