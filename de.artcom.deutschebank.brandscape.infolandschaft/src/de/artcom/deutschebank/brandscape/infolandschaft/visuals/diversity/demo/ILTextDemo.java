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
package de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.demo;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.events.CCKeyEvent;
import cc.creativecomputing.graphics.font.CCFontIO;
import cc.creativecomputing.graphics.font.CCFontSettings;
import cc.creativecomputing.graphics.font.CCTextureMapFont;
import cc.creativecomputing.graphics.font.text.CCLineBreakMode;
import cc.creativecomputing.graphics.font.util.CCLoremIpsumGenerator;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILText;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILText.ILTextSettings;

/**
 * Demonstrated how to extends the text class to create animated text
 * @author christianriekoff
 *
 */
public class ILTextDemo extends CCApp {
	
	
	@CCControl(name = "shadow settings")
	private static ILTextSettings _cShadowSettings = new ILTextSettings();
	@CCControl(name = "text settings")
	private static ILTextSettings _cTextSettings = new ILTextSettings();
	
	@CCControl(name = "progress", min = 0, max = 1)
	private float _cProgress = 0;

	private ILText _myText;
	private ILText _myTextShadow;
	
	CCTextureMapFont myTextFont;
	CCTextureMapFont myShadowFont;
	
	public void setup() {
		
		String myText = CCLoremIpsumGenerator.generate(5)+"\n"+CCLoremIpsumGenerator.generate(5);
		
		CCFontSettings myFontSettings = new CCFontSettings("DeuBaUnivers-Regular", 30);
		myTextFont = CCFontIO.createTextureMapFont(myFontSettings);
		_myText = new ILText(myTextFont, _cTextSettings);
		_myText.dimension(300, 400);
		_myText.text(myText);
		_myText.position(-300,300);
		_myText.lineBreak(CCLineBreakMode.NONE);	
		
		CCFontSettings myShadowFontSettings = new CCFontSettings("DeuBaUnivers-Regular", 30);
		myShadowFontSettings.blurRadius(10f);
		myShadowFont = CCFontIO.createTextureMapFont(myShadowFontSettings);
		_myTextShadow = new ILText(myShadowFont, _cShadowSettings);
		_myTextShadow.dimension(300, 400);
		_myTextShadow.text(myText);
		_myTextShadow.position(-300,300);
		_myTextShadow.lineBreak(CCLineBreakMode.NONE);	
		
		addControls("text", "text", ILText.class);
		addControls("text", "app", this);
	}
	
	public void update(final float theDeltaTime) {
//		_myText.update(theDeltaTime);
		_myText.progress(_cProgress);
		_myTextShadow.progress(_cProgress);
	}

	public void draw() {
		g.clearColor(200);
		g.clear();
		g.color(255);
		
		g.blend();
		_myTextShadow.draw(g);
		_myText.draw(g);
		
//		g.image(myTextFont.texture(), -width/2, -height/2);
	}
	
	@Override
	public void keyPressed(CCKeyEvent theKeyEvent) {
		switch(theKeyEvent.keyCode()) {
		case CCKeyEvent.VK_O:
			_myText.open();
			break;
		case CCKeyEvent.VK_C:
			break;
		}
	}

	public static void main(String[] args) {
		final CCApplicationManager myManager = new CCApplicationManager(ILTextDemo.class);
		myManager.settings().size(800, 800);
		myManager.settings().antialiasing(8);
		myManager.start();
	}
}

