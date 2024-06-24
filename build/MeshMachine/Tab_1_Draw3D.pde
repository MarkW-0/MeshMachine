import java.util.*;
import java.util.function.*;
byte[] stl_header = new byte[80]; // saved, not used
ArrayList<Triangle2D> triangles = new ArrayList<Triangle2D>();
Comparator<Triangle2D> compare = new Comparator<Triangle2D>(){// approx overlap comparison
  int compare(Triangle2D a, Triangle2D b){
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
float pitch = 0.5;

int redValue = 128;
int greenValue = 128;
int blueValue = 128;

void draw3D(Model world) {
  if (!pause){
    frame++;
  }
  time = 0.05 * frame;
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
