package org.sidia.pdmdemo;

import com.samsungxr.ITouchEvents;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRGazeCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.physics.SXRRigidBody;

import org.joml.Matrix4f;

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

    static void resetDynamicBody(SXRNode sxrNode, Matrix4f localMatrix) {
        SXRRigidBody rb = (SXRRigidBody)sxrNode.getComponent(SXRRigidBody.getComponentType());
        sxrNode.getTransform().setModelMatrix(localMatrix);
        rb.setLinearVelocity(0f, 0f, 0f);
        rb.setAngularVelocity(0f, 0f, 0f);
        rb.reset(false);
    }

}
