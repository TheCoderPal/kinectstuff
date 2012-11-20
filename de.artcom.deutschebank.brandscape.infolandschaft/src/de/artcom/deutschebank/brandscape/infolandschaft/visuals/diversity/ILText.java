package de.artcom.deutschebank.brandscape.infolandschaft.visuals.diversity;

import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.font.CCFont;
import cc.creativecomputing.graphics.font.text.CCText;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.easing.CCEasing;
import cc.creativecomputing.math.easing.CCEasingMode;
import cc.creativecomputing.math.random.CCRandom;

public class ILText extends CCText {
	
	public static class ILTextSettings{
		@CCControl(name = "min time", min = 0, max = 5)
		private float _cMinTime;
		@CCControl(name = "max time", min = 0, max = 5)
		private float _cMaxTime;
		
		@CCControl(name = "min delay", min = 0, max = 5)
		private float _cMinDelay;
		@CCControl(name = "max delay", min = 0, max = 5)
		private float _cMaxDelay;

		@CCControl(name = "line time", min = 0, max = 5)
		private float _cLineTime;
		

		@CCControl(name = "r", min = 0, max = 1)
		private float _cR;
		@CCControl(name = "g", min = 0, max = 1)
		private float _cG;
		@CCControl(name = "b", min = 0, max = 1)
		private float _cB;
		
		@CCControl(name = "leading", min = 0, max = 100)
		private float _cTextLeading = 0;
	}
	
	private float[][] _myAngles;
	private float[][] _myDelays;
	private float[][] _myFreqs;
	
	private float _myProgress = 0;
	
	private CCRandom _myRandom;
	private long _mySeed;
	
	private ILTextSettings _myTextSettings;

	/**
	 * @param theFont
	 */
	public ILText(CCFont<?> theFont, ILTextSettings theSettings) {
		super(theFont); 
		_myTextSettings = theSettings;
	}

	public void update(float theDeltaTime) {
//		for (int i = 0; i < _myAngles.length; i++) {
//			for (int j = 0; j < _myAngles[i].length; j++) {
//				_myAngles[i][j] += theDeltaTime * _myFreqs[i][j] * _myFreqSign;
//			}
//		}
	}
	
	private void angleRange(float theMin, float theMax) {
		if(_myRandom == null) {

			_mySeed = (long)CCMath.random(_myText.hashCode());
			_myRandom = new CCRandom(_mySeed);
		}
		_myRandom.setSeed(_myText.hashCode());
		for (int i = 0; i < _myDelays.length; i++) {
			for (int j = 0; j < _myDelays[i].length; j++) {
				float myStart = j / (float)_myDelays[i].length * _myTextSettings._cLineTime;
				_myDelays[i][j] = -myStart - _myRandom.random(theMin, theMax);
				_myFreqs[i][j] = _myRandom.random(_myTextSettings._cMinTime, _myTextSettings._cMaxTime);
			}
		}
	}
	
	public void progress(float theProgress) {
		angleRange(_myTextSettings._cMinDelay, _myTextSettings._cMaxDelay);
		
		_myProgress = theProgress * (_myTextSettings._cLineTime + _myTextSettings._cMaxDelay) * 1.1f;
		
		for (int i = 0; i < _myAngles.length; i++) {
			for (int j = 0; j < _myAngles[i].length; j++) {
				_myAngles[i][j] = _myDelays[i][j] + _myProgress;// * _myFreqs[i][j];
			}
		}
	}
	
	public void open() {
		angleRange(_myTextSettings._cMinDelay, _myTextSettings._cMaxDelay);
	}
	
//	public void close() {
//		angleRange(1 + _cMinDelay, 1 + _cMaxDelay);
//		_myFreqSign = -1;
//	}

	@Override
	public void text(String theText) {
		super.text(theText);

		_myAngles = new float[textGrid().gridLines().size()][];
		_myFreqs = new float[textGrid().gridLines().size()][];
		_myDelays = new float[textGrid().gridLines().size()][];
		
		for (int i = 0; i < textGrid().gridLines().size(); i++) {
			int myNumberOfChars = textGrid().gridLines().get(i).myNumberOfChars();
			_myAngles[i] = new float[myNumberOfChars];
			_myFreqs[i] = new float[myNumberOfChars];
			_myDelays[i] = new float[myNumberOfChars];
		}
		

//		angleRange(_myTextSettings._cMinDelay, _myTextSettings._cMaxDelay);
	}

	@Override
	public void draw(CCGraphics g) {
		if(_myProgress <= 0)return;
		
		_myFont.beginText(g);
		int myLine = 0;
		for (CCTextGridLine myGridLines : _myTextGrid.gridLines()) {
			for (int i = 0; i < myGridLines.myNumberOfChars(); i++) {
				float myAlpha = 0;
				if(_myAngles[myLine][i] >= 1) {
					myAlpha = 1;
				}else if(_myAngles[myLine][i] > 0 && _myAngles[myLine][i] < 1) {
					myAlpha = CCEasing.easeInOut(CCEasingMode.SINE, _myAngles[myLine][i]);
				}
				if(myAlpha == 0)continue;
				g.color(_myTextSettings._cR, _myTextSettings._cG, _myTextSettings._cB, myAlpha);
				myGridLines.drawChar(g, i);
			}
			myLine++;
		}
		_myFont.endText(g);
	}
}