/**
 * 
 */
package sheepRecog;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * The Class Tagger.
 *
 * @author Conor James Giles
 */
public class Tagger extends Task<Void> {
	
	/** The pane. */
	Pane pane;
	
	/** The image view. */
	ImageView imageView;
	
	/** The filtered image. */
	Image filteredImage;
	
	/** The image. */
	Image image;
	
	/** The threshold value. */
	double threshold;
	
	/** The array of disjoint sets. */
	ArrayDisjointSet ads;
	
	/** The list bounded of bounded disjoint sets. */
	ArrayList<Integer> bounded;
	
	/** The sheep count. */
	Label count;

    /**
	 * Instantiates a new tagger.
	 *
	 * @param pane
	 *            the pane
	 * @param imageView
	 *            the image view
	 * @param filteredImage
	 *            the filtered image
	 * @param count
	 *            the count
	 * @param threshold
	 *            the threshold
	 */
    public Tagger(Pane pane, ImageView imageView, Image filteredImage, Label count, double threshold) {
    	this.pane = pane;
    	this.imageView = imageView;
    	this.filteredImage = filteredImage;
    	this.threshold = threshold;
    	image = imageView.getImage();
    	this.count = count;
    }

    /* (non-Javadoc)
     * @see javafx.concurrent.Task#call()
     */
    @Override protected Void call() throws Exception {
    	reduce();
    	Platform.runLater(new Runnable() {
    		@Override public void run() {
    			count.setText("No. of sheep/clusters: " + pane.getChildren().filtered((x)->x.getClass()==Rectangle.class).size());
    		}});
		return null;
    }
	
	/**
	 * Pixel check function, returns whether a filtered pixel is white or black.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the boolean
	 */
	Boolean pxlCheck(int x,int y) {
		return filteredImage.getPixelReader().getArgb(x, y)!=0xff000000;
	}
	
	/**
	 * Populate the array, bind the disjoint sets, filter out outliers, and tag clusters.
	 */
	void reduce() {
		int width = (int) filteredImage.getWidth();
		int height = (int) filteredImage.getHeight();
		ads = new ArrayDisjointSet(width*height);
		for(int i=0;i<ads.getSize();i++) {
			if(pxlCheck(i%width,i/width)) {
				if(width-(i%width) > 1 && pxlCheck((i+1)%width,i/width)) {
					if(ads.get(i)<ads.get(i+1)){
						ads.set(i+1, i);
					}else{
						ads.set(i, i+1);
					}
				}
				if(height - (i/width) > 1 && pxlCheck(i%width,(i+width)/width)) {
					if(ads.get(i)<ads.get(i+width)){
						ads.set(i+width, i);
					}else{
						ads.set(i, i+width);
					}
				}
			}
			else
				ads.set(i, 0);
		}
		bounded = new ArrayList<>();
		for(int i=0;i<ads.getSize();i++)
			bound(i);
		double avg = 0;
		double max = 0;
		double min = Double.MAX_VALUE;
		FilteredList<Node> rectList = pane.getChildren().filtered((x)->x.getClass()==Rectangle.class);
		for(Node rect: rectList){
			Rectangle rct = (Rectangle) rect;
			double area = rct.getWidth()*rct.getHeight();
			avg += rct.getWidth()*rct.getHeight();
			max = Math.max(max, area);
			min = Math.min(min, area);
		}
		avg /= bounded.size();
		final double favg = avg;
		final double fmax = max;
		final double fmin = min;
		Platform.runLater(new Runnable() {
			@Override public void run() {				
				for(Node rect: rectList){
					Rectangle rct = (Rectangle) rect;
					double area = rct.getWidth()*rct.getHeight();
					if(area>(favg+fmax)/2) {
						rct.setStroke(Paint.valueOf("#0000FF"));
						Tooltip t = new Tooltip("Sheep: " + (int) Math.ceil(area/favg*clusterCount(rct.getX(),rct.getY(),rct.getWidth(),rct.getHeight())));
						Tooltip.install(rct, t);
					}
					if(area<(favg+fmin)/2)
						pane.getChildren().remove(rect);
		}}});
	}
	
	/**
	 * Draws a rectangle around a sheep.
	 *
	 * @param index
	 *            the index
	 */
	void bound(int index) {
		if(ads.get(index) != index || index==0)
			return;
		if(bounded.contains(ads.find(index)))
			return;
		int width = (int) image.getWidth();
		double imgHeight = imageView.getBoundsInParent().getHeight()/image.getHeight();
		double imgWidth = imageView.getBoundsInParent().getWidth()/image.getWidth();
		int x1 = index%width, y1 = index/width, x2 = x1, y2 = y1;
		for(int i = index,since=0; i<ads.getSize();i++) {
			if(since==imgWidth)
				break;
			if(ads.find(i)==index) {
				x1 = Math.min(i%width, x1);
				x2 = Math.max(i%width, x2);
				y1 = Math.min(i/width, y1);
				y2 = Math.max(i/width, y2);
			}
			else
				since++;
		}
		x1*=imgWidth;
		x2*=imgWidth;
		y1*=imgHeight;
		y2*=imgHeight;
		bounded.add(index);
		Rectangle r = new Rectangle();
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	pane.getChildren().add(r);
            }
        });
		r.setLayoutX(imageView.getLayoutX());
		r.setLayoutY(imageView.getLayoutY());
		r.setX(x1+imgWidth/2);
		r.setY(y1+imgHeight/2);
		r.setHeight(y2-y1);
		r.setWidth(x2-x1);
		r.setFill(Paint.valueOf("#0000"));
		r.setStroke(Paint.valueOf("#FF0000"));
		r.setVisible(true);
	}
	
	/**
	 * Counts the number of sheep in a cluster.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return the double
	 */
	double clusterCount(double x, double y, double width, double height) {
		int w = 0, t=0;
		double imgHeight = imageView.getBoundsInParent().getHeight()/image.getHeight();
		double imgWidth = imageView.getBoundsInParent().getWidth()/image.getWidth();
		int cwidth =(int) (width/imgWidth);
		int cheight =(int) (height/imgHeight);
		int cx = (int)(x/imgWidth);
		int cy = (int)(y/imgHeight);
		for(int i = cy;i<cheight+cy;i++) {
			for(int j = cx;j<cwidth+cx;j++) {
				t++;
				if(pxlCheck(j, i))
					w++;
			}
		}
		return (double) w/t;
	}
}