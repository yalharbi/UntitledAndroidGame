package com.yazeed.game;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Yazeed on 1/9/2015.
 */
public class Model2D {
    int texID;
    String name;
    Vector3[] vertices;
    Vector3[] boundingBox;

    int numberOfVertices, numberOfTriangles;
    int[] triangles;
    float[] uvs;
    FloatBuffer vertexBuffer, textureBuffer;
    IntBuffer indexBuffer;
    String modelName;
    boolean textured;
    Bitmap image;

    public Model2D(String name, InputStream imageStream){
        this.name = name;
        numberOfTriangles = 2*3;
        numberOfVertices = 4*3;
        triangles = new int[]{0, 3, 1, 0, 2, 3};
        uvs = new float[]{1,1,0,1,1,0,0,0};
        vertices = new Vector3[4];
        vertexBuffer = null;
        indexBuffer = null;
        boundingBox = new Vector3[2];
        textureBuffer = null;
        setInitialVertices();
        computeBoundingBox();
        try{
            image = BitmapFactory.decodeStream(imageStream);
        } finally {
            try{
                imageStream.close();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }



    public void setInitialVertices(){
        vertices[1] = new Vector3(-0.5f, -0.5f, 0);
        vertices[2] = new Vector3(0.5f, 0.5f, 0);
        vertices[3] = new Vector3(-0.5f, 0.5f, 0);
        vertices[0] = new Vector3(0.5f, -0.5f, 0);
    }

    public FloatBuffer getVertexBuffer(){
        if(vertexBuffer == null)
            createVertexBuffer();

        return vertexBuffer;
    }

    public IntBuffer getIndexBuffer(){
        if(indexBuffer == null)
            createIndexBuffer();

        return indexBuffer;
    }

    public FloatBuffer getTextureBuffer(){
        if(textureBuffer == null)
            createTexturexBuffer();

        return textureBuffer;
    }

    public void createIndexBuffer(){
        indexBuffer = ByteBuffer.allocateDirect(numberOfTriangles * 4)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indexBuffer.put(triangles).position(0);
    }

    public void createTexturexBuffer(){
        textureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureBuffer.put(uvs).position(0);
    }

    public void createVertexBuffer(){
        vertexBuffer = ByteBuffer.allocateDirect(numberOfVertices * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        for(int i=0;i<numberOfVertices/3;i++)
            vertexBuffer.put(vertices[i].asFloatArray());
    }

    public void updateVertexBuffer(){
        if(vertexBuffer==null)
            createVertexBuffer();
        else {
            float[] vectorToFloatArray = new float[numberOfVertices];
            for (int i = 0; i < numberOfVertices / 3; i++) {
                vectorToFloatArray[i*3] = vertices[i].get(0);
                vectorToFloatArray[i*3+1] = vertices[i].get(1);
                vectorToFloatArray[i*3+2] = vertices[i].get(2);
            }
            vertexBuffer.clear();
            vertexBuffer.put(vectorToFloatArray).position(0);
            vectorToFloatArray = null;
        }
    }

    public void scale(float factor){
        for(int i=0;i<numberOfVertices/3;i++)
            vertices[i].multiplyByScalar(factor);
        updateVertexBuffer();
        computeBoundingBox();
    }

    public void translate(Vector3 translation){
        for(int i=0;i<numberOfVertices/3;i++)
            vertices[i].add(translation);
        updateVertexBuffer();
        computeBoundingBox();
    }

    public void rotate(Vector3 rotVector, float angle){
        Vector3 oldCenter = getCenter();
        placeAt(Vector3.origin());
        for(int i=0;i<numberOfVertices/3;i++)
            vertices[i] = vertices[i].rotateAround(Vector3.origin(), rotVector, angle);

        updateVertexBuffer();
        computeBoundingBox();

    }

    public void print(){
        for(int i = 0; i<numberOfVertices; i=i+3)
            System.out.println("Vertex: " + vertices[i] + " " + vertices[i+1] + vertices[i+2]);
        for(int i = 0; i<numberOfTriangles; i=i+3)
            System.out.println("Vertex: " + triangles[i] + " " + triangles[i+1] + triangles[i+2]);
    }

    public void computeBoundingBox(){
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

        for(int i=0;i<numberOfVertices/3;i++){
            Vector3 vertex = vertices[i];

            if(vertex.get(0) < minX)    minX = vertex.get(0);
            if(vertex.get(1) < minY)    minY = vertex.get(1);
            if(vertex.get(2) < minZ)    minZ = vertex.get(2);

            if(vertex.get(0) > maxX)    maxX = vertex.get(0);
            if(vertex.get(1) > maxY)    maxY = vertex.get(1);
            if(vertex.get(2) > maxZ)    maxZ = vertex.get(2);
        }

        boundingBox[0] = new Vector3(minX, minY, minZ);
        boundingBox[1] = new Vector3(maxX, maxY, maxZ);
    }

    public Vector3 getCenter(){
        Vector3 center = Vector3.addVectors(boundingBox[0], boundingBox[1]);
        center.divide(2f);
        return center;
    }

    public void placeAt(Vector3 newPosition){
        Vector3 oldCenter = getCenter();
        Vector3 amount = newPosition;

        amount.subtract(oldCenter);
        translate(amount);

        updateVertexBuffer();
        computeBoundingBox();
    }

    public void scaleToNewDiagonal(float newDiagonal){
        Vector3 max = boundingBox[1];
        max.subtract(boundingBox[0]);
        float oldDiagonal = max.length();
        Vector3 oldCenter = getCenter();
        float scaleFactor = newDiagonal/oldDiagonal;
        placeAt(new Vector3(0.0f,0.0f,0.0f));
        scale(scaleFactor);
        placeAt(oldCenter);
        computeBoundingBox();
    }

    @Override
    public boolean equals(Object o) {
        return modelName.equals(((Model3D)o).modelName);
    }
}
