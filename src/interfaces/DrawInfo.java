package interfaces;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * Represents the basic unit of 2D information sent over the network. One stroke likely consists of multiple
 * DrawInfo objects.
 */
public class DrawInfo extends LobbyMessage {
    public GeneralPath path;
    public Color color;
    public int strokeSize;

    public DrawInfo(GeneralPath path, Color color, int strokeSize) {
        this.type = MessageType.DRAW_INFO;
        this.path = path;
        this.color = color;
        this.strokeSize = strokeSize;
    }
}
