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
	//public Point point, lastPoint1, lastPoint2;
	//public int px, py, lp1x, lp1y, lp2x, lp2y;
    public Color color;
    public int strokeSize;

    public DrawInfo(GeneralPath path, Color color, int strokeSize) {
        this.type = MessageType.DRAW_INFO;
        this.path = path;
//        px = p1.x;
//        py = p1.y;
//        lp1x = p2.x;
//        lp1y = p2.y;
//        lp2x = p3.x;
//        lp2y = p3.y;
        
        this.color = color;
        this.strokeSize = strokeSize;
    }
}
