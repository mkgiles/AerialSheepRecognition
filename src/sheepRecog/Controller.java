package sheepRecog;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

/**
 * The Class Controller.
 */
public class Controller {
	
	/** The FXML file for the GUI. */
	FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
	
	/** The file loaded. */
	protected File file;
	
	/** The image loaded. */
	protected Image image;
	
	/** The filtered image. */
	protected Image filteredImage;
	
	/** The pixel reader. */
	protected PixelReader pr;
	
	/** The pane. */
	@FXML protected AnchorPane pane;
	
	/** The image view. */
	@FXML protected ImageView imageView;
	
	/** The sheep count. */
	@FXML protected Label count;
	
	/** The threshold slider. */
	@FXML protected Slider thresh;
	
	/** The id button. */
	@FXML protected Button ID;
	
	/** The tagger task. */
	protected Tagger tagger;
	
	/**
	 * Exit.
	 */
	@FXML protected void exit() {
		System.exit(0);
	}
	
	/**
	 * Open file.
	 *
	 * @param event
	 *            the event
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@FXML protected void openFile(ActionEvent event) throws IOException {
	    FileChooser chooser = new FileChooser();
	    chooser.setTitle("Open File");
	    file = chooser.showOpenDialog(((MenuItem) event.getTarget()).getParentPopup().getScene().getWindow());
	    image = new Image(file.toURI().toString());
	    pr = image.getPixelReader();
		imageView.setImage(image);
		imageView.setOnContextMenuRequested((ContextMenuEvent e) -> iVContext(e));
		pane.getChildren().removeIf((x) -> x.getClass() == Rectangle.class);
	}
	
	/**
	 * Change view between original image and filtered image.
	 *
	 * @param event
	 *            the event
	 */
	@FXML protected void changeView(ActionEvent event) {
		switch(((MenuItem) event.getSource()).getText()) {
			case "Filter":
				filteredImage = filter(image);
				imageView.setImage(filteredImage);
				break;
			default:
				imageView.setImage(image);
				break;
		}
	}
	
	/**
	 * ImageView context menu.
	 *
	 * @param event
	 *            the event
	 */
	@FXML protected void iVContext(ContextMenuEvent event) {
		ContextMenu cm = new ContextMenu();
		ImageView iv = imageView;
		MenuItem mi = new MenuItem("Filter");
		mi.setOnAction((ActionEvent e)->changeView(e));
		cm.getItems().add(mi);
		mi = new MenuItem("Original");
		mi.setOnAction((ActionEvent e)->changeView(e));
		cm.getItems().add(mi);
		cm.show(iv, event.getScreenX(), event.getScreenY());
	}
	
	/**
	 * Filter the image using a low pass chroma-key filter, and a high pass gamma filter.
	 *
	 * @param img
	 *            the img
	 * @return the image
	 */
	protected Image filter(Image img) {
		WritableImage output = new WritableImage((int)img.getWidth(),(int)img.getHeight());
		PixelWriter pw = output.getPixelWriter();
		for(int x=0; x<img.getWidth(); x++) {
			for(int y=0; y<img.getHeight(); y++) {
				int pxl = pr.getArgb(x, y);
				if(((pxl>>8)&0xff) > ((pxl>>16)&0xff) && ((pxl>>8)&0xff) > (pxl&0xff) && ((pxl>>16)&0xff) > 0x3B && ((pxl>>8)&0xff) > 0x3B && (pxl&0xff) > 0x3B)
					pxl = 0xff000000;
				else
					pxl &= 0xffff00ff;
				double pxlgamma = .299*((pxl>>16)&0xff) + .114*(pxl&0xff);
				if(pxlgamma < thresh.getValue()/2.3)
					pxl = 0xff000000;
				else
					pxl=0xffffffff;
				pw.setArgb(x, y, pxl);
			}
		}
		return output;
	}
	
	/**
	 * Run the tagger task.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@FXML void id() throws Exception {
		pane.getChildren().removeIf((x)->x.getClass()==Rectangle.class);
		filteredImage = filter(image);
		tagger = new Tagger(pane, imageView, filteredImage, count, thresh.getValue());
		Thread thread = new Thread(tagger);
		thread.setDaemon(true);
		thread.start();
	}
}
