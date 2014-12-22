package lando.systems.lordsandships.scene.ui;

/**
 * Brian Ploeckelman created on 12/21/2014.
 */
public class CVar {

    final String key;
    String value;

    public CVar(String key) {
        this(key, "");
    }

    public CVar(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

}
