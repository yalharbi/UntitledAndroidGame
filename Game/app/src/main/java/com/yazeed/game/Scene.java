package com.yazeed.game;



import java.util.ArrayList;


/**
 * Created by Yazeed on 1/5/2015.
 */
public class Scene {
    int numberOfObjects;
    int numberOfModels3D;

    ArrayList<Model3D> sceneModels;

    public Scene(){
        numberOfModels3D = 0;
        numberOfObjects = 0;
        sceneModels = new ArrayList<Model3D>();
    }

    public void addModel3D(Model3D model){
        sceneModels.add(model);
        numberOfModels3D++;
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
