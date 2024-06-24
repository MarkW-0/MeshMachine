import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import g4p_controls.*; 
import java.util.*; 
import java.util.function.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class MeshMachine extends PApplet {


class Func {
  float cos = random(-1,1);
  float sin = random(-1,1);
  public float eval() {
    return this.cos*costime + this.sin*sintime;
  }
}
class MetaBall {
  // MetaBall pos path
  Func X = new Func();
  Func Y = new Func();
  Func Z = new Func();
  float x, y, z;
  public void update() {
    this.x = this.X.eval();
    this.y = this.Y.eval();
    this.z = this.Z.eval();
  }
  public float eval(float x, float y, float z) {
    x -= this.x; y -= this.y; z -= this.z;
    return 1.0f/sqrt(x*x + y*y + z*z);
  }
}
ArrayList<MetaBall> MetaBalls = new ArrayList<MetaBall>();
Model world = new Model();
float[][][] map = new float[12][12][12];

public void setup() {
  
  frameRate(30);
  createGUI();
  for(int i = 0; i < 2; i++) {
    MetaBalls.add(new MetaBall());
  }
  world = new Terrain(map);
}

public void draw() {
  println(frameRate);
  background(128,128,128);
  noStroke();
  for(MetaBall metaBall : MetaBalls) {metaBall.update();} // update Metaballs positons 
  updateMap(map);
  draw3D(world);
}

public float func(float x, float y, float z) { // sum of all MetaBalls
  float sum = -2;
  for(MetaBall metaBall : MetaBalls) {
    sum += metaBall.eval(5*x, 5*y, 5*z);
  }
  return sum;
}

public void updateMap(float[][][] map) {
  for(float x = 0; x < map.length; x++) { // for every entry in the map
    for(float y = 0; y < map[0].length; y++) {
      for(float z = 0; z < map[0][0].length; z++) {
        // set this entry to ...
        map[(int)x][(int)y][(int)z] = 
          // ... the field evaluated at ... 
          func(
            // the transformed coords.
            x/map.length -0.5f,
            y/map[0].length -0.5f,
            z/map[0][0].length -0.5f
          );
      }
    }
  }
}


byte[] stl_header = new byte[80]; // saved, not used
ArrayList<Triangle2D> triangles = new ArrayList<Triangle2D>();
Comparator<Triangle2D> compare = new Comparator<Triangle2D>(){// approx overlap comparison
  public int compare(Triangle2D a, Triangle2D b){
    return Float.compare(b.dist, a.dist);
  }
};

boolean pause = false;
int frame = 0;
float time = 0;
static float costime = 1;
static float sintime = 0;
Mat translate = new Mat(new float[]{1, 0, -2, 1});
float zoom = 3;
float yaw = 0;
float pitch = 0.5f;

int redValue = 128;
int greenValue = 128;
int blueValue = 128;

public void draw3D(Model world) {
  if (!pause){
    frame++;
  }
  time = 0.05f * frame;
  costime = cos(time);
  sintime = sin(time);
  Mat m0 = Mat.rotz(yaw*PI /*+ 0.01*frame*/);
  Mat m1 = Mat.rotx(pitch*PI /* + 0.1*sin(0.02*frameCount)*/);
  Mat camMatrix = m0.mult(translate.translate()).mult(m1);
  Mat drawMatrix = Mat.perspective(2400 / zoom);
  Mat viewMatrix = drawMatrix.mult(camMatrix.invtrans());
  
  triangles.clear();
  world.draw(new Mat(4), viewMatrix);
  triangles.sort(compare);
  for(Triangle2D triangle : triangles) {
    triangle.draw();
  }
}
static class Mat {
  float[][] data;
  int cols, rows;
  
  Mat(float[] data) { // vector with data
    assert(0 < data.length);
    this.cols = 1;
    this.rows = data.length;
    this.data = new float[this.cols][this.rows];
    for (int i = 0; i < this.rows; i++) {
      this.data[0][i] = data[i];
    }
  }
  Mat(float[][] data) { // matrix with data
    assert(0 < data.length);
    this.cols = data.length;
    this.rows = data[0].length;
    this.data = new float[this.cols][this.rows];
    for (int i = 0; i < this.cols; i++) {
      assert(data[i].length == this.rows);
      for (int j = 0; j < this.rows; j++) {
        this.data[i][j] = data[i][j];
      }
    }
  }
  Mat(int size) { // sqare ID matrix size
    this.cols = size;
    this.rows = size;
    this.data = new float[size][size];
    for(int i = 0; i < size ; i++) {
      this.data[i][i] = 1;
    }
  }
  Mat(int cols, int rows) { // ID matrix size
    this.cols = cols;
    this.rows = rows;
    this.data = new float[cols][rows];
    for(int i = 0; i < min(cols, rows) ; i++) {
      this.data[i][i] = 1;
    }
  }
  public static Mat perspective(float depth) { // matrix
    Mat mat = new Mat(4);
    mat.data[1][1] = depth;
    mat.data[2][2] = depth;
    mat.data[0][0] = 0; mat.data[3][0] = 1;
    mat.data[0][3] = 1; mat.data[3][3] = -1;
    return mat;
  }
  public static Mat rotx(float theta) {
    Mat mat = new Mat(4);
    float c = cos(theta), s = sin(theta);
    mat.data = new float[][]{
      {1.0f, 0.0f, 0.0f, 0.0f}, 
      {0.0f, 1.0f, 0.0f, 0.0f}, 
      {0.0f, 0.0f, c,  -s}, 
      {0.0f, 0.0f, s,   c}};
    return mat;
  }
  public static Mat roty(float theta) {
    Mat mat = new Mat(4);
    float c = cos(theta), s = sin(theta);
    mat.data = new float[][]{
      {1.0f, 0.0f, 0.0f, 0.0f}, 
      {0.0f, c,   0.0f,-s}, 
      {0.0f, 0.0f, 1.0f, 0.0f}, 
      {0.0f, s,   0.0f, c}};
    return mat;
  }
  public static Mat rotz(float theta) {
    Mat mat = new Mat(4);
    float c = cos(theta), s = sin(theta);
    mat.data = new float[][]{
      {1.0f, 0.0f, 0.0f, 0.0f}, 
      {0.0f, c,  -s,   0.0f}, 
      {0.0f, s,   c,   0.0f}, 
      {0.0f, 0.0f, 0.0f, 1.0f}};
    return mat;
  }
  public void printMat(String str) {
    println(str);
    for (int j = 0; j < 4; j++) {// row
      for (int i = 0; i < 4; i++) {// col
        print("\t");
        print(this.data[i][j]);
      }
      println();
    }
  }
  public Mat get(int col, int row, int cols, int rows) {
    assert(col + cols <= this.cols);
    assert(row + rows <= this.rows);
    Mat mat = new Mat(cols, rows);
    for (int i = 0; i < cols; i++) {
      for (int j = 0; j < rows; j++) {
        mat.data[i][j] = this.data[col+i][row+j];
      }
    }
    return mat;
  }
  public void set(int col, int row, Mat mat) {
    assert(col + mat.cols <= this.cols);
    assert(row + mat.rows <= this.rows);
    for (int i = 0; i < mat.cols; i++) {
      for (int j = 0; j < mat.rows; j++) {
        this.data[col+i][row+j] = mat.data[i][j];
      }
    }
  }
  public Mat transpose() {
    Mat mat = new Mat(this.rows, this.cols);
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        mat.data[i][j] = this.data[j][i];
      }
    }
    return mat;
  }
  public Mat translate() {
    assert(this.cols == 1);
    Mat mat = new Mat(this.rows);
    mat.set(0, 0, this);
    return mat;
  }
  public Mat add(Mat input) {
    assert(this.cols == input.cols);
    assert(this.rows == input.rows);
    Mat mat = new Mat(input.cols, this.rows);
    for (int i = 0; i < this.cols; i++) {// col
      for (int j = 0; j < this.rows; j++) {// row
        mat.data[i][j] = input.data[i][j] + this.data[i][j];
      }
    }
    return mat;
  }
  public Mat mult(Mat input) {
    assert(this.cols == input.rows);
    Mat mat = new Mat(input.cols, this.rows);
    for (int i = 0; i < input.cols; i++) {// col
      for (int j = 0; j < this.rows; j++) {// row
        mat.data[i][j] = 0.0f;
        for (int k = 0; k < input.rows; k++) {
          mat.data[i][j] += input.data[i][k] * this.data[k][j];
        }
      }
    }
    return mat;
  }
  public Mat mult(float input) {
    Mat mat = new Mat(this.cols, this.rows);
    for (int i = 0; i < this.cols; i++) {// col
      for (int j = 0; j < this.rows; j++) {// row
        mat.data[i][j] = input * this.data[i][j];
      }
    }
    return mat;
  }
  public Mat invtrans() {
    // [ 1 | 0 ] -1  [ 1   | 0 ]
    // [ C | D ]   = [-D'C | D'
    assert(this.cols == 4);
    assert(this.rows == 4);
    Mat translate = this.get(0, 0, 1, 4);
    Mat transpose = this.transpose();
    transpose.set(0, 0, new Mat(4, 1));
    Mat invtranslate = transpose.mult(translate);
    for (int j = 1; j < 4; j++) { // row
        invtranslate.data[0][j] = -invtranslate.data[0][j];
    }
    transpose.set(0, 0, invtranslate);
    return transpose;
  }
  static float[][] saveLR = new float[4][16];
  public static void saveLR(float[][] left, float[][] right) {
    for (int i = 0; i < 4; i++) {// row
      for (int j = 0; j < 4; j++) {// col
        saveLR[i][j] = left[j][i];
      }
      for (int j = 0; j < 4; j++) {// col
        saveLR[i][j+4] = right[j][i];
      }
    }
  }
  public static void saveLR2(float[][] left, float[][] right) {
    for (int i = 0; i < 4; i++) {// row
      for (int j = 0; j < 4; j++) {// col
        saveLR[i][j+8] = left[j][i];
      }
      for (int j = 0; j < 4; j++) {// col
        saveLR[i][j+12] = right[j][i];
      }
    }
  }
  public static void printLR(String str, String flag1, int index1, String flag2, int index2) {
    println("\t\t" + "left " + str + "\t\t\t" + "right " + str);
    for (int p1 = 0; p1 < 4; p1++) {// row
      if(p1 == index1) {print(flag1);} print("\t");
      if(p1 == index2) {print(flag2);}
      for (int p2 = 0; p2 < 16; p2++) {// col
        print("\t");
        print(Math.round(10000 * saveLR[p1][p2])/10000.0f);
      }
      println();
    }
    println();
  }
  public Mat invGauss(Mat mat) {
    //Gaussian elimination
    float[][] left = new float[4][4];
    float[][] right = new float[][]{
      {1, 0, 0, 0},
      {0, 1, 0, 0},
      {0, 0, 1, 0},
      {0, 0, 0, 1}
    };
    for (int i = 0; i < 4; i++) {// col
      for (int j = 0; j < 4; j++) {// row
        left[i][j] = mat.data[i][j];
      }
    }
    for (int i = 0; i < 4; i++) {// step
      //swap max pivot
        saveLR(left, right);
        int index = i;
        for (int j = i; j < 4; j++) {// row
          if(Math.abs(left[i][index]) < Math.abs(left[i][j])) index = j;
        }
        for (int j = i; j < 4; j++) {
          float temp = left[j][i];
          left[j][i] = left[j][index];
          left[j][index] = temp;
          temp = right[j][i];
          right[j][i] = right[j][index];
          right[j][index] = temp;
        }
        saveLR2(left, right);
        printLR("0." + i + ".0", "max", index, "swap", i);
      //
        float pivot = left[i][i];
        for (int j = i+1; j < 4; j++) {// row
          saveLR(left, right);
          float coefficient = -left[i][j]/pivot;
          for (int k = 0; k < 4; k++) {
            left[k][j] += coefficient*left[k][i];
            right[k][j] += coefficient*right[k][i];
          }
          saveLR2(left, right);
          printLR("0." + i + "." + j, "pivot", i, "row", j);
        }
    }
    //back substitution
    //a x y
    //  b z
    //    c
    for (int i = 3; 0 <= i; i--) {// step
      float pivot = left[i][i];
      for (int j = 0; j < i; j++) {// row
        saveLR(left, right);
        float coefficient = -left[i][j]/pivot;
        for (int k = 0; k < 4; k++) {
          left[k][j] += coefficient*left[k][i];
          right[k][j] += coefficient*right[k][i];
        }
        saveLR2(left, right);
        printLR("1." + i + "." + j, "pivot", i, "row", j);
      }
      saveLR(left, right);
      for (int j = 0; j < 4; j++) {// row
        left[j][i] /= pivot;
        right[j][i] /= pivot;
      }
      saveLR2(left, right);
      printLR("1." + i + ".4", "divide", i, "pivot", i);
    }
    this.data = right;
    return this;
  }
}
/*math matrix
 / a11, a12, a13, a14 \
 | a21, a22, a23, a24 |
 | a31, a32, a33, a34 |
 \ a41, a42, a43, a44 /
 (a.b)ij = sum(1 <= k <= 4) aik * bkj
 
 code matrix
 [[a[0][0], a[0][1], a[0][2], a[0][3]],
 [[a[1][0], a[1][1], a[1][2], a[1][3]],
 [[a[2][0], a[2][1], a[2][2], a[2][3]],
 [[a[3][0], a[3][1], a[3][2], a[3][3]],
 mult(a, b)[i][j] = for(k = 0; k < 4; k++) sum += a[i][k] * b[k][j];
 
 aij = a[i-1][j-1]
 */
static class Model {
  Mat local = new Mat(4);
  ArrayList<Model> contents = new ArrayList<Model>();
  
  Model() {}
  
  public void draw(Mat parent, Mat viewMatrix) {
    Mat world = parent.mult(this.local);
    for (Model model : this.contents) {
      model.draw(world, viewMatrix);
    }
  }
}
class Terrain extends Model {
  Terrain(float[][][] map) {
    this.local.data[0][1] = -0.5f; this.local.data[1][1] = 1.0f/(map.length - 1.0f);
    this.local.data[0][2] = -0.5f; this.local.data[2][2] = 1.0f/(map[0].length - 1.0f);
    this.local.data[0][3] = 0.5f; this.local.data[3][3] = 1.0f/(map[0][0].length - 1.0f);
    
    for(int x = 0; x < map.length-1; x++) {
      for(int y = 0; y < map[0].length-1; y++) {
        for(int z = 0; z < map[0][0].length-1; z++) {
          this.contents.add(new Cell(map, x, y, z));
        }
      }
    }
  }
}
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
  public void draw(Mat parent, Mat viewMatrix) {
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
//@FunctionalInterface
//interface Case {
//  int operation(int a, int b);
//}
int[][] tetrahedronToEdges = {
          {0, 1}, {0, 2}, {0, 3},
  {1, 0},         {1, 2}, {1, 3},
  {2, 0}, {2, 1},         {2, 3},
  {3, 0}, {3, 1}, {3, 2},        
};
class Tetrahedron extends Model {
  int[] vertexIndices = new int[4];
  float[][] vecs = new float[4][4];
  Tetrahedron() {
    this.contents.add(new Triangle());
    this.contents.add(new Triangle());
  }
  public void draw(float[] data, Mat parent, Mat viewMatrix) {
    Mat world = parent.mult(this.local);
    ArrayList<Mat> Points = new ArrayList<Mat>();
    int ID = 0;
    for(int i = 0; i < 4; i++) {
      for(int j = 0; j < 4; j++) {
        if(data[i] <= 0 && 0 < data[j]) {
          Mat A = new Mat(vecs[i]).mult(-data[j]);
          Mat B = new Mat(vecs[j]).mult(data[i]);
          Points.add(A.add(B).mult(1/(data[i] - data[j])));
          ID++;
        }
        ID <<= 2;
      }
    }
    if(Points.size() == 3) {
      Triangle triangle = (Triangle)this.contents.get(0);
      switch(ID) {
        case 82944:
        case 67372032:
        case 1073758272:
        case 1409286144:
          triangle.local.set(1, 0, Points.get(0));
          triangle.local.set(2, 0, Points.get(1));
          triangle.local.set(3, 0, Points.get(2));
          break;
        case 336: 
        case 16843008:
        case 18087936:
        case 269484048:
          triangle.local.set(1, 0, Points.get(2));
          triangle.local.set(2, 0, Points.get(1));
          triangle.local.set(3, 0, Points.get(0));
          break;
        default:
          print(ID, "\t");
      }
      triangle.draw(world, viewMatrix);
    } else if(Points.size() == 4) {
      Triangle triangle1 = (Triangle)this.contents.get(0);
      Triangle triangle2 = (Triangle)this.contents.get(1);
      switch(ID) {
        case 82240:
          triangle1.local.set(1, 0, Points.get(0));
          triangle1.local.set(2, 0, Points.get(1));
          triangle1.local.set(3, 0, Points.get(2));
          triangle2.local.set(1, 0, Points.get(3));
          triangle2.local.set(2, 0, Points.get(2));
          triangle2.local.set(3, 0, Points.get(1));
          break;
        case 17105920:
          triangle1.local.set(1, 0, Points.get(0));
          triangle1.local.set(2, 0, Points.get(1));
          triangle1.local.set(3, 0, Points.get(2));
          triangle2.local.set(1, 0, Points.get(3));
          triangle2.local.set(2, 0, Points.get(2));
          triangle2.local.set(3, 0, Points.get(1));
          break;
        case 17826064:
          triangle1.local.set(1, 0, Points.get(2));
          triangle1.local.set(2, 0, Points.get(1));
          triangle1.local.set(3, 0, Points.get(0));
          triangle2.local.set(1, 0, Points.get(1));
          triangle2.local.set(2, 0, Points.get(2));
          triangle2.local.set(3, 0, Points.get(3));
          break;
        case 336855040:
          triangle1.local.set(1, 0, Points.get(0));
          triangle1.local.set(2, 0, Points.get(1));
          triangle1.local.set(3, 0, Points.get(2));
          triangle2.local.set(1, 0, Points.get(3));
          triangle2.local.set(2, 0, Points.get(2));
          triangle2.local.set(3, 0, Points.get(1));
          break;
        case 1342177360:
          triangle1.local.set(1, 0, Points.get(0));
          triangle1.local.set(2, 0, Points.get(1));
          triangle1.local.set(3, 0, Points.get(2));
          triangle2.local.set(1, 0, Points.get(3));
          triangle2.local.set(2, 0, Points.get(2));
          triangle2.local.set(3, 0, Points.get(1));
          break;
        case 1140868096:
          triangle1.local.set(1, 0, Points.get(2));
          triangle1.local.set(2, 0, Points.get(1));
          triangle1.local.set(3, 0, Points.get(0));
          triangle2.local.set(1, 0, Points.get(1));
          triangle2.local.set(2, 0, Points.get(2));
          triangle2.local.set(3, 0, Points.get(3));
          break;
        default:
          print(ID, "\t");
      }
      triangle1.draw(world, viewMatrix);
      triangle2.draw(world, viewMatrix);
    }
  }
}
class Triangle extends Model {
  short mat = 0;
  Triangle2D triangle = new Triangle2D();
  
  public void draw(Mat parent, Mat viewMatrix) {
    Mat O = this.local.get(1, 0, 1, 4).mult(-1);
    Mat A = this.local.get(2, 0, 1, 4).add(O);
    Mat B = this.local.get(3, 0, 1, 4).add(O);
    assert(A.cols == 1); assert(A.rows == 4);
    assert(B.cols == 1); assert(B.rows == 4);
    this.local.set(0, 0, new Mat(new float[]{
      0,
      A.data[0][2] * B.data[0][3] - A.data[0][3] * B.data[0][2],
      A.data[0][3] * B.data[0][1] - A.data[0][1] * B.data[0][3],
      A.data[0][1] * B.data[0][2] - A.data[0][2] * B.data[0][1],
    }));
    this.triangle.dist = 0;
    Mat world = viewMatrix.mult(parent).mult(this.local);
    for( int i = 0; i < 3; i++ ) {
      float dist = world.data[i+1][0];
      if(dist < 0) return;
      this.triangle.dist += dist;
      this.triangle.screen[0+2*i] = world.data[i+1][1]/dist + width/2;
      this.triangle.screen[1+2*i] = world.data[i+1][2]/dist + height/2;
    }
    this.triangle.visible = 
        this.triangle.screen[0] * (this.triangle.screen[3] - this.triangle.screen[5])
      + this.triangle.screen[2] * (this.triangle.screen[5] - this.triangle.screen[1])
      + this.triangle.screen[4] * (this.triangle.screen[1] - this.triangle.screen[3])
      < 0;
    triangles.add(this.triangle);
    this.triangle.col = color(col(redValue), col(greenValue), col(blueValue));
  };
  public int col(int channel) {
    float value = channel * this.local.data[0][3];
    return max(0, min(255, PApplet.parseInt(value)));
  }
}
class Triangle2D {
  float dist;
  boolean visible;
  int col;
  float[] screen = new float[6];
  
  Triangle2D() {}
  
  public void draw() {
    if(this.visible) {
      fill(this.col);
      triangle(this.screen[0], this.screen[1],
               this.screen[2], this.screen[3],
               this.screen[4], this.screen[5]
      );
    }
  };
}
public void parseSTL(Model world, byte[] stl) {
  int i = 0;
  for(int i2 = 0; i2 < 80; i2++) { stl_header[i2] = stl[i++]; }
  int stl_size = 0;
  for(int i2 = 0; i2 < 32; i2 += 8) { stl_size |= (stl[i++] & 0xFF) << i2; }
  println(stl_size);
  world.contents.clear();
  for(int i2 = 0; i2 < stl_size; i2++) {
    Triangle triangle = new Triangle();
    world.contents.add(triangle);
    for(int i3 = 0; i3 < 4; i3++) {
      triangle.local.data[i3][0] = 1;
      for(int i4 = 1; i4 < 4; i4++) {
        int temp = 0;
        for(int i5 = 0; i5 < 32; i5 += 8) { temp |= (stl[i++] & 0xFF) << i5; }
        triangle.local.data[i3][i4] = Float.intBitsToFloat(temp);
      }
    }
    triangle.local.data[0][0] = 0;
    for(int i3 = 0; i3 < 16; i3 += 8) { triangle.mat |= (stl[i++] & 0xFF) << i3; }
  }
}
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */

public void blue_change(GSlider source, GEvent event) { //_CODE_:blue_slider:503038:
  blueValue = PApplet.parseInt(source.getValueF() * 255);
  println("slider1 - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:blue_slider:503038:

public void green_change(GSlider source, GEvent event) { //_CODE_:green_slider:856539:
  greenValue = PApplet.parseInt(source.getValueF() * 255);
  println("slider2 - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:green_slider:856539:

public void red_change(GSlider source, GEvent event) { //_CODE_:red_slider:200590:
  redValue = PApplet.parseInt(source.getValueF() * 255);
  println("slider3 - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:red_slider:200590:

public void pos_change(GSlider2D source, GEvent event) { //_CODE_:pos_slider:813612:
  translate.data[0][1] = source.getValueXF();
  translate.data[0][2] = source.getValueYF();
  println("slider2d1 - GSlider2D >> GEvent." + event + " @ " + millis());
} //_CODE_:pos_slider:813612:

public void zoom_change(GSlider source, GEvent event) { //_CODE_:zoom_slider:412975:
  zoom = source.getValueF();
  println("slider4 - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:zoom_slider:412975:

public void yaw_change(GSlider source, GEvent event) { //_CODE_:yaw_slider:707459:
  yaw = source.getValueF();
  println("slider5 - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:yaw_slider:707459:

public void pause_click(GButton source, GEvent event) { //_CODE_:pause_button:319142:
  pause = !pause;
  println("button5 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:pause_button:319142:

public void pitch_change(GSlider source, GEvent event) { //_CODE_:pitch_slider:252390:
  pitch = source.getValueF();
  println("slider1 - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:pitch_slider:252390:

public void height_change(GSlider source, GEvent event) { //_CODE_:height_slider:787319:
  translate.data[0][3] = source.getValueF();
  println("slider2 - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:height_slider:787319:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setMouseOverEnabled(false);
  surface.setTitle("Sketch Window");
  label1 = new GLabel(this, 10, 10, 110, 20);
  label1.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  label1.setText("Mesh Machine");
  label1.setOpaque(true);
  label3 = new GLabel(this, 30, 240, 70, 20);
  label3.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  label3.setText("Color");
  label3.setOpaque(true);
  blue_slider = new GSlider(this, 120, 270, 90, 30, 10.0f);
  blue_slider.setRotation(PI/2, GControlMode.CORNER);
  blue_slider.setLimits(0.5f, 1.0f, 0.0f);
  blue_slider.setNumberFormat(G4P.DECIMAL, 2);
  blue_slider.setOpaque(false);
  blue_slider.addEventHandler(this, "blue_change");
  green_slider = new GSlider(this, 80, 270, 90, 30, 10.0f);
  green_slider.setRotation(PI/2, GControlMode.CORNER);
  green_slider.setLimits(0.5f, 1.0f, 0.0f);
  green_slider.setNumberFormat(G4P.DECIMAL, 2);
  green_slider.setOpaque(false);
  green_slider.addEventHandler(this, "green_change");
  red_slider = new GSlider(this, 40, 270, 90, 30, 10.0f);
  red_slider.setRotation(PI/2, GControlMode.CORNER);
  red_slider.setLimits(0.5f, 1.0f, 0.0f);
  red_slider.setNumberFormat(G4P.DECIMAL, 2);
  red_slider.setOpaque(false);
  red_slider.addEventHandler(this, "red_change");
  pos_slider = new GSlider2D(this, 10, 40, 110, 110);
  pos_slider.setLimitsX(0.0f, -3.0f, 3.0f);
  pos_slider.setLimitsY(-3.0f, -3.0f, 3.0f);
  pos_slider.setNumberFormat(G4P.DECIMAL, 2);
  pos_slider.setOpaque(true);
  pos_slider.addEventHandler(this, "pos_change");
  zoom_slider = new GSlider(this, 10, 200, 110, 30, 10.0f);
  zoom_slider.setLimits(5.0f, 3.0f, 10.0f);
  zoom_slider.setNumberFormat(G4P.DECIMAL, 2);
  zoom_slider.setOpaque(false);
  zoom_slider.addEventHandler(this, "zoom_change");
  yaw_slider = new GSlider(this, 10, 160, 110, 30, 10.0f);
  yaw_slider.setLimits(0.0f, -1.0f, 1.0f);
  yaw_slider.setNumberFormat(G4P.DECIMAL, 2);
  yaw_slider.setOpaque(false);
  yaw_slider.addEventHandler(this, "yaw_change");
  pause_button = new GButton(this, 130, 160, 30, 30);
  pause_button.setText("| |");
  pause_button.addEventHandler(this, "pause_click");
  pitch_slider = new GSlider(this, 160, 40, 110, 30, 10.0f);
  pitch_slider.setRotation(PI/2, GControlMode.CORNER);
  pitch_slider.setLimits(0.5f, 0.0f, 1.0f);
  pitch_slider.setNumberFormat(G4P.DECIMAL, 2);
  pitch_slider.setOpaque(false);
  pitch_slider.addEventHandler(this, "pitch_change");
  height_slider = new GSlider(this, 200, 40, 110, 30, 10.0f);
  height_slider.setRotation(PI/2, GControlMode.CORNER);
  height_slider.setLimits(3.0f, 3.0f, -3.0f);
  height_slider.setNumberFormat(G4P.DECIMAL, 2);
  height_slider.setOpaque(false);
  height_slider.addEventHandler(this, "height_change");
}

// Variable declarations 
// autogenerated do not edit
GLabel label1; 
GLabel label3; 
GSlider blue_slider; 
GSlider green_slider; 
GSlider red_slider; 
GSlider2D pos_slider; 
GSlider zoom_slider; 
GSlider yaw_slider; 
GButton pause_button; 
GSlider pitch_slider; 
GSlider height_slider; 
  public void settings() {  size(1200, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "MeshMachine" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
