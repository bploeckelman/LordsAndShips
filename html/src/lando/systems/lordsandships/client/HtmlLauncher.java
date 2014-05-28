package lando.systems.lordsandships.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.utils.Constants;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(Constants.win_width, Constants.win_height);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new LordsAndShips();
        }
}