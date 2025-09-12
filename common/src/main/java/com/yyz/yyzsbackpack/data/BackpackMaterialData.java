// BackpackMaterialData.java
package com.yyz.yyzsbackpack.data;

public class BackpackMaterialData {
    public String type;
    public int size;
    public int columns;
    public int rows;
    public int width;
    public int height;
    public String guiTexture;
    public String modelTexture;
    
    public BackpackMaterialData() {
    }
    
    public BackpackMaterialData(String type, int size, int columns, int rows, 
                               int width, int height, String guiTexture, String modelTexture) {
        this.type = type;
        this.size = size;
        this.columns = columns;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.guiTexture = guiTexture;
        this.modelTexture = modelTexture;
    }
}