package lando.systems.lordsandships.scene.ui;

/**
 * Brian Ploeckelman created on 12/22/2014.
 */
public class CCmd {

    final String command;
    Function function;

    public CCmd(String command, Function function) {
        this.command = command;
        this.function = function;
    }

    public interface Function {
        public Object invoke(Object... params);
    }

}
