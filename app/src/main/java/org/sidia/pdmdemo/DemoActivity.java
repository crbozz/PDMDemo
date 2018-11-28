package org.sidia.pdmdemo;

import android.os.Bundle;

import com.samsungxr.SXRActivity;

public class DemoActivity extends SXRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Set DemoMain Scene
         * It will be displayed when app starts
         */
        DemoMain demoMain = new DemoMain();
        setMain(demoMain, "sxr.xml");
    }

    /*
    private final class DemoMain extends SXRMain {

        @Override
        public void onInit(SXRContext sxrContext) throws Throwable {

            //Load texture
            SXRTexture texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.__default_splash_screen__));

            //Create a rectangle with the texture we just loaded
            SXRNode quad = new SXRNode(sxrContext, 4, 2, texture);
            quad.getTransform().setPosition(0, 0, -5);

            //Add rectangle to the scene
            sxrContext.getMainScene().addNode(quad);

            SXRMaterial mat = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.Phong.ID);
            mat.setDiffuseColor(1f, 0f, 0f, 1f);
            SXRNode node = new SXRSphereNode(sxrContext, true, 1f);
            node.getRenderData().setMaterial(mat);
            node.getTransform().setPosition(0f, 0f, -3f);

            sxrContext.getMainScene().getMainCameraRig().addChildObject(node);
        }

        @Override
        public SplashMode getSplashMode() {
            return SplashMode.NONE;
        }

        @Override
        public void onStep() {
            //Add update logic here
        }
    }
    */
}
