package lando.systems.lordsandships.scene.levelgen;

/**
 * Defines level generation parameters
 *
 * Brian Ploeckelman created on 7/13/2014.
 */
public class LevelGenParams {

	// Default parameter values
	private static int DEFAULT_MAP_MAX_X             = 100;
	private static int DEFAULT_MAP_MAX_Y             = 75;
	private static int DEFAULT_ROOM_WIDTH_MIN        = 4;
	private static int DEFAULT_ROOM_WIDTH_MAX        = 10;
	private static int DEFAULT_ROOM_HEIGHT_MIN       = 4;
	private static int DEFAULT_ROOM_HEIGHT_MAX       = 10;
	private static int DEFAULT_NUM_INITIAL_ROOMS     = 150;
	private static int DEFAULT_NUM_SELECTED_ROOMS    = 15;
	private static float DEFAULT_PERCENT_CYCLE_EDGES = 0.1f; // 10%

	public int mapMaxX;
	public int mapMaxY;
	public int roomWidthMin;
	public int roomWidthMax;
	public int roomHeightMin;
	public int roomHeightMax;
	public int numInitialRooms;
	public int numSelectedRooms;
	public float percentCycleEdges;

	public LevelGenParams() {
		mapMaxX           = DEFAULT_MAP_MAX_X;
		mapMaxY           = DEFAULT_MAP_MAX_Y;
		roomWidthMin      = DEFAULT_ROOM_WIDTH_MIN;
		roomWidthMax      = DEFAULT_ROOM_WIDTH_MAX;
		roomHeightMin     = DEFAULT_ROOM_HEIGHT_MIN;
		roomHeightMax     = DEFAULT_ROOM_HEIGHT_MAX;
		numInitialRooms   = DEFAULT_NUM_INITIAL_ROOMS;
		numSelectedRooms  = DEFAULT_NUM_SELECTED_ROOMS;
		percentCycleEdges = DEFAULT_PERCENT_CYCLE_EDGES;
	}

}
