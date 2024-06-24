import g4p_controls.*;
class Func {
  float cos = random(-1,1);
  float sin = random(-1,1);
  float eval() {
    return this.cos*costime + this.sin*sintime;
  }
}
class MetaBall {
  // MetaBall pos path
  Func X = new Func();
  Func Y = new Func();
  Func Z = new Func();
  float x, y, z;
  void update() {
    this.x = this.X.eval();
    this.y = this.Y.eval();
    this.z = this.Z.eval();
  }
  float eval(float x, float y, float z) {
    x -= this.x; y -= this.y; z -= this.z;
    return 1.0/sqrt(x*x + y*y + z*z);
  }
}
ArrayList<MetaBall> MetaBalls = new ArrayList<MetaBall>();
Model world = new Model();
float[][][] map = new float[12][12][12];

void setup() {
  size(1200, 600);
  frameRate(30);
  createGUI();
  for(int i = 0; i < 2; i++) {
    MetaBalls.add(new MetaBall());
  }
  world = new Terrain(map);
}

void draw() {
  println(frameRate);
  background(128,128,128);
  noStroke();
  for(MetaBall metaBall : MetaBalls) {metaBall.update();} // update Metaballs positons 
  updateMap(map);
  draw3D(world);
}

float func(float x, float y, float z) { // sum of all MetaBalls
  float sum = -2;
  for(MetaBall metaBall : MetaBalls) {
    sum += metaBall.eval(5*x, 5*y, 5*z);
  }
  return sum;
}

void updateMap(float[][][] map) {
  for(float x = 0; x < map.length; x++) { // for every entry in the map
    for(float y = 0; y < map[0].length; y++) {
      for(float z = 0; z < map[0][0].length; z++) {
        // set this entry to ...
        map[(int)x][(int)y][(int)z] = 
          // ... the field evaluated at ... 
          func(
            // the transformed coords.
            x/map.length -0.5,
            y/map[0].length -0.5,
            z/map[0][0].length -0.5
          );
      }
    }
  }
}
