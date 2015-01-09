package com.yazeed.game;


/**
 * Created by Yazeed on 1/6/2015.
 */
public class Matrix3 {
    Vector3[] rows;
    final int NUM_ROWS = 3;
    final int NUM_COLUMNS = 3;

    public Matrix3(){
        rows = new Vector3[3];
    }

    public Matrix3(Vector3 firstRow, Vector3 secondRow, Vector3 thirdRow){
        rows = new Vector3[3];
        rows[0] = firstRow;
        rows[1] = secondRow;
        rows[2] = thirdRow;
    }

    public float getElement(int rowPosition, int columnPosition){
        assert(rowPosition>=0 && rowPosition<NUM_ROWS && columnPosition>=0 && columnPosition<NUM_COLUMNS);

        return rows[rowPosition].get(columnPosition);
    }

    public void setElement(int rowPosition, int columnPosition, float value){
        assert(rowPosition>=0 && rowPosition<NUM_ROWS && columnPosition>=0 && columnPosition<NUM_COLUMNS);

        rows[rowPosition].set(columnPosition, value);
    }

    public void setRow(int rowIndex, Vector3 newRow){
        assert(rowIndex>=0 && rowIndex < NUM_ROWS);

        rows[rowIndex] = newRow;
    }

    public Vector3 getRow(int rowIndex){
        assert(rowIndex>=0 && rowIndex < NUM_ROWS);

        return rows[rowIndex];
    }

    public void setColumn(int columnIndex, Vector3 newColumn){
        assert(columnIndex >= 0 && columnIndex<NUM_COLUMNS);

        for(int i=0;i<NUM_COLUMNS;i++)
           setElement(i, columnIndex, newColumn.get(i));
    }

    public Vector3 getColumn(int columnIndex){
        assert(columnIndex >= 0 && columnIndex<NUM_COLUMNS);

        float columnValues[] = new float[3];

        for(int i=0;i<NUM_COLUMNS;i++)
            columnValues[i] = getElement(i, columnIndex);
        return new Vector3(columnValues[0], columnValues[1], columnValues[2]);
    }

    public Vector3 multiplyByVector(Vector3 vector){
        float firstProduct = rows[0].mulitplyByVector(vector);
        float secondProduct = rows[1].mulitplyByVector(vector);
        float thirdProduct = rows[2].mulitplyByVector(vector);

        return new Vector3(firstProduct, secondProduct, thirdProduct);
    }

    public Matrix3 multiplyByMatrix(Matrix3 matrix){
        Matrix3 result = new Matrix3();

        for(int i=0;i<NUM_ROWS;i++){
            for(int j=0;j<NUM_COLUMNS;j++){
                float rowColumnProduct = rows[i].mulitplyByVector(getColumn(j));
                result.setElement(i, j, rowColumnProduct);
            }
        }

        return result;
    }

    public String toString(){
        return new String(rows[0].toString() + "\n" + rows[1].toString() + "\n" + rows[2].toString());
    }
}
