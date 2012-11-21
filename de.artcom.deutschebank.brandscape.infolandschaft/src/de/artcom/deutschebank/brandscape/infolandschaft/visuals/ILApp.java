package de.artcom.deutschebank.brandscape.infolandschaft.visuals;

import cc.creativecomputing.CCApplicationManager;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.ILConnectivityTheme;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILDiversityApp;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILDiversityTheme;

public class ILApp extends ILAbstractApp {
	
	public static final float SCALE = 1f;
	
	public static final int WIDTH = 1920;
	
	public static final int HEIGHT = 1080;
	
	public void initThemes(){
		addTheme(new ILDiversityTheme(this,_myInteractionArea));
		addTheme(new ILConnectivityTheme(this,_myInteractionArea));
	}
	
	

	public static void main(String[] args) {
		/*
		CCApplicationManager myManager = new CCApplicationManager(ILApp.class);
		myManager.settings().size(1600, 1000);
		myManager.settings().location(0, 0);
		// myManager.settings().undecorated(true);
		// myManager.settings().display(1);
		// myManager.settings().uiTranslation(120, 130);
		// myManager.settings().displayMode(CCDisplayMode.FULLSCREEN);
		myManager.settings().antialiasing(8);
		myManager.start();*/
		
		CCApplicationManager myManager = new CCApplicationManager(ILApp.class);
		myManager.settings().size(1920, 1079);
		myManager.settings().undecorated(true);
		myManager.settings().location(0,0);
		myManager.settings().antialiasing(8);
		myManager.settings().uiTranslation(0, 40);
////		myManager.settings().display(1);
		myManager.start();
	}
}
