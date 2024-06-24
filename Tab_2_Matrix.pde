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
  static Mat perspective(float depth) { // matrix
    Mat mat = new Mat(4);
    mat.data[1][1] = depth;
    mat.data[2][2] = depth;
    mat.data[0][0] = 0; mat.data[3][0] = 1;
    mat.data[0][3] = 1; mat.data[3][3] = -1;
    return mat;
  }
  static Mat rotx(float theta) {
    Mat mat = new Mat(4);
    float c = cos(theta), s = sin(theta);
    mat.data = new float[][]{
      {1.0, 0.0, 0.0, 0.0}, 
      {0.0, 1.0, 0.0, 0.0}, 
      {0.0, 0.0, c,  -s}, 
      {0.0, 0.0, s,   c}};
    return mat;
  }
  static Mat roty(float theta) {
    Mat mat = new Mat(4);
    float c = cos(theta), s = sin(theta);
    mat.data = new float[][]{
      {1.0, 0.0, 0.0, 0.0}, 
      {0.0, c,   0.0,-s}, 
      {0.0, 0.0, 1.0, 0.0}, 
      {0.0, s,   0.0, c}};
    return mat;
  }
  static Mat rotz(float theta) {
    Mat mat = new Mat(4);
    float c = cos(theta), s = sin(theta);
    mat.data = new float[][]{
      {1.0, 0.0, 0.0, 0.0}, 
      {0.0, c,  -s,   0.0}, 
      {0.0, s,   c,   0.0}, 
      {0.0, 0.0, 0.0, 1.0}};
    return mat;
  }
  void printMat(String str) {
    println(str);
    for (int j = 0; j < 4; j++) {// row
      for (int i = 0; i < 4; i++) {// col
        print("\t");
        print(this.data[i][j]);
      }
      println();
    }
  }
  Mat get(int col, int row, int cols, int rows) {
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
  void set(int col, int row, Mat mat) {
    assert(col + mat.cols <= this.cols);
    assert(row + mat.rows <= this.rows);
    for (int i = 0; i < mat.cols; i++) {
      for (int j = 0; j < mat.rows; j++) {
        this.data[col+i][row+j] = mat.data[i][j];
      }
    }
  }
  Mat transpose() {
    Mat mat = new Mat(this.rows, this.cols);
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        mat.data[i][j] = this.data[j][i];
      }
    }
    return mat;
  }
  Mat translate() {
    assert(this.cols == 1);
    Mat mat = new Mat(this.rows);
    mat.set(0, 0, this);
    return mat;
  }
  Mat add(Mat input) {
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
  Mat mult(Mat input) {
    assert(this.cols == input.rows);
    Mat mat = new Mat(input.cols, this.rows);
    for (int i = 0; i < input.cols; i++) {// col
      for (int j = 0; j < this.rows; j++) {// row
        mat.data[i][j] = 0.0;
        for (int k = 0; k < input.rows; k++) {
          mat.data[i][j] += input.data[i][k] * this.data[k][j];
        }
      }
    }
    return mat;
  }
  Mat mult(float input) {
    Mat mat = new Mat(this.cols, this.rows);
    for (int i = 0; i < this.cols; i++) {// col
      for (int j = 0; j < this.rows; j++) {// row
        mat.data[i][j] = input * this.data[i][j];
      }
    }
    return mat;
  }
  Mat invtrans() {
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
  static void saveLR(float[][] left, float[][] right) {
    for (int i = 0; i < 4; i++) {// row
      for (int j = 0; j < 4; j++) {// col
        saveLR[i][j] = left[j][i];
      }
      for (int j = 0; j < 4; j++) {// col
        saveLR[i][j+4] = right[j][i];
      }
    }
  }
  static void saveLR2(float[][] left, float[][] right) {
    for (int i = 0; i < 4; i++) {// row
      for (int j = 0; j < 4; j++) {// col
        saveLR[i][j+8] = left[j][i];
      }
      for (int j = 0; j < 4; j++) {// col
        saveLR[i][j+12] = right[j][i];
      }
    }
  }
  static void printLR(String str, String flag1, int index1, String flag2, int index2) {
    println("\t\t" + "left " + str + "\t\t\t" + "right " + str);
    for (int p1 = 0; p1 < 4; p1++) {// row
      if(p1 == index1) {print(flag1);} print("\t");
      if(p1 == index2) {print(flag2);}
      for (int p2 = 0; p2 < 16; p2++) {// col
        print("\t");
        print(Math.round(10000 * saveLR[p1][p2])/10000.0);
      }
      println();
    }
    println();
  }
  Mat invGauss(Mat mat) {
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
