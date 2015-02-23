package lando.systems.lordsandships.scene.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import lando.systems.lordsandships.scene.levelgen.LevelGenParams;

/**
 * Brian Ploeckelman created on 1/13/2015.
 */
public class LevelGenDialog extends Dialog {

    LevelGenParams params;

    TextField randomSeed;
    TextField mapMaxX;
    TextField mapMaxY;
    TextField roomWidthMin;
    TextField roomWidthMax;
    TextField roomHeightMin;
    TextField roomHeightMax;
    TextField numInitialRooms;
    TextField numSelectedRooms;
    TextField percentCycleEdges;

    public LevelGenDialog(String title, Skin skin) {
        super(title, skin);
        params = new LevelGenParams();
        initializeWidgets(skin);
    }

    public void update(float delta) {
        // ...
    }

    // -------------------------------------------------------------------------
    // Private Implementation
    // -------------------------------------------------------------------------

    private void initializeWidgets(Skin skin) {
        randomSeed        = new TextField("" + params.randomSeed       , skin);
        mapMaxX           = new TextField("" + params.mapMaxX          , skin);
        mapMaxY           = new TextField("" + params.mapMaxY          , skin);
        roomWidthMin      = new TextField("" + params.roomWidthMin     , skin);
        roomWidthMax      = new TextField("" + params.roomWidthMax     , skin);
        roomHeightMin     = new TextField("" + params.roomHeightMin    , skin);
        roomHeightMax     = new TextField("" + params.roomHeightMax    , skin);
        numInitialRooms   = new TextField("" + params.numInitialRooms  , skin);
        numSelectedRooms  = new TextField("" + params.numSelectedRooms , skin);
        percentCycleEdges = new TextField("" + params.percentCycleEdges, skin);

        Label randomSeedLabel        = new Label("randomSeed        ", skin);
        Label mapMaxXLabel           = new Label("mapMaxX           ", skin);
        Label mapMaxYLabel           = new Label("mapMaxY           ", skin);
        Label roomWidthMinLabel      = new Label("roomWidthMin      ", skin);
        Label roomWidthMaxLabel      = new Label("roomWidthMax      ", skin);
        Label roomHeightMinLabel     = new Label("roomHeightMin     ", skin);
        Label roomHeightMaxLabel     = new Label("roomHeightMax     ", skin);
        Label numInitialRoomsLabel   = new Label("numInitialRooms   ", skin);
        Label numSelectedRoomsLabel  = new Label("numSelectedRooms  ", skin);
        Label percentCycleEdgesLabel = new Label("percentCycleEdges ", skin);

        randomSeedLabel        .setAlignment(Align.right);
        mapMaxXLabel           .setAlignment(Align.right);
        mapMaxYLabel           .setAlignment(Align.right);
        roomWidthMinLabel      .setAlignment(Align.right);
        roomWidthMaxLabel      .setAlignment(Align.right);
        roomHeightMinLabel     .setAlignment(Align.right);
        roomHeightMaxLabel     .setAlignment(Align.right);
        numInitialRoomsLabel   .setAlignment(Align.right);
        numSelectedRoomsLabel  .setAlignment(Align.right);
        percentCycleEdgesLabel .setAlignment(Align.right);

        row(); add(randomSeedLabel);        add(randomSeed);
        row(); add(mapMaxXLabel);           add(mapMaxX);
        row(); add(mapMaxYLabel);           add(mapMaxY);
        row(); add(roomWidthMinLabel);      add(roomWidthMin);
        row(); add(roomWidthMaxLabel);      add(roomWidthMax);
        row(); add(roomHeightMinLabel);     add(roomHeightMin);
        row(); add(roomHeightMaxLabel);     add(roomHeightMax);
        row(); add(numInitialRoomsLabel);   add(numInitialRooms);
        row(); add(numSelectedRoomsLabel);  add(numSelectedRooms);
        row(); add(percentCycleEdgesLabel); add(percentCycleEdges);

        pack();
    }

}
