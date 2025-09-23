// BackpackMaterialData.java
package com.yyz.yyzsbackpack.data;

public class BackpackMaterialData {
    public String type;
    public int size;
    public int columns;
    public int rows;
    public int guiWidth;
    public int guiHeight;
    public String guiTexture;
    public String modelTexture;
    
    public BackpackMaterialData() {
    }
    
    public BackpackMaterialData(String type, int size, int columns, int rows,
                                int guiWidth, int guiHeight, String guiTexture, String modelTexture) {
        this.type = type;
        this.size = size;
        this.columns = columns;
        this.rows = rows;
        this.guiWidth = guiWidth;
        this.guiHeight = guiHeight;
        this.guiTexture = guiTexture;
        this.modelTexture = modelTexture;
    }
}