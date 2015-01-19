package com.yazeed.game;



import java.nio.FloatBuffer;
import java.util.ArrayList;


/**
 * Created by Yazeed on 1/5/2015.
 */

class Light{
    public Vector3 position;
    public float intensity;

    public Light(){
        position = new Vector3(0,0,0);
        intensity = 0;
    }

    public Light(Vector3 position, float intensity){
        this.position = position;
        this.intensity = intensity;
    }
}

public class Scene {
    int numberOfObjects;
    int numberOfModels3D;
    int numberOfModels2D;
    int numberOfLights;

    ArrayList<Model3D> sceneModels;
    ArrayList<Model2D> models2D;
    Light[] lights;

    public Scene(){
        numberOfModels3D = 0;
        numberOfModels2D = 0;
        numberOfObjects = 0;
        numberOfLights = 0;
        sceneModels = new ArrayList<Model3D>();
        models2D = new ArrayList<Model2D>();
        lights = new Light[5];
    }

    public void addModel3D(Model3D model){
        sceneModels.add(model);
        numberOfModels3D++;
    }

    public void addModel2D(Model2D model){
        models2D.add(model);
        numberOfModels2D++;
    }

    public void addLight(Vector3 position, float intensity){
        if(numberOfLights==4){
            return;
        }
        lights[numberOfLights++] = new Light(position, intensity);
    }

    public void deleteModel3D(Model3D modelToDelete){
        /*for(int i=0;i<numberOfModels3D;i++){
            if(sceneModels.get(i).modelName.equals(modelNameDelete)){
                sceneModels.remove(i);
                return;
            }
        }*/
        sceneModels.remove(modelToDelete);
    }




}
