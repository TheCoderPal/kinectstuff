package de.artcom.deutschebank.brandscape.infolandschaft.visuals.connectivity.demo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.control.CCControl;
import cc.creativecomputing.geometry.CCTriangleIntersector.CCTriangleIntersectionData;
import cc.creativecomputing.geometry.CCTriangleMesh;
import cc.creativecomputing.geometry.CCTriangleMesh.CCTriangleMeshIntersectionData;
import cc.creativecomputing.graphics.CCAbstractGraphics.CCBlendMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCGraphics.CCPolygonMode;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCVBOMesh;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCMatrix4f;
import cc.creativecomputing.math.CCRay3f;
import cc.creativecomputing.math.CCTriangle3f;
import cc.creativecomputing.math.CCVecMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.math.CCVector4f;
import cc.creativecomputing.math.spline.CCCatmulRomSpline;
import cc.creativecomputing.math.spline.CCLinearSpline;
import cc.creativecomputing.math.util.CCArcball;
import cc.creativecomputing.model.collada.CCColladaSkeletonMesh;
import cc.creativecomputing.model.skeleton.CCSkeletonJoint;
import cc.creativecomputing.nio.CCFileChannel;
import cc.creativecomputing.nio.CCFileChannel.CCFileMode;

public class ILLoadAvatarPathsDemo extends CCApp {
	
	
	
	@CCControl(name = "axis length", min = 0, max = 100)
	private float _cAxisLength = 0;
	
	@CCControl(name = "mesh alpha", min = 0, max = 1)
	private float _cMeshAlpha = 0;
	
	@CCControl(name = "curves alpha", min = 0, max = 1)
	private float _cCurvesAlpha = 0;
	
	private CCArcball _myArcball;
	
	private CCColladaSkeletonMesh _mySkeletonMesh;
	
	@CCControl(name = "time", min = 0, max = 2)
	private float _cTime = 0;
	
	
	
	
	private CCVBOMesh _myPathMesh;

	public void setup() {
		addControls("app", "app", this);
		_myArcball = new CCArcball(this);
		
		_mySkeletonMesh = new CCColladaSkeletonMesh(
			"120411_humanoid_01_bakeTR.dae",
			"humanoid-lib",
			"bvh_import/Hips"
		);

		_myPathMesh = buildMesh();
	}
	
	private int _myEquals = 0;
	
	private int _myAll = 0;
	
	public CCVBOMesh buildMesh() {
		
		CCFileChannel myFileChannel = new CCFileChannel("avatar_curves.bla", CCFileMode.RW);
		int myNumberOfIndices = myFileChannel.readInt();

		IntBuffer myIndices = myFileChannel.readInts(myNumberOfIndices);

		myIndices.rewind();
		
		int myNumberOfVertices = myFileChannel.readInt();
		FloatBuffer myPositions = myFileChannel.readFloats(myNumberOfVertices * 3);
		myPositions.rewind();
		
		FloatBuffer myWeights = myFileChannel.readFloats(myNumberOfVertices * 4);
		FloatBuffer myWeightIndices = myFileChannel.readFloats(myNumberOfVertices * 4);
		
		myPositions.rewind();
		myIndices.rewind();
		myWeights.rewind();
		myWeightIndices.rewind();
		
		CCVBOMesh myMesh = new CCVBOMesh(CCDrawMode.LINES);
		myMesh.vertices(myPositions);
		myMesh.indices(myIndices);
		myMesh.textureCoords(1, myWeights, 4);
		myMesh.textureCoords(2, myWeightIndices, 4); 
		return myMesh;
	}
	
	@Override
	public void update(float theDeltaTime) {
		_mySkeletonMesh.skeletonController().time(_cTime);
	}

	public void draw() {
		g.clearColor(0,0,0);
		g.clear();

		g.noDepthTest();
		g.blend(CCBlendMode.ADD);
		_myArcball.draw(g);

		g.color(125, 0, 0);
		g.strokeWeight(1);
		g.line(0, 0, 0, width, 0, 0);
		g.color(0, 125, 0);
		g.line(0, 0, 0, 0, 0, -width);
		g.color(0, 0, 125);
		g.line(0, 0, 0, 0, -height, 0);
		
		g.color(255);
		g.polygonMode(CCPolygonMode.LINE);
		_mySkeletonMesh.draw(g, _myPathMesh);
		g.clearDepthBuffer();
		g.color(255,0,0);
		_mySkeletonMesh.skeleton().draw(g);
		
		g.color(1f,_cMeshAlpha);
		_mySkeletonMesh.draw(g);

		g.polygonMode(CCPolygonMode.FILL);
		
		g.color(1f,_cCurvesAlpha);
//		for(CCSkeletonCurve myCurve:_myCurves) {
//			myCurve.draw(g);
//		}
//		_myPathMesh.draw(g);
		g.blend();
//		_mySkeletonMesh.skeleton().drawOrientations(g, _cAxisLength);
	}

	public static void main(String[] args) {
		CCApplicationManager myManager = new CCApplicationManager(ILLoadAvatarPathsDemo.class);
		myManager.settings().size(1500, 900);
		myManager.settings().antialiasing(8);
		myManager.start();
	}
}
