static class Model {
  Mat local = new Mat(4);
  ArrayList<Model> contents = new ArrayList<Model>();
  
  Model() {}
  
  void draw(Mat parent, Mat viewMatrix) {
    Mat world = parent.mult(this.local);
    for (Model model : this.contents) {
      model.draw(world, viewMatrix);
    }
  }
}
