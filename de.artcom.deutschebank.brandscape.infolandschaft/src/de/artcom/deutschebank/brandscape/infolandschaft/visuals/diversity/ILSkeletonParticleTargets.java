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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCVBOMesh;
import cc.creativecomputing.graphics.shader.CCGLSLShader;
import cc.creativecomputing.graphics.shader.CCShaderTexture;
import cc.creativecomputing.io.CCIOUtil;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector2i;
import cc.creativecomputing.math.CCVector3f;

/**
 * @author christianriekoff
 *
 */
public class ILSkeletonParticleTargets {
	
	private class ILSkeletonEmitPosition{
		private CCVector2i _myTarget;
		private CCVector3f _myLastPosition;
		private CCVector3f _myPosition;
		
		public ILSkeletonEmitPosition(float theWeight, int theTargetX, int theTargetY) {
			_myTarget = new CCVector2i(theTargetX, theTargetY);
		}
		
		public CCVector3f target(float theBlend) {
			if(_myPosition == null)return new CCVector3f();
			if(_myLastPosition == null)return _myPosition;
			return CCVecMath.blend(theBlend, _myPosition, _myLastPosition);
		}
		
		public void update(float theDeltaTime) {
			_myPosition = _myLastPosition;
			int myIndex = (_myTarget.y * _myTargetTexture.width() + _myTarget.x) * 4;
			_myPosition = new CCVector3f(_myCurrentData.get(myIndex), _myCurrentData.get(myIndex + 1), _myCurrentData.get(myIndex + 2));
		}
	}
	
	private class ILEmitPositions{
		private List<ILSkeletonEmitPosition> _myPositions = new ArrayList<ILSkeletonParticleTargets.ILSkeletonEmitPosition>();
		private int _myId;
		
		public ILEmitPositions(int theID) {
			_myId = theID;
		}
		
		public void checkVertex(float[] theVertex, int theTargetX, int theTargetY) {
			for(int i = 0; i < 4; i++) {
				int myVertexID = (int)theVertex[i + 7];
				if(myVertexID == _myId) {
					_myPositions.add(new ILSkeletonEmitPosition(theVertex[i + 3], theTargetX, theTargetY));
				}
			}
		}
		
		public void update(float theDeltaTime) {
			for(ILSkeletonEmitPosition myPosition:_myPositions) {
				myPosition.update(theDeltaTime);
			}
		}
		
		public CCVector3f target(float theBlend) {
			int myTargetID = (int)CCMath.random(_myPositions.size());
			return _myPositions.get(myTargetID).target(theBlend);
		}
	}
	
	private ILEmitPositions _myLeftPositions;
	private ILEmitPositions _myRightPositions;
	
	private IntBuffer _myIndices;
	private FloatBuffer _myPositionBuffer;
	private FloatBuffer _myWeightBuffer;
	private FloatBuffer _myWeightIndexBuffer;
	
	private FloatBuffer _myCurrentData;
	
	private CCVBOMesh _myMesh;
	
	private CCGLSLShader _myWeightsShader;

	private CCShaderTexture _myTargetTexture;
	
	private CCGraphics g;
	
	private int _myXRes;
	private int _myYRes;
	
	private ILDiversityUserManager _myUserManager;

	public ILSkeletonParticleTargets(
		CCApp theApp, 
		ILDiversityUserManager theUserManager,
		int theXRes, int theYRes
	) {
		g = theApp.g;
		
		_myXRes = theXRes;
		_myYRes = theYRes;
		
		_myUserManager = theUserManager;
		 
		_myIndices = _myUserManager.skeletonMesh().indices();
		_myPositionBuffer = _myUserManager.skeletonMesh().positions();
		_myWeightBuffer = _myUserManager.skeletonMesh().skinWeights();
		_myWeightIndexBuffer = _myUserManager.skeletonMesh().skinIndices();
		
		_myLeftPositions = new  ILEmitPositions(_myUserManager.skeletonMesh().skeleton().joint("LeftHand").index());
		_myRightPositions = new  ILEmitPositions(_myUserManager.skeletonMesh().skeleton().joint("RightHand").index());
		
		int myNumberOfPoints = theXRes * theYRes;
		
		_myTargetTexture = new CCShaderTexture(16,4,_myXRes * ILDiversityUserManager.MAX_USER, _myYRes);
		FloatBuffer myTargetIds = FloatBuffer.allocate(myNumberOfPoints * 2);
		FloatBuffer myPositions = FloatBuffer.allocate(myNumberOfPoints * 3);
		FloatBuffer myRandoms = FloatBuffer.allocate(myNumberOfPoints * 3);
		FloatBuffer myWeights = FloatBuffer.allocate(myNumberOfPoints * 4);
		FloatBuffer myWeightIndicess = FloatBuffer.allocate(myNumberOfPoints * 4);
		
		for(int x = 0; x < theXRes;x++) {
			for(int y = 0; y < theYRes;y++) {
				myTargetIds.put(x);
				myTargetIds.put(y);
				
				float[] myVertex = randomPoint();
				
				
				myPositions.put(myVertex[0]);
				myPositions.put(myVertex[1]);
				myPositions.put(myVertex[2]);

				CCVector3f myRandom = CCVecMath.random3f();
				myRandoms.put(myRandom.x);
				myRandoms.put(myRandom.y);
				myRandoms.put(myRandom.z);
				
				myWeights.put(myVertex[3]);
				myWeights.put(myVertex[4]);
				myWeights.put(myVertex[5]);
				myWeights.put(myVertex[6]);
				
				myWeightIndicess.put(myVertex[7]);
				myWeightIndicess.put(myVertex[8]);
				myWeightIndicess.put(myVertex[9]);
				myWeightIndicess.put(myVertex[10]);
				
				_myLeftPositions.checkVertex(myVertex, x, y);
				_myRightPositions.checkVertex(myVertex, x, y);
			}
		}
		
		myTargetIds.rewind();
		myPositions.rewind();
		myRandoms.rewind();
		myWeights.rewind();
		myWeightIndicess.rewind();

		_myMesh = new CCVBOMesh(CCDrawMode.POINTS);
		_myMesh.vertices(myTargetIds, 2);
		_myMesh.textureCoords(0, myPositions, 3);
		_myMesh.textureCoords(1, myWeights, 4);
		_myMesh.textureCoords(2, myWeightIndicess, 4);
		_myMesh.textureCoords(3, myRandoms,3);
		
		_myWeightsShader = new CCGLSLShader(
			CCIOUtil.classPath(this, "particle_weights_vert.glsl"),
			CCIOUtil.classPath(this, "particle_weights_frag.glsl")
		);
		_myWeightsShader.load();
	}
	
	public CCVector3f leftTarget(float theBlend) {
		return _myLeftPositions.target(theBlend);
	}
	
	public CCVector3f rightTarget(float theBlend) {
		return _myRightPositions.target(theBlend);
	}
	
	public CCShaderTexture targets() {
		return _myTargetTexture;
	}
	
	public void update(final float theDeltaTime) {
		
		_myTargetTexture.beginDraw();
		g.clearColor(0, 0);
		g.clear();
		
		for(int i = 0; i < ILDiversityUserManager.MAX_USER;i++) {
			ILDiversityUser myUser = _myUserManager.user(i);
			if(myUser == null)continue;
			
//			if(myUser.setTargets()) {
				g.pushMatrix();
				g.translate(i * _myXRes, 0);
				_myWeightsShader.start();
				_myWeightsShader.uniformMatrix4fv("joints", myUser.skeleton().skinningMatrices());
				_myWeightsShader.uniform1f("randomScale", myUser.targetRandom());
				_myMesh.draw(g);
				_myWeightsShader.end();
				g.popMatrix();
//			}else {
//				g.pushMatrix();
//				g.translate(i * _myXRes, 0);
//				g.popMatrix();
//			}
		}
		_myTargetTexture.endDraw();
//		_myCurrentData = _myTargetTexture.getData();
		
//		_myLeftPositions.update(theDeltaTime);
//		_myRightPositions.update(theDeltaTime);
	}
	
	private float[] randomPoint() {
		int myTriangleIndices = (int)CCMath.random(_myIndices.limit() / 3) * 3;
		_myIndices.position(myTriangleIndices);
		int myIndex0 = _myIndices.get();
		int myIndex1 = _myIndices.get();
		int myIndex2 = _myIndices.get();
		
		_myPositionBuffer.position(myIndex0 * 3);
		float myP0X = _myPositionBuffer.get();
		float myP0Y = _myPositionBuffer.get();
		float myP0Z = _myPositionBuffer.get();

		_myPositionBuffer.position(myIndex1 * 3);
		float myP1X = _myPositionBuffer.get();
		float myP1Y = _myPositionBuffer.get();
		float myP1Z = _myPositionBuffer.get();

		_myPositionBuffer.position(myIndex2 * 3);
		float myP2X = _myPositionBuffer.get();
		float myP2Y = _myPositionBuffer.get();
		float myP2Z = _myPositionBuffer.get();
		
		float myP01X = myP1X - myP0X;
		float myP01Y = myP1Y - myP0Y;
		float myP01Z = myP1Z - myP0Z;
		
		float myP02X = myP2X - myP0X;
		float myP02Y = myP2Y - myP0Y;
		float myP02Z = myP2Z - myP0Z;
	
		float myBlend1 = CCMath.random();
		float myBlend2 = CCMath.random();
		
		float[] myResult = new float[11];
				
		myResult[0] = myP0X + myP01X * myBlend1 + myP02X * myBlend2;
		myResult[1] = myP0Y + myP01Y * myBlend1 + myP02Y * myBlend2;
		myResult[2] = myP0Z + myP01Z * myBlend1 + myP02Z * myBlend2;
		
		_myWeightIndexBuffer.position(myIndex0 * 4);
		_myWeightBuffer.position(myIndex0 * 4);
		
		myResult[3] = _myWeightBuffer.get();
		myResult[4] = _myWeightBuffer.get();
		myResult[5] = _myWeightBuffer.get();
		myResult[6] = _myWeightBuffer.get();

		myResult[7] = _myWeightIndexBuffer.get();
		myResult[8] = _myWeightIndexBuffer.get();
		myResult[9] = _myWeightIndexBuffer.get();
		myResult[10] = _myWeightIndexBuffer.get();
		
		return myResult;
	}
	
	public void drawTargets(CCGraphics g) {
		g.color(255);
		g.image(_myTargetTexture,0,0);
	}
}
