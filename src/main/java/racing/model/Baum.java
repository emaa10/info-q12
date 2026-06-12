package racing.model;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class Baum extends StackPane {

    private ImageView imageView;

    public Baum() {
        Image image = new Image(getClass().getResourceAsStream("/images/tree.png"));
        imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        this.getChildren().add(imageView);
        this.setPickOnBounds(true);
    }
}