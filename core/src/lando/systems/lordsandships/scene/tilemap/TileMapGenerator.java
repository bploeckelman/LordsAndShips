package lando.systems.lordsandships.scene.tilemap;

import lando.systems.lordsandships.scene.levelgen.Room;
import lando.systems.lordsandships.scene.levelgen.RoomEdge;
import lando.systems.lordsandships.utils.graph.Edge;
import lando.systems.lordsandships.utils.graph.Graph;

import java.util.HashSet;
import java.util.Set;

/**
 * Brian Ploeckelman created on 3/12/2015.
 */
public class TileMapGenerator {

    Graph<Room> roomGraph;
    int width;
    int height;

    public Tile[][] generateTilesFromGraph(Graph<Room> roomGraph) {
        this.roomGraph = roomGraph;
        width     = getMapWidthInTiles();
        height    = getMapHeightInTiles();

        Tile[][] tiles = new Tile[height][width];

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                tiles[y][x] = new Tile(TileType.BLANK, x, y);
            }
        }
        for (Room room : roomGraph.vertices()) {
            generateRoomTiles(room, tiles);
        }
        generateCorridorTiles(tiles);
        generateWallTiles(tiles);

        return tiles;
    }

    private void generateRoomTiles(Room room, Tile[][] tiles) {
        int worldx0 = (int) room.rect.x;
        int worldy0 = (int) room.rect.y;
        int worldx1 = (int)(room.rect.x + room.rect.width)  - 1;
        int worldy1 = (int)(room.rect.y + room.rect.height) - 1;

        // Internal tiles
        for (int y = worldy0 + 1; y < worldy1; ++y) {
            for (int x = worldx0 + 1; x < worldx1; ++x) {
                tiles[y][x].type = TileType.FLOOR;
            }
        }
    }

    private void generateCorridorTiles(Tile[][] tiles) {
        Set<Edge> completedEdges = new HashSet<Edge>();
        RoomEdge edge;
        int xStart, xEnd;
        int yStart, yEnd;

        for (Room u : roomGraph.vertices()) {
            Iterable<Room> neighbors = roomGraph.adjacentTo(u);
            if (neighbors == null) continue;

            // For each edge
            for (Room v : neighbors) {
                edge = new RoomEdge(u, v);
                // If a corridor has already been generated for this edge, skip it
                if (completedEdges.contains(edge)) {
                    continue;
                }

                // Determine direction of corridor:
                if (u.center.x <= v.center.x) {
                    xStart = (int) Math.floor(u.center.x);
                    xEnd   = (int) Math.floor(v.center.x) + 1;
                    int y  = (int) Math.floor(u.center.y);
                    // u is to the left of v
                    for (int x = xStart; x <= xEnd; ++x) {
                        tiles[y-1][x].type = TileType.FLOOR;
                        tiles[y-0][x].type = TileType.FLOOR;
                        tiles[y+1][x].type = TileType.FLOOR;
                    }
                } else {
                    xStart = (int) Math.floor(u.center.x);
                    xEnd   = (int) Math.floor(v.center.x) - 1;
                    int y  = (int) Math.floor(u.center.y);
                    // u is to the right of v
                    for (int x = xStart; x >= xEnd; --x) {
                        tiles[y-1][x].type = TileType.FLOOR;
                        tiles[y-0][x].type = TileType.FLOOR;
                        tiles[y+1][x].type = TileType.FLOOR;
                    }
                }
                if (u.center.y <= v.center.y) {
                    yStart = (int) Math.floor(u.center.y);
                    yEnd   = (int) Math.floor(v.center.y);
                    int x  = (int) Math.floor(v.center.x);
                    // u is above v
                    for (int y = yStart; y <= yEnd; ++y) {
                        tiles[y][x-1].type = TileType.FLOOR;
                        tiles[y][x-0].type = TileType.FLOOR;
                        tiles[y][x+1].type = TileType.FLOOR;
                    }
                } else {
                    yStart = (int) Math.floor(u.center.y);
                    yEnd   = (int) Math.floor(v.center.y);
                    int x  = (int) Math.floor(v.center.x);
                    // u is below v
                    for (int y = yStart; y >= yEnd; --y) {
                        tiles[y][x-1].type = TileType.FLOOR;
                        tiles[y][x-0].type = TileType.FLOOR;
                        tiles[y][x+1].type = TileType.FLOOR;
                    }
                }

                // Add edge to completed list so its reverse isn't also processed
                completedEdges.add(edge);
            }
        }
    }

    private void generateWallTiles(Tile[][] tiles) {
        addCornerTiles(tiles);
        addWallTiles(tiles);
    }

    private void addCornerTiles(Tile[][] tiles) {
        // Add corner wall tiles
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // Clamp neighbor indices to map boundaries
                int xl = (x - 1 < 0) ? x : x - 1;
                int yd = (y - 1 < 0) ? y : y - 1;
                int xr = (x + 1 >= width)  ? x : x + 1;
                int yu = (y + 1 >= height) ? y : y + 1;

                if (tiles[y][x].type == TileType.FLOOR) {
                    addInnerCornerTiles(tiles, x, y, xl, yd, xr, yu);
                    addOuterCornerTiles(tiles, x, y, xl, yd, xr, yu);
                }
            }
        }
    }

    private void addWallTiles(Tile[][] tiles) {
        // Add non-corner wall tiles
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (tiles[y][x].type == TileType.FLOOR) {
                    // Clamp neighbor indices to map boundaries
                    int xl = (x - 1 < 0) ? x : x - 1;
                    int yd = (y - 1 < 0) ? y : y - 1;
                    int xr = (x + 1 >= width)  ? x : x + 1;
                    int yu = (y + 1 >= height) ? y : y + 1;

                    // Check edge neighbors
                    if (tiles[yu][x].type == TileType.BLANK) {
                        tiles[yu][x].type = TileType.WALL_HORIZ_N;
                    }
                    if (tiles[yd][x].type == TileType.BLANK) {
                        tiles[yd][x].type = TileType.WALL_HORIZ_S;
                    }
                    if (tiles[y][xl].type == TileType.BLANK) {
                        tiles[y][xl].type = TileType.WALL_VERT_E;
                    }
                    if (tiles[y][xr].type == TileType.BLANK) {
                        tiles[y][xr].type = TileType.WALL_VERT_W;
                    }
                }
            }
        }
    }


    private boolean isCorner(TileType type) {
        return (type == TileType.CORNER_OUTER_NE || type == TileType.CORNER_OUTER_NW
             || type == TileType.CORNER_OUTER_SE || type == TileType.CORNER_OUTER_SW
             || type == TileType.CORNER_INNER_NE || type == TileType.CORNER_INNER_NW
             || type == TileType.CORNER_INNER_SE || type == TileType.CORNER_INNER_SW);
    }

    private void addOuterCornerTiles(Tile[][] tiles, int x, int y, int xl, int yd, int xr, int yu) {
        if ((tiles[y ][xl].type == TileType.BLANK || isCorner(tiles[y ][xl].type))
         &&  tiles[yu][xl].type == TileType.BLANK
         && (tiles[yu][x ].type == TileType.BLANK || isCorner(tiles[yu][x ].type))) {
            tiles[yu][xl].type = TileType.CORNER_OUTER_NW;
        }
        if ((tiles[yu][x ].type == TileType.BLANK || isCorner(tiles[yu][x ].type))
         &&  tiles[yu][xr].type == TileType.BLANK
         && (tiles[y ][xr].type == TileType.BLANK || isCorner(tiles[y ][xr].type))) {
            tiles[yu][xr].type = TileType.CORNER_OUTER_NE;
        }
        if ((tiles[y ][xr].type == TileType.BLANK || isCorner(tiles[y ][xr].type))
         &&  tiles[yd][xr].type == TileType.BLANK
         && (tiles[yd][x ].type == TileType.BLANK || isCorner(tiles[yd][x ].type))) {
            tiles[yd][xr].type = TileType.CORNER_OUTER_SE;
        }
        if ((tiles[yd][x ].type == TileType.BLANK || isCorner(tiles[yd][x ].type))
         &&  tiles[yd][xl].type == TileType.BLANK
         && (tiles[y ][xl].type == TileType.BLANK || isCorner(tiles[y ][xl].type))) {
            tiles[yd][xl].type = TileType.CORNER_OUTER_SW;
        }
    }

    private void addInnerCornerTiles(Tile[][] tiles, int x, int y, int xl, int yd, int xr, int yu) {
        if (tiles[y ][xl].type != TileType.BLANK && !isCorner(tiles[y ][xl].type)
         && tiles[yu][xl].type == TileType.BLANK
         && tiles[yu][x ].type != TileType.BLANK && !isCorner(tiles[yu][x ].type)) {
            tiles[yu][xl].type = TileType.CORNER_INNER_NE;
        }
        if (tiles[yu][x ].type != TileType.BLANK && !isCorner(tiles[yu][x ].type)
         && tiles[yu][xr].type == TileType.BLANK
         && tiles[y ][xr].type != TileType.BLANK && !isCorner(tiles[y ][xr].type)) {
            tiles[yu][xr].type = TileType.CORNER_INNER_NW;
        }
        if (tiles[y ][xr].type != TileType.BLANK && !isCorner(tiles[y ][xr].type)
         && tiles[yd][xr].type == TileType.BLANK
         && tiles[yd][x ].type != TileType.BLANK && !isCorner(tiles[yd][x ].type)) {
            tiles[yd][xr].type = TileType.CORNER_INNER_SW;
        }
        if (tiles[yd][x ].type != TileType.BLANK && !isCorner(tiles[yd][x ].type)
         && tiles[yd][xl].type == TileType.BLANK
         && tiles[y ][xl].type != TileType.BLANK && !isCorner(tiles[y ][xl].type)) {
            tiles[yd][xl].type = TileType.CORNER_INNER_SE;
        }
    }

    private int getMapWidthInTiles() {
        int width = 0;
        if (roomGraph.vertices() == null) {
            return width;
        }

        for (Room room : roomGraph.vertices()) {
            int x = (int) (room.rect.x + room.rect.width);
            if (width < x) width = x;
        }
        return width;
    }

    private int getMapHeightInTiles() {
        int height = 0;
        if (roomGraph.vertices() == null) {
            return height;
        }

        for (Room room : roomGraph.vertices()) {
            int y = (int) (room.rect.y + room.rect.height);
            if (height < y) height = y;
        }
        return height;
    }

}
