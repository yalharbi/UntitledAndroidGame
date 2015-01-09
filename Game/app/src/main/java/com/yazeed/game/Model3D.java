package com.yazeed.game;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by Yazeed on 1/3/2015.
 */
public class Model3D {
    Vector3[] vertices;
    Vector3[] boundingBox;

    int numberOfVertices, numberOfTriangles;
    int[] triangles;
    FloatBuffer vertexBuffer;
    IntBuffer indexBuffer;
    String modelName;

    public Model3D(String name, InputStream inputStream) throws IOException{
        modelName = name;
        vertexBuffer = null;
        indexBuffer = null;
        boundingBox = new Vector3[2];
        readFromFile(inputStream);
        computeBoundingBox();
    }

    public void readFromFile(InputStream is) throws IOException{

        BufferedReader fileBuffered = new BufferedReader(new InputStreamReader(is));

        ArrayList<Float> verts = new ArrayList<Float>();
        ArrayList<Float> norms = new ArrayList<Float>();
        ArrayList<Float> cols = new ArrayList<Float>();
        ArrayList<Integer> tris = new ArrayList<Integer>();

        Scanner fileScanner = new Scanner(fileBuffered);


        while (fileScanner.hasNextLine() ) {
            String line = fileScanner.nextLine();
            Scanner lineScanner = new Scanner(line);
            lineScanner.next();
            if(line.charAt(0) == 'v' && line.charAt(1) == ' ') {
                verts.add(lineScanner.nextFloat());
                verts.add(lineScanner.nextFloat());
                verts.add(lineScanner.nextFloat());
            }
            else if(line.charAt(1) == 'n') {
                norms.add(lineScanner.nextFloat());
                norms.add(lineScanner.nextFloat());
                norms.add(lineScanner.nextFloat());
            }
            else if(line.charAt(0) == 'f'){
                int first = lineScanner.nextInt();
                int second = lineScanner.nextInt();
                int third = lineScanner.nextInt();

                tris.add(first-1);
                tris.add(second-1);
                tris.add(third-1);
            }

        }
        triangles = new int[tris.size()];
        vertices = new Vector3[verts.size()/3];

        numberOfVertices = verts.size();

        for(int j = 0; j<verts.size(); j=j+3){
            vertices[j/3] = new Vector3(verts.get(j), verts.get(j+1), verts.get(j+2));
        }

        int i=0;
        for(Integer in : tris){
            triangles[i++] = in;
        }
        numberOfTriangles = i;

        verts = null;
        norms = null;
        cols = null;
        tris = null;
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

    public void createVertexBuffer(){
        vertexBuffer = ByteBuffer.allocateDirect(numberOfVertices * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        for(int i=0;i<numberOfVertices/3;i++)
            vertexBuffer.put(vertices[i].asFloatArray());
    }

    public void createIndexBuffer(){
        indexBuffer = ByteBuffer.allocateDirect(numberOfTriangles * 4)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indexBuffer.put(triangles).position(0);
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
