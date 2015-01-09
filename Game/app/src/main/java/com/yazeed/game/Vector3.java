package com.yazeed.game;

import java.util.Vector;

/**
 * Created by Yazeed on 1/5/2015.
 */
public class Vector3 {
    float[] xyz;

    public Vector3(){
        xyz = new float[3];
    }

    public Vector3(float x, float y, float z){
        xyz = new float[3];
        xyz[0] = x;
        xyz[1] = y;
        xyz[2] = z;
    }

    public static Vector3 origin(){
        return new Vector3(0,0,0);
    }

    public float[] asFloatArray(){
        return xyz;
    }

    public float get(int position){
        assert(position>0 && position <3);

        return xyz[position];
    }

    public void set(int position, float value){
        assert(position>=0 && position<3);

        xyz[position] = value;
    }

    public void add(Vector3 aVector){
        xyz[0] += aVector.get(0);
        xyz[1] += aVector.get(1);
        xyz[2] += aVector.get(2);
    }

    public void subtract(Vector3 aVector){
        xyz[0] -= aVector.get(0);
        xyz[1] -= aVector.get(1);
        xyz[2] -= aVector.get(2);
    }

    public float mulitplyByVector(Vector3 aVector){
        return xyz[0]*aVector.get(0) + xyz[1]*aVector.get(1) + xyz[2]*aVector.get(2);
    }

    public void multiplyByScalar(float scalar){
        xyz[0] *= scalar;
        xyz[1] *= scalar;
        xyz[2] *= scalar;
    }

    public void divide(float denominator){
        assert(denominator!=0);

        xyz[0] /= denominator;
        xyz[1] /= denominator;
        xyz[2] /= denominator;
    }

    public void normalize(){
        float length = length();

        divide(length);
    }

    public float length(){
        return (float)Math.sqrt(getNorm());
    }

    public Vector3 rotateAround(Vector3 origin, Vector3 axis, float angle){
        Vector3 rotAxis = subtractVectors(axis, origin);
        rotAxis.normalize();

        Vector3 xAxis = new Vector3(1.0f, 0.0f, 0.0f);
        Vector3 yAxis = new Vector3(0.0f, 1.0f, 0.0f);

        if(Math.abs(xAxis.mulitplyByVector(rotAxis)) > Math.abs(yAxis.mulitplyByVector(rotAxis))){
            xAxis = yAxis;
        }

        Vector3 b = crossVectors(xAxis, rotAxis);
        b.normalize();
        Vector3 c = crossVectors(rotAxis, b);
        c.normalize();

        xyz[0] -= origin.get(0);
        xyz[1] -= origin.get(1);
        xyz[2] -= origin.get(2);

        Matrix3 newCoordinates = new Matrix3(rotAxis, b, c);

        Vector3 pointInNewCoordinates = newCoordinates.multiplyByVector(this);

        Matrix3 rotM = new Matrix3(new Vector3(1f,0f,0f), new Vector3(0f, (float)Math.cos(TORAD(angle)), -1*(float)Math.sin(TORAD(angle))), new Vector3(0f, (float)Math.sin(TORAD(angle)), (float)Math.cos(TORAD(angle))));

       pointInNewCoordinates = rotM.multiplyByVector(pointInNewCoordinates);

        Matrix3 oldCoord = new Matrix3(newCoordinates.multiplyByVector(new Vector3(1f,0f,0f)), newCoordinates.multiplyByVector(new Vector3(0,1,0)),
                newCoordinates.multiplyByVector(new Vector3(0,0,1)));

        pointInNewCoordinates = oldCoord.multiplyByVector(pointInNewCoordinates);

        pointInNewCoordinates.add(origin);

        return pointInNewCoordinates;
    }

    public static Vector3 subtractVectors(Vector3 left, Vector3 right){
        Vector3 result = left;
        result.subtract(right);
        return result;
    }

    public static Vector3 addVectors(Vector3 left, Vector3 right){
        Vector3 result = new Vector3(left.get(0), left.get(1), left.get(2));
        result.add(right);
        return result;
    }

    public static Vector3 crossVectors(Vector3 left, Vector3 right){
        float i = left.get(1)*right.get(2)-left.get(2)*right.get(1);
        float j = -(left.get(0)*right.get(2)-left.get(2)*right.get(0));
        float k = left.get(0)*right.get(1)-left.get(1)*right.get(0);
        return new Vector3(i, j, k);
    }

    public float TORAD(float degree){
        return  (float) (degree * 3.14159265 / (float)180);
    }

    public float getNorm(){
        return xyz[0]*xyz[0] + xyz[1]*xyz[1] + xyz[2]*xyz[2];
    }

    public String toString(){
        return "V3 [" + xyz[0] + ", " + xyz[1] + ", " + xyz[2] + "]";
    }

}
