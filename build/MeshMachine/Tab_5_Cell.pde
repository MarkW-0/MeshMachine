int[][] cubeToDatas = {
  {0, 1, 3, 7}, //   6----7
  {0, 5, 1, 7}, //  /|   /|
  {0, 4, 5, 7}, // 4----5 |
  {0, 6, 4, 7}, // | 2--|-3
  {0, 2, 6, 7}, // |/   |/
  {0, 3, 2, 7}, // 0----1
};
float[][] cubeToVecs = {
  {1, 0, 0, 0},
  {1, 1, 0, 0},
  {1, 0, 1, 0},
  {1, 1, 1, 0},
  {1, 0, 0, 1},
  {1, 1, 0, 1},
  {1, 0, 1, 1},
  {1, 1, 1, 1},
};

class Cell extends Model {
  int x, y, z;
  float[] data = new float[8];
  float[][][] map;
  Cell(float[][][] map, int x, int y, int z) {
    this.x = x; this.y = y; this.z = z;
    this.map = map;
    this.local = new Mat(new float[][]{{1, x, y, z}}).translate();
    for (int i = 0; i < cubeToDatas.length; i++) {
        Tetrahedron tetrahedron = new Tetrahedron();
        for(int v = 0; v < 4; v++) {
          tetrahedron.vertexIndices[v] = cubeToDatas[i][v];
          tetrahedron.vecs[v] = cubeToVecs[cubeToDatas[i][v]];
        }
        this.contents.add(tetrahedron);
    }
  };
  void draw(Mat parent, Mat viewMatrix) {
    this.data[0] = map[x][y][z];     this.data[1] = map[x+1][y][z];     //   6---7
    this.data[2] = map[x][y+1][z];   this.data[3] = map[x+1][y+1][z];   // 4---5 |
    this.data[4] = map[x][y][z+1];   this.data[5] = map[x+1][y][z+1];   // | 2-|-3
    this.data[6] = map[x][y+1][z+1]; this.data[7] = map[x+1][y+1][z+1]; // 0---1
    Mat world = parent.mult(this.local);
    for (Model model : this.contents) {
      Tetrahedron tetrahedron = (Tetrahedron)model;
        float[] tetrahedronData = new float[4];
        for(int v = 0; v < 4; v++) {
          tetrahedronData[v] = data[tetrahedron.vertexIndices[v]];
        }
      tetrahedron.draw(tetrahedronData, world, viewMatrix);
    }
  }
}
