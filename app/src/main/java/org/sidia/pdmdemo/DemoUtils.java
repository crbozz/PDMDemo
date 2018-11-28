package org.sidia.pdmdemo;

import com.samsungxr.ITouchEvents;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRTransform;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRGazeCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.physics.SXRRigidBody;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.EnumSet;

public class DemoUtils {

    static SXRNode loadSingleNode(SXRContext context, String filePath) throws Throwable {
        SXRNode model = context.getAssetLoader().loadModel(filePath);
        SXRNode sxrNode = model;
        while (true) {
            if (sxrNode.getRenderData() != null) {
                SXRNode parent = sxrNode.getParent();
                if (parent != null) {
                    org.joml.Matrix4f mat = sxrNode.getTransform().getModelMatrix4f();
                    parent.removeChildObject(sxrNode);
                    sxrNode.getTransform().setModelMatrix(mat);
                }
                return sxrNode;
            }

            if (sxrNode.getChildrenCount() == 0) {
                break;
            }

            sxrNode = sxrNode.getChildByIndex(0);
        }

        return null;
    }

    static void initInputManager(SXRContext context, final ITouchEvents touchEvents, final float screenDepth) {
        SXRInputManager inputManager = context.getInputManager();
        final int cursorDepth = 5;
        final EnumSet<SXRPicker.EventOptions> eventOptions = EnumSet.of(
                SXRPicker.EventOptions.SEND_TOUCH_EVENTS,
                SXRPicker.EventOptions.SEND_TO_LISTENERS,
                SXRPicker.EventOptions.SEND_TO_HIT_OBJECT);

        inputManager.selectController(new SXRInputManager.ICursorControllerSelectListener() {
            @Override
            public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(touchEvents);
                }
                newController.addPickEventListener(touchEvents);
                newController.setCursorDepth(cursorDepth);
                newController.setCursorControl(SXRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setPickClosest(false);
                newController.getPicker().setEventOptions(eventOptions);
                if (newController instanceof SXRGazeCursorController) {
                    ((SXRGazeCursorController) newController).setTouchScreenDepth(screenDepth);
                    // Don't show any cursor
                    newController.setCursor(null);
                }

            }
        });
    }

    static void resetDynamicBody(SXRNode sxrNode, Matrix4f localMatrix) {
        sxrNode.getTransform().setModelMatrix(localMatrix);
        SXRRigidBody rb = (SXRRigidBody)sxrNode.getComponent(SXRRigidBody.getComponentType());
        rb.setLinearVelocity(0f, 0f, 0f);
        rb.setAngularVelocity(0f, 0f, 0f);
        rb.reset(false);
    }

    static SXRNode createQuadPlane(SXRContext context)
    {
        SXRNode node = new SXRNode(context);
        SXRMesh mesh = SXRMesh.createQuad(context, "float3 a_position", 1.0f, 1.0f);
        SXRMaterial mat = new SXRMaterial(context, SXRMaterial.SXRShaderType.Phong.ID);
        SXRNode polygonObject = new SXRNode(context, mesh, mat);

        node.setName("Plane");
        polygonObject.setName("PlaneGeometry");
        mat.setDiffuseColor(0.5f, 0.5f, 0.5f, 0.5f);
        polygonObject.getRenderData().disableLight();
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);
        node.addChildObject(polygonObject);

        return node;
    }

    static Vector3f calcForce(SXRNode target, SXRCameraRig cameraRig) {
        Matrix4f m_ori = cameraRig.getTransform().getModelMatrix4f();
        Vector3f v_ori = new Vector3f();
        m_ori.getTranslation(v_ori);

        Matrix4f m_tgt = target.getTransform().getModelMatrix4f();
        Vector3f v_tgt = new Vector3f();
        m_tgt.getTranslation(v_tgt);

        v_tgt.sub(v_ori);
        v_tgt.y = 0;
        v_tgt.normalize();
        v_tgt.x *= 10000f;
        v_tgt.y = 1000f;
        v_tgt.z *= 10000f;

        return v_tgt;
    }

}
