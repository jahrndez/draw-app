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
    public int strokeSize;
    public boolean clear;

    /**
     * Normal DrawInfo; represents drawing data
     */
    public DrawInfo(GeneralPath path, Color color, int strokeSize) {
        this.type = MessageType.DRAW_INFO;
        this.path = path;
        this.color = color;
        this.strokeSize = strokeSize;
        this.clear = false;
    }

    /**
     * Clear pane DrawInfo; represents a request to clear the canvas
     */
    public DrawInfo() {
        this.type = MessageType.DRAW_INFO;
        clear = true;
    }

    public boolean isClear() {
        return clear;
    }
}
