class Triangle extends Model {
  short mat = 0;
  Triangle2D triangle = new Triangle2D();
  
  void draw(Mat parent, Mat viewMatrix) {
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
  int col(int channel) {
    float value = channel * this.local.data[0][3];
    return max(0, min(255, int(value)));
  }
}
