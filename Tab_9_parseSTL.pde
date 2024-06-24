void parseSTL(Model world, byte[] stl) {
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
