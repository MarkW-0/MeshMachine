class Terrain extends Model {
  Terrain(float[][][] map) {
    this.local.data[0][1] = -0.5; this.local.data[1][1] = 1.0/(map.length - 1.0);
    this.local.data[0][2] = -0.5; this.local.data[2][2] = 1.0/(map[0].length - 1.0);
    this.local.data[0][3] = 0.5; this.local.data[3][3] = 1.0/(map[0][0].length - 1.0);
    
    for(int x = 0; x < map.length-1; x++) {
      for(int y = 0; y < map[0].length-1; y++) {
        for(int z = 0; z < map[0][0].length-1; z++) {
          this.contents.add(new Cell(map, x, y, z));
        }
      }
    }
  }
}
