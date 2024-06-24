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
  void draw(float[] data, Mat parent, Mat viewMatrix) {
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
