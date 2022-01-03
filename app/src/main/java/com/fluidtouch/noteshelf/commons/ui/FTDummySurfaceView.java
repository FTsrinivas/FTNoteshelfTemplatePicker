package com.fluidtouch.noteshelf.commons.ui;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.renderingengine.renderer.OpenGLES3Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by sreenu on 28/08/20.
 */
public class FTDummySurfaceView extends GLSurfaceView {
    public FTDummySurfaceView(Context context) {
        super(context);

        this.setEGLContextClientVersion(3);
        setRenderer(new Renderer() {
            private static final String TAG = "MyGLRenderer";

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                EGLDisplay display = EGL14.eglGetCurrentDisplay();
                EGLSurface surface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
                if (EGL14.eglSurfaceAttrib(display, surface, EGL14.EGL_RENDER_BUFFER, EGL14.EGL_SINGLE_BUFFER) == false) {
                    Log.d(TAG, "failed to enalbe single buffer");
                    return;
                }
                int currentMode = getRenderBufferType(display, surface);
                if (currentMode != EGL14.EGL_SINGLE_BUFFER) {
                    Log.d(TAG, "could not enable EGL_SINGLE_BUFFER");
                } else {
                    GLES30.glDisable(GLES30.GL_SCISSOR_TEST);
                    GLES30.glBindFramebuffer(OpenGLES3Renderer.FRAMEBUFFER_TARGET, 0);
                    GLES30.glClearColor(0.0f, 0, 0, 0.0f);
                    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
                    EGL14.eglSwapBuffers(display, surface);
                    Log.d(TAG, "Rama");
                    if (EGL14.eglSurfaceAttrib(display, surface, OpenGLES3Renderer.EGL_FRONT_BUFFER_AUTO_REFRESH_ANDROID, EGL14.EGL_TRUE) == false) {
                        Log.d(TAG, "failed to auto refresh screen");
                    }
                }
            }

            private int getRenderBufferType(EGLDisplay disp, EGLSurface surf) {
                int value[] = new int[4];
                EGL14.eglQuerySurface(disp, surf, EGL14.EGL_RENDER_BUFFER, value, -0); //EGL_RENDER_BUFFER, EGL_SINGLE_BUFFER
                return value[0];
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES30.glBindFramebuffer(OpenGLES3Renderer.FRAMEBUFFER_TARGET, 0);
                gl.glClearColor(0, 0, 0, 0);
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

                if (checkGlErroForFBR()) {
                    OpenGLES3Renderer.useFBRRendering = false;
                    FTApp.getPref().save(SystemPref.SUPPORTS_FBR, false);
                }
                FTApp.getPref().save(SystemPref.IS_FBR_SUPPORT_TESTING_DONE, true);
            }

            private Boolean checkGlErroForFBR() {
                return GLES20.glGetError() != GLES20.GL_NO_ERROR;
            }
        });

    }
}
