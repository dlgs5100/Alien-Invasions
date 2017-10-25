import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

class Pos {
    private double x;
    private double y;    
    public Pos(){}
    public Pos(double xx, double yy) {
        x = xx;
        y = yy;
    }
    public void setX(double xx) {
        x = xx;
    }
    public void setY(double yy) {
        y = yy;
    }
    public double getX() {return x;}
    public double getY() {return y;}
}

public class Character {
    double vx=0, vy=0;
    double lbnd, rbnd, tbnd, bbnd;
    private ObjectProperty<Pos> pos = new SimpleObjectProperty<Pos>(new Pos());
    Image[] frame;
    int curFrame;
    int durs[];
    long lastRemainTime = 0;
    ImageView iv = new ImageView();
    boolean valid;

    public Character(String fname, int n_frames, int[] ds) {
        String fn;
        frame = new Image[n_frames];
        durs = new int[n_frames];
        for (int i=0; i<n_frames; i++) {
            fn = fname + i + ".png";
            frame[i] = new Image(fn);
            this.durs[i] = ds[i];
        }
        curFrame = (int)(n_frames*Math.random());
        iv.xProperty().set(getX());
        iv.yProperty().set(getY());
        iv.setImage(frame[curFrame]);
        valid = true;
    }

    public ObjectProperty<Pos> posProperty() {
        return pos;
    }

    public void setBnd(double lb, double rb, double tb, double bb) {
        lbnd = lb;
        rbnd = rb;
        tbnd = tb;
        bbnd = bb;
    }

    public void setVx(double v) {
        vx = v;
        if (v<0)
            iv.setScaleX(-1); // mirror the image horizontally on reaching the left boundary
        else
            iv.setScaleX(1); // mirror the image horizontally on reaching the left boundary
    }

    public void setVy(double v) {
        vy = v;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    protected void updateCurFrameIdx(long elapsedTime) {
        long t = elapsedTime + lastRemainTime;
        while (t>durs[curFrame]) {
            t -= durs[curFrame];
            curFrame = (curFrame+1)%frame.length;
        }
        lastRemainTime = t;
    }

    public void update(long elapsedTime) {
        double newx = getX()+vx*elapsedTime/1000;
        double newy = getY()+vy*elapsedTime/1000;
        if (newx<0)
            setPos(0, newy);
        else if (newx>rbnd-getFrame().getWidth())
            setPos(rbnd-getFrame().getWidth(), newy);
        else
            setPos(newx, newy);
        updateCurFrameIdx(elapsedTime);
        if (curFrame>=0)
            iv.setImage(frame[curFrame]);
    }

    public Image getFrame() {
        return frame[curFrame];
    }

    public double getWidth() {
        return frame[curFrame].getWidth();
    }

    public double getHeight() {
        return frame[curFrame].getHeight();
    }

    public ImageView getView() {
        return iv;
    }

    public void setPos(double x, double y) {
        pos.set(new Pos(x, y)); // this statement would trigger the change listener
        iv.setX(x);
        iv.setY(y);
    }

    public double getX() {
        return pos.get().getX();
    }

    public double getY() {
        return pos.get().getY();
    }

    public boolean collideWith(Character a) {
        double left1 = getX();
        double right1 = getX() + getWidth();
        double top1 = getY();
        double bottom1 = getY() + getHeight();
        double left2 = a.getX();
        double right2 = a.getX() + a.getWidth();
        double top2 = a.getY();
        double bottom2 = a.getY() + a.getHeight();
        if (Math.max(left1, left2)<Math.min(right1, right2)
            && Math.max(top1, top2)<Math.min(bottom1, bottom2))
            return true;
        else
            return false;
    }
}