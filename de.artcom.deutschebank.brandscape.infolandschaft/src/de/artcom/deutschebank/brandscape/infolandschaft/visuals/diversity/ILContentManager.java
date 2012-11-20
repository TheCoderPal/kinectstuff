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

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.font.CCFontIO;
import cc.creativecomputing.graphics.font.CCFontSettings;
import cc.creativecomputing.graphics.font.CCTextureMapFont;
import cc.creativecomputing.graphics.font.text.CCTextAlign;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector2f;
import cc.creativecomputing.xml.CCXMLElement;
import cc.creativecomputing.xml.CCXMLIO;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILBubble.ILBubbleListener;
import de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity.ILText.ILTextSettings;

/**
 * @author christianriekoff
 *
 */
public class ILContentManager implements ILBubbleListener{
	private static enum ILContentState{
		OPENING, CLOSING, CLOSED, OPEN
	}
	
	public static class ILTips{
		
		@CCControl(name = "tip time", min = 0, max = 30)
		private float _cTipTime = 0;
		
		@CCControl(name = "tip x", min = -1000, max = 1000)
		private float _cTipX = 0;
		@CCControl(name = "tip y", min = -1000, max = 1000)
		private float _cTipY = 0;

		private List<ILContent> _myTipList = new ArrayList<ILContent>();
		private ILContent _myCurrentTip;
		
		private float _myTipTime = 0;
		
		private int _myTipIndex = 0;
		
		private boolean _myIsOpen = false;
		
		public ILTips() {
		}
		
		public void open() {
			if(_myIsOpen)return;
			
			_myTipIndex = 0;
			_myTipTime = 0;
			_myCurrentTip = _myTipList.get(_myTipIndex);
			_myCurrentTip.open();
			_myIsOpen = true;
		}
		
		public void close() {
			if(!_myIsOpen)return;
			
			_myIsOpen = false;
			if(_myCurrentTip != null)_myCurrentTip.close();
		}
		
		public void update(float theDeltaTime) {
			for(ILContent myTip:_myTipList) {
				myTip.position(new CCVector2f(_cTipX, _cTipY));
				myTip.update(theDeltaTime);
			}
			
			if(!_myIsOpen)return;
			
			_myTipTime += theDeltaTime;
			if(_myTipTime > _cTipTime) {
				_myTipTime -= _cTipTime;
				_myTipIndex++;
				_myTipIndex %= _myTipList.size();
				
				if(_myCurrentTip != null)_myCurrentTip.close();
				_myCurrentTip = _myTipList.get(_myTipIndex);
				_myCurrentTip.open();
			}
			
			
		}
		
		public void draw(CCGraphics g) {
			for(ILContent myTip:_myTipList) {
				myTip.draw(g);
			}
		}
	}
	
	public static class ILContent{
		
		@CCControl(name = "shadow settings")
		private static ILTextSettings _cShadowSettings = new ILTextSettings();
		@CCControl(name = "text settings")
		private static ILTextSettings _cTextSettings = new ILTextSettings();
		
		@CCControl(name = "open delay", min = 0, max = 5)
		private static float _cOpenDelay;

		@CCControl(name = "open time", min = 0, max = 5)
		private static float _cOpenTime;

		@CCControl(name = "close time", min = 0, max = 5)
		private static float _cCloseTime;
		
		private ILText _myContent;
		private ILText _myContentShadow;
		
		private String _myEnString;
		private String _myDeString;
		
		private ILContentState _myState = ILContentState.CLOSED;
		
		private float _myBlend = 0;
		private int _myNumberOfParticles;
		
		private boolean _myShowEnglish;
		
		private float _myRadiusScale;
		
		public ILContent(
			CCXMLElement theDataXML, 
			CCTextureMapFont theTextFont, 
			CCTextureMapFont theShadowFont, 
			boolean theShowEnglish, 
			float theLeading
		){
			_myContentShadow = new ILText(theShadowFont, _cShadowSettings);
			_myContentShadow.leading(theLeading);
			_myContent = new ILText(theTextFont, _cTextSettings);
			_myContent.leading(theLeading);
			
			_myEnString = theDataXML.child("en").content();
			_myDeString = theDataXML.child("de").content();
			
			_myNumberOfParticles = theDataXML.intAttribute("number",0);
			
			_myShowEnglish = theShowEnglish;
			
			_myRadiusScale = radiusFromVolume(_myNumberOfParticles);
		}
		
		public float radiusScale() {
			return _myRadiusScale;
		}
		
		public int numberOfParticles() {
			return _myNumberOfParticles;
		}
		
		public void open() {
			if(_myState == ILContentState.OPEN || _myState == ILContentState.OPENING)return;
			
			_myBlend = -_cOpenDelay / _cOpenTime;
			_myState = ILContentState.OPENING;
			
			if(_myShowEnglish) {
				_myContent.text(_myEnString);
				_myContentShadow.text(_myEnString);
			}else {
				_myContent.text(_myDeString);
				_myContentShadow.text(_myDeString);
			}
			
			_myShowEnglish = !_myShowEnglish;
		}
		
		public void close() {
			if(_myState == ILContentState.CLOSED || _myState == ILContentState.CLOSING) return;
				
			_myState = ILContentState.CLOSING;
		}
		
		public void update(float theDeltaTime) {
			switch(_myState) {
			case OPENING:
				_myBlend += theDeltaTime / _cOpenTime;
				if(_myBlend >= 1f) {
					_myBlend = 1;
					_myState = ILContentState.OPEN;
				}
				break;
			case CLOSING:
				_myBlend -= theDeltaTime / _cCloseTime;
				if(_myBlend <= 0f) {
					_myBlend = 0;
					_myState = ILContentState.CLOSED;
				}
				break;
			}
			
			_myContent.progress(_myBlend);
			_myContentShadow.progress(_myBlend);
//			_mySelectProgress -= theDeltaTime;
//			_mySelectProgress = CCMath.saturate(_mySelectProgress);
		}
		
		public ILText content() {
			return _myContent;
		}
		
		public ILText contentShadow() {
			return _myContentShadow;
		}
		
		public void position(CCVector2f thePosition) {
			_myContent.position(thePosition);
			_myContentShadow.position(thePosition);
		}
		
		public void draw(CCGraphics g) {
			if(_myState == ILContentState.CLOSED)return;
			if(_myBlend <= 0) {
				return;
			}
			
			_myContentShadow.draw(g);
			_myContent.draw(g);
		}
	}
	
	@CCControl(name = "headline x", min = -1000, max = 1000)
	private float _cHeadLineX = 0;
	@CCControl(name = "headline y", min = -1000, max = 1000)
	private float _cHeadLineY = 0;
	
	@CCControl(name="show headline", external = true)
	private boolean _cShowHeadline = false;
	
	@CCControl(name="show content", external = true)
	private boolean _cShowContent = false;

	private List<ILContent> _myContentList = new ArrayList<ILContent>();
	private ILTips _myTips;
	
	private ILTips _myHeadline;
	
	private int _myOpenContents = 0;

	private int _myNumberOfBubbles;
	private int _myNumberOfParticles;
	
	public ILContentManager() {
		CCFontSettings myFontSettings = new CCFontSettings("DeuBaUnivers-Regular", 30);
		CCTextureMapFont myTextFont = CCFontIO.createTextureMapFont(myFontSettings);
		
		CCFontSettings myShadowFontSettings = new CCFontSettings("DeuBaUnivers-Regular", 30);
		myShadowFontSettings.blurRadius(10);
		CCTextureMapFont myShadowFont = CCFontIO.createTextureMapFont(myShadowFontSettings);
		
		CCFontSettings myHeadlineSettings = new CCFontSettings("DeuBaUnivers-Regular", 60);
		CCTextureMapFont myHeadlineFont = CCFontIO.createTextureMapFont(myHeadlineSettings);
		
		CCFontSettings myShadowHeadlineSettings = new CCFontSettings("DeuBaUnivers-Regular", 60);
		myShadowHeadlineSettings.blurRadius(20);
		CCTextureMapFont myShadowHeadlineFont = CCFontIO.createTextureMapFont(myShadowHeadlineSettings);
		
		CCXMLElement myDiversityContentXML = CCXMLIO.createXMLElement("diversity_content_test.xml");
		
		_myHeadline = createTips(myDiversityContentXML.child("headline"), myHeadlineFont, myShadowHeadlineFont, CCTextAlign.LEFT);
		_myTips = createTips(myDiversityContentXML.child("tips"), myHeadlineFont, myShadowHeadlineFont, CCTextAlign.CENTER);
		
		int myMaximumNumberOfParticles = 0;
		int myCounter = 0;
		
		for(CCXMLElement myItemXML:myDiversityContentXML.child("items")) {
			ILContent myContent = new ILContent(myItemXML, myTextFont, myShadowFont, myCounter++ % 2 == 0, 36);
			_myContentList.add(myContent);
			_myNumberOfBubbles++;
			int myNumber = myItemXML.intAttribute("number");
			_myNumberOfParticles += myItemXML.intAttribute("number");
			myMaximumNumberOfParticles = CCMath.max(myMaximumNumberOfParticles, myNumber);
		}
		float myMaximumRadius = radiusFromVolume(myMaximumNumberOfParticles);
		for(ILContent myContent:_myContentList) {
			myContent._myRadiusScale /= myMaximumRadius;
		}
	}
	
	private ILTips createTips(CCXMLElement theContent, CCTextureMapFont theHeadlineFont, CCTextureMapFont theShadowFont, CCTextAlign theAlign) {
		int myCounter = 0;
		ILTips myTips = new ILTips();
		for(CCXMLElement myTipXML:theContent) {
			ILContent myContent = new ILContent(myTipXML, theHeadlineFont, theShadowFont, myCounter++ % 2 == 0, 72);
			myContent._myContent.align(theAlign);
			myContent._myContentShadow.align(theAlign);
			myTips._myTipList.add(myContent);
		}
		return myTips;
	}
	
	private static float radiusFromVolume(float theVolume) {
		return CCMath.pow(theVolume / CCMath.PI * 0.75f, 1f/3);
	}
	
	public int numberOfParticles() {
		return _myNumberOfParticles;
	}
	
	public List<ILContent> contentList(){
		return _myContentList;
	}
	
	public ILTips tips() {
		return _myTips;
	}
	
	public ILTips headline() {
		return _myHeadline;
	}

	@Override
	public void onOpen(ILBubble theBubble) {
		if(!theBubble.isContentBubble())return;
		
//		_myHeadline.close();
		_myTips.close();
		_myOpenContents++;
	}

	@Override
	public void onClose(ILBubble theBubble) {
		if(!theBubble.isContentBubble())return;
			
		_myOpenContents--;
		if(_myOpenContents == 0) {
//			_myHeadline.open();
			_myTips.open();
		}
	}

	@Override
	public void onClosed(ILBubble theBubble) {
		// TODO Auto-generated method stub
		
	}
	
	public void update(final float theDeltaTime) {
		for(ILContent myContent:_myContentList) {
			myContent.update(theDeltaTime);
		}
		if(_cShowHeadline && _myOpenContents == 0) {
			_myHeadline.open();
			_myTips.open();
		}
		_myHeadline.update(theDeltaTime);
		_myTips.update(theDeltaTime);
	}
	
	public void draw(CCGraphics g) {
		for(ILContent myContent:_myContentList) {
			myContent.draw(g);
		}
		_myHeadline.draw(g);
		_myTips.draw(g);
	}
}
