package interfaces;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.io.Serializable;

/**
 * Represents the basic unit of 2D information sent over the network. One stroke likely consists of multiple
 * DrawInfo objects.
 */
public class DrawInfo extends LobbyMessage implements Serializable {
    public GeneralPath path;
    public Color color;
    public Stroke stroke;
    public boolean clear;

    /**
     * Normal DrawInfo; represents drawing data
     */
    public DrawInfo(GeneralPath path, Color color, Stroke stroke) {
        this.type = MessageType.DRAW_INFO;
        this.path = path;
        this.color = color;
        this.stroke = stroke;
        this.clear = false;
    }

    /**
     * Clear pane DrawInfo; represents a request to clear the canvas
     */
    public DrawInfo(Color color) {
        this.color = color;
        clear = true;
    }

    public boolean isClear() {
        return clear;
    }
}
