package com.yazeed.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.opengl.GLES20;
import android.os.SystemClock;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.os.Bundle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
public class FirstRenderer implements GLSurfaceView.Renderer
{
    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] mProjectionMatrix = new float[16];

    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] mMVPMatrix = new float[16];



    FloatBuffer modelVerts;
    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;

    /** This will be used to pass in model color information. */
    private int mColorHandle;

    private int mNormalHandle;

    private int mLightPosHandle;

    private int mTexHandle;

    private int mTexture;
    
    private int regularShaderProgram;
    
    private int textureShaderProgram;

    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    /** How many elements per vertex. */
    private final int mStrideBytes = 7 * mBytesPerFloat;

    /** Offset of the position data. */
    private final int mPositionOffset = 0;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** Offset of the color data. */
    private final int mColorOffset = 3;

    Model3D m;
    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;

    private Scene scene;
    /**
     * Initialize the model data.
     */
    public FirstRenderer(Context context)
    {
        scene = new Scene();
        // Define points for equilateral triangles.
        m = null;
        Model2D orange = null;
        try {
            m = new Model3D("test", context.getResources().openRawResource(R.raw.file));
            orange = new Model2D("orange", context.getResources().openRawResource((R.raw.orange)));
        }catch (IOException ioe){}

        m.scaleToNewDiagonal(1.5f);
        scene.addModel3D(m);
        scene.addModel2D(orange);
        scene.addLight(new Vector3(1,1,1), 1);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 2.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        createRegularShader();
        //createTextureShader();

        // Tell OpenGL to use this program when rendering.

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {

        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -2.0f;
        final float top = 2.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearDepthf(1.0f);
        GLES20.glDepthFunc( GLES20.GL_LEQUAL );
        GLES20.glDepthMask( true );
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable( GLES20.GL_CULL_FACE );
        GLES20.glCullFace(GLES20.GL_BACK);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        Matrix.setIdentityM(mModelMatrix, 0);
        /*Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);*/
;
        drawScene();
    }

    /**
     * Draws a triangle from the given vertex data.
     *
     *
     */
    private void drawScene()
    {
        // Pass in the position information
       // aTriangleBuffer.position(mPositionOffset);
        for (Model3D model : scene.sceneModels) {
            //model.translate(new Vector3(0.01f, 0, -0.01f));
            GLES20.glUseProgram(regularShaderProgram);
            model.rotate(new Vector3(0,0,1), 0.1f);
            FloatBuffer aTriangleBuffer = model.getVertexBuffer();
            aTriangleBuffer.position(0);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    0, aTriangleBuffer);

            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Pass in the color information
            aTriangleBuffer.position(0);
            GLES20.glVertexAttribPointer(mColorHandle, 3, GLES20.GL_FLOAT, false,
                    0, aTriangleBuffer);

            GLES20.glEnableVertexAttribArray(mColorHandle);

            //pass in normal
            FloatBuffer normalBuffer = model.getNormalBuffer();
            normalBuffer.position(0);
            GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false,
                    0, normalBuffer);

            GLES20.glEnableVertexAttribArray(mNormalHandle);

            // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
            // (which currently contains model * view).
            Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

            // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
            // (which now contains model * view * projection).
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniform3f(mLightPosHandle, 1.0f,1.0f,1.0f);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, m.numberOfTriangles, GLES20.GL_UNSIGNED_INT, model.getIndexBuffer());
            GLES20.glDisableVertexAttribArray(mPositionHandle);
            GLES20.glDisableVertexAttribArray(mColorHandle);
            GLES20.glDisableVertexAttribArray(mNormalHandle);

            //model.print();
        }
        /*for(Model2D model : scene.models2D){
            GLES20.glUseProgram(textureShaderProgram);
            load(model );
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, model.texID);
            GLES20.glUniform1i(mTexture, 0);

            FloatBuffer aTriangleBuffer = model.getVertexBuffer();

            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    0, aTriangleBuffer);

            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Pass in the color information
            aTriangleBuffer.position(0);
            GLES20.glVertexAttribPointer(mColorHandle, 3, GLES20.GL_FLOAT, false,
                    0, aTriangleBuffer);

            GLES20.glEnableVertexAttribArray(mColorHandle);

            GLES20.glVertexAttribPointer(mTexHandle, 2, GLES20.GL_FLOAT, false, 8,model.getTextureBuffer());
            GLES20.glEnableVertexAttribArray(mTexHandle);

            // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
            // (which currently contains model * view).
            Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

            // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
            // (which now contains model * view * projection).
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

            //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 29);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, model.getIndexBuffer());
        }*/
    }
    
    public void createRegularShader(){
        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
                        + "uniform vec3 lightPos;       \n"       // added light position

                        + "attribute vec3 a_Position;     \n"		// Per-vertex position information we will pass in.
                        + "attribute vec3 a_Color;        \n"		// Per-vertex color information we will pass in.
                        + "attribute vec3 a_Normal;       \n"       // added normals



                        + "varying vec3 v_Color;          \n"		// This will be passed into the fragment shader.
                        + "varying vec3 v_Normal;         \n"       // added normals

                        + "void main()                    \n"		// The entry point for our vertex shader.
                        + "{                              \n"
                        + "   vec3 lightDir = a_Position - lightPos;         \n"
                        + "   lightDir = normalize(lightDir);                              \n"
                        + "   vec3 norm = a_Normal;                              \n"
                        + "   float intensity = max(dot(norm, lightDir), 0.0);                             \n"
                        + "   v_Color = intensity * vec3(1,0,0) + 0.2*vec3(1,0,0);       \n"		// Pass the color through to the fragment shader.
                        + "   v_Normal = a_Normal;   \n"
                        + "   vec4 pos = vec4(a_Position.rgb,1);   \n"
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
                        + "               * pos;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        final String fragmentShader =
                "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec3 v_Color;          \n"		// This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "varying vec3 v_Normal;          \n"		//

                        + "void main()                    \n"		// The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = vec4(v_Color, 1);     \n"		// Pass the color directly through the pipeline.
                        + "}                              \n";


        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0)
        {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0)
        {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Create a program object and store the handle to it.
       regularShaderProgram = GLES20.glCreateProgram();

        if (regularShaderProgram != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(regularShaderProgram, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(regularShaderProgram, fragmentShaderHandle);


            // Bind attributes
            GLES20.glBindAttribLocation(regularShaderProgram, 0, "a_Position");
            GLES20.glBindAttribLocation(regularShaderProgram, 1, "a_Color");
            GLES20.glBindAttribLocation(regularShaderProgram, 2, "a_Normal");
            GLES20.glBindAttribLocation(regularShaderProgram, 3, "a_TexCoord");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(regularShaderProgram);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(regularShaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                GLES20.glDeleteProgram(regularShaderProgram);
                regularShaderProgram = 0;
            }
        }

        if (regularShaderProgram == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(regularShaderProgram, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(regularShaderProgram, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(regularShaderProgram, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(regularShaderProgram, "a_Normal");
        mTexHandle = GLES20.glGetAttribLocation(regularShaderProgram, "a_TexCoord");
        mLightPosHandle = GLES20.glGetUniformLocation(regularShaderProgram, "lightPos");
    }
    
    public void createTextureShader(){
        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.

                        + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.
                        + "attribute vec4 a_Normal;       \n"       // added normals
                        + "attribute vec2 a_TexCoord;       \n"       // added textures

                        + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
                        + "varying vec4 v_Normal;         \n"       // added normals
                        + "varying vec2 v_TexCoord;         \n"       // added textures

                        + "void main()                    \n"		// The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"		// Pass the color through to the fragment shader.
                        + "   v_Normal = a_Normal;          \n"
                        + "   v_TexCoord = a_TexCoord;          \n"
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        final String fragmentShader =
                "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the
                        + "varying vec2 v_TexCoord;          \n"
                        + "uniform sampler2D s_texture;     \n"
                        // triangle per fragment.
                        + "void main()                    \n"		// The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = texture2D(s_texture, v_TexCoord);     \n"		// Pass the color directly through the pipeline.
                        + "}                              \n";


        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0)
        {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0)
        {
            throw new RuntimeException("Error creating fragment shader.");
        }

       
        // Create a program object and store the handle to it.
        textureShaderProgram = GLES20.glCreateProgram();

        if (textureShaderProgram != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(textureShaderProgram, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(textureShaderProgram, fragmentShaderHandle);


            // Bind attributes
            GLES20.glBindAttribLocation(textureShaderProgram, 0, "a_Position");
            GLES20.glBindAttribLocation(textureShaderProgram, 1, "a_Color");
            GLES20.glBindAttribLocation(textureShaderProgram, 2, "a_Normal");
            GLES20.glBindAttribLocation(textureShaderProgram, 3, "a_TexCoord");
            GLES20.glBindAttribLocation(textureShaderProgram, 4, "s_texture");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(textureShaderProgram);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(textureShaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                GLES20.glDeleteProgram(textureShaderProgram);
                textureShaderProgram = 0;
            }
        }

        if (textureShaderProgram == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(textureShaderProgram, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(textureShaderProgram, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(textureShaderProgram, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(textureShaderProgram, "a_Normal");
        mTexHandle = GLES20.glGetAttribLocation(textureShaderProgram, "a_TexCoord");
        mTexture = GLES20.glGetAttribLocation(textureShaderProgram, "s_texture");
    }

    public void load(Model2D model){
        int textures[] = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        model.texID = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, model.texID);

        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, model.image, 0);
    }

}