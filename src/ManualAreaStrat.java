import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.Color;

import georegression.struct.point.Point2D_I32;

class ManualAreaStrat extends AreaRecognitionStrategy {

	private Point2D_I32[] points = new Point2D_I32[4];
	private ContourBoundingBox bound;
    private int draggingPoint = -1;
    private int width;
    private int height;

    @Override
    public ArrayList<MatchResult> recognize(BufferedImage in, RecognitionStrategy strat) {
		ArrayList<MatchResult> res = new ArrayList<MatchResult>();
		BufferedImage norm = ImageUtil.getScaledImage(bound.getTransformedImage(in,false));
        BufferedImage flip = ImageUtil.getScaledImage(bound.getTransformedImage(in,true));
        ImageDesc id = new ImageDesc(norm,flip);
        MatchResult m = strat.getMatch(id, SettingsPanel.RECOG_THRESH/100f);
        if(m != null)
        {
            res.add(m);
        }
        return res;
    }

    private void updateBoundedZone()
	{
		ArrayList<Point2D_I32> pts = new ArrayList<Point2D_I32>();
		for(int ix=0; ix<points.length; ix++)
		{
			pts.add(new Point2D_I32((int)points[ix].x,(int)points[ix].y));
		}
		bound = new ContourBoundingBox(pts);
	}

    @Override
    public String getStratName() {
        return "manual";
    }

    @Override
    public String getStratDisplayName() {
        return "Manually Set Bounds (Legacy)";
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
		for(int i=0;i<4;i++){
			Point2D_I32 pt = points[i];
			if(Math.abs(p.x-pt.x)<=3 && Math.abs(p.y-pt.y)<=3)
			{
				draggingPoint = i;
				return;
			}
		}
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggingPoint = -1;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(draggingPoint != -1)
		{
			Point p = e.getPoint();
			if(p.x >= 0 && p.x <= this.width)
			{
				points[draggingPoint].x = p.x;
			}
			if(p.y >= 0 && p.y <= this.height)
			{
				points[draggingPoint].y = p.y;
			}
		}
		updateBoundedZone();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void draw(Graphics g) {
		drawBounds(g,0,0);
    }

    @Override
	public void init(int width, int height)
	{
        this.width = width;
        this.height = height;
		int h = (int)(height*8/10);
		int w = height*50/88;
		int x = (int)(width/2-w/2);
		int y = (int)(height/2-h/2);

		points[0] = new Point2D_I32(x,y);
		points[1] = new Point2D_I32(x+w,y);
		points[2] = new Point2D_I32(x+w,y+h);
		points[3] = new Point2D_I32(x,y+h);
		updateBoundedZone();
	}

    public void drawBounds(Graphics g,int offx, int offy)
	{
		g.setColor(Color.WHITE);
		bound.draw(g);
		for(int i=0;i<4;i++){
			Point2D_I32 p = points[i];
			if(draggingPoint == i)
			{
				g.setColor(Color.RED);
			}
			else
			{
				g.setColor(Color.WHITE);
			}
			g.fillOval(p.x-3, p.y-3, 7, 7);
		}
	}

	@Override
	public SettingsEntry getSettingsEntry() {
		return null;
	}

}