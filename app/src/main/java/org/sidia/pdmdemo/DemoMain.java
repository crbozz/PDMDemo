package org.sidia.pdmdemo;

import android.util.Log;
import android.view.MotionEvent;

import com.samsungxr.ITouchEvents;
import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.mixedreality.IAnchorEvents;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IPlaneEvents;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.physics.SXRRigidBody;
import com.samsungxr.physics.SXRWorld;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DemoMain extends SXRMain {

    private SXRMixedReality mixedReality;

    private SXRNode rubiks;
    private SXRNode mug;

    private SXRRigidBody board;

    @Override
    public void onInit(SXRContext gvrContext) throws Throwable {

        loadObjects();

        mixedReality = new SXRMixedReality(getSXRContext().getMainScene());
        mixedReality.getEventReceiver().addListener(planeEvents);
        mixedReality.getEventReceiver().addListener(anchorEvents);
        mixedReality.resume();
    }

    private SXRPlane plane = null;

    private IPlaneEvents planeEvents = new IPlaneEvents() {
        @Override
        public void onStartPlaneDetection(IMixedReality iMixedReality) {
            DemoUtils.initInputManager(getSXRContext(), touchEvents, mixedReality.getScreenDepth());
        }

        @Override
        public void onStopPlaneDetection(IMixedReality iMixedReality) {

        }

        @Override
        public void onPlaneDetected(SXRPlane sxrPlane) {
            if (sxrPlane.getPlaneType() == SXRPlane.Type.VERTICAL) {
                return;
            }

            if (plane == null) {
                plane = sxrPlane;

                final SXRNode node = DemoUtils.createQuadPlane(getSXRContext());
                node.attachComponent(plane);
                getSXRContext().getMainScene().addNode(node);
            }
        }

        @Override
        public void onPlaneStateChange(SXRPlane sxrPlane, SXRTrackingState sxrTrackingState) {

        }

        @Override
        public void onPlaneMerging(SXRPlane sxrPlane, SXRPlane sxrPlane1) {

        }
    };

    private IAnchorEvents anchorEvents = new IAnchorEvents() {
        @Override
        public void onAnchorStateChange(SXRAnchor sxrAnchor, SXRTrackingState sxrTrackingState) {
        }
    };

    private float bottom = -1000f;
    @Override
    public void onStep() {
        if (rubiks.getTransform().getPositionY() < bottom) {
            DemoUtils.resetDynamicBody(rubiks, rubiksMat);
        }

        if (mug.getTransform().getPositionY() < bottom) {
            DemoUtils.resetDynamicBody(mug, mugMat);
        }
    }

    ITouchEvents touchEvents = new ITouchEvents() {
        @Override
        public void onEnter(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onExit(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onTouchStart(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onTouchEnd(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {
            if (rubiks.getParent() == null) {
                placeObjects();
                initPhysics();
                return;
            }

            String name = sxrNode.getName();
            if (!name.equals("board")) {
                Vector3f force = DemoUtils.calcForce(sxrNode, getSXRContext().getMainScene().getMainCameraRig());
                SXRRigidBody rb = (SXRRigidBody) sxrNode.getComponent(SXRRigidBody.getComponentType());
                rb.applyCentralForce(force.x, force.y, force.z);
            }
        }

        @Override
        public void onInside(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onMotionOutside(SXRPicker sxrPicker, MotionEvent motionEvent) {

        }
    };

    private void loadObjects() throws Throwable {
        rubiks = DemoUtils.loadSingleNode(getSXRContext(), "models/rubiks.fbx");
        rubiks.getTransform().setScale(10f, 10f, 10f);
        SXRCollider collider = new SXRMeshCollider(getSXRContext(), false);
        rubiks.attachComponent(collider);

        mug = DemoUtils.loadSingleNode(getSXRContext(), "models/mug.fbx");
        mug.getTransform().setScale(10f, 10f, 10f);
        SXRMeshCollider meshCollider2 = new SXRMeshCollider(getSXRContext(), false);
        mug.attachComponent(meshCollider2);
    }

    private SXRDrawFrameListener drawFrameListener = new SXRDrawFrameListener() {
        @Override
        public void onDrawFrame(float v) {
            Matrix4f mat = plane.getOwnerObject().getChildByIndex(0).getTransform().getModelMatrix4f();
            SXRNode boardNode = board.getOwnerObject();
            mat.m31(boardNode.getTransform().getPositionY());
            boardNode.getTransform().setModelMatrix(mat);
            boardNode.getTransform().setScaleZ(1f);
            board.reset(false);
        }
    };

    private void initPhysics() {
        SXRWorld world = new SXRWorld(getSXRContext());
        world.setGravity(0f, -200f, 0f);
        getSXRContext().getMainScene().getRoot().attachComponent(world);

        SXRRigidBody rb = new SXRRigidBody(getSXRContext(), 1f);
        rb.setCcdMotionThreshold(0.00001f);
        rb.setCcdSweptSphereRadius(5f);
        rb.setRestitution(0f);
        rubiks.attachComponent(rb);

        rb = new SXRRigidBody(getSXRContext(), 2f);
        rb.setCcdMotionThreshold(0.00001f);
        rb.setCcdSweptSphereRadius(5f);
        mug.attachComponent(rb);

//        SXRNode boardNode = new SXRCubeNode(getSXRContext(), true);
//        boardNode.setName("board");
//        SXRMaterial material = new SXRMaterial(getSXRContext(), SXRMaterial.SXRShaderType.Phong.ID);
//        material.setDiffuseColor(0f, 1f, 0f, 1f);
//        boardNode.getRenderData().setMaterial(material);

        SXRNode boardNode = new SXRNode(getSXRContext());

        Matrix4f mat = plane.getOwnerObject().getChildByIndex(0).getTransform().getModelMatrix4f();
        boardNode.getTransform().setModelMatrix(mat);
        boardNode.getTransform().setScaleZ(1f);

        SXRBoxCollider collider = new SXRBoxCollider(getSXRContext());
        collider.setHalfExtents(0.5f, 0.5f, 0.5f);

        boardNode.attachComponent(collider);

        board = new SXRRigidBody(getSXRContext(), 0f);
        board.setRestitution(0.5f);
        board.setCcdMotionThreshold(0.00001f);
        board.setCcdSweptSphereRadius(4f);
        boardNode.attachComponent(board);
        getSXRContext().getMainScene().addNode(boardNode);

        getSXRContext().registerDrawFrameListener(drawFrameListener);
    }

    private Matrix4f rubiksMat;
    private Matrix4f mugMat;
    private void placeObjects() {
        Matrix4f mat = plane.getTransform().getModelMatrix4f();
        float x = mat.m30();
        float y = mat.m31() + 20f;
        float z = mat.m32();

        rubiks.getTransform().setPosition(x + 10f, y, z + 10f);
        getSXRContext().getMainScene().addNode(rubiks);
        rubiksMat = rubiks.getTransform().getLocalModelMatrix4f();

        mug.getTransform().setPosition(x, y, z);
        getSXRContext().getMainScene().addNode(mug);
        mugMat = mug.getTransform().getLocalModelMatrix4f();
    }
}
