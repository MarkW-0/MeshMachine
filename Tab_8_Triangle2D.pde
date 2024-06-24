class Triangle2D {
  float dist;
  boolean visible;
  color col;
  float[] screen = new float[6];
  
  Triangle2D() {}
  
  void draw() {
    if(this.visible) {
      fill(this.col);
      triangle(this.screen[0], this.screen[1],
               this.screen[2], this.screen[3],
               this.screen[4], this.screen[5]
      );
    }
  };
}
