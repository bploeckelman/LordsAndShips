package lando.systems.lordsandships.scene.levelgen;

import lando.systems.lordsandships.utils.graph.Graph;

/**
 * Brian Ploeckelman created on 9/15/2014.
 */
public interface RoomGraphGenerator {

    public Graph<Room> generateRoomGraph(LevelGenParams params);


}
