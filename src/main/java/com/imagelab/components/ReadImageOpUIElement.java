package com.imagelab.components;

import com.imagelab.components.events.OnUIElementCloneCreated;
import com.imagelab.components.events.OnUIElementDragDone;
import com.imagelab.operators.ReadImage;
import com.imagelab.views.forms.ReadImgPropertiesForm;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import static com.imagelab.utils.Constants.ANY_NODE;
import static javafx.scene.input.TransferMode.MOVE;

/**
 * Class which builds the Read image operation
 * related UI element
 */
public class ReadImageOpUIElement extends OperatorUIElement<Button, AnchorPane> implements Draggable, Cloneable {

    // Associate an control panel for each of the operator UI elements.
    // that way we can set / hide the control panel content directly inside
    // the operator element.
    //
    private final ScrollPane uiElementPropertiesPane;

    @FXML
    private Pane playground;

    public ReadImageOpUIElement(
            OnUIElementCloneCreated onCloneCreated,
            OnUIElementDragDone onDragDone,
            ScrollPane uiElementPropertiesPane
    ) {
        super(
                new ReadImage(), // To invoke openCV related logic
                ReadImage.class.getCanonicalName(),
                ReadImage.class.getSimpleName(),
                onCloneCreated,
                onDragDone,
                "readImage",
                100d,
                60d,
                true,
                true,
                false
        );
        this.uiElementPropertiesPane = uiElementPropertiesPane;
    }


    /**
     * Overridden method from the draggable interface to
     * detect if the user is dragging and UI element
     *
     * @param event - Mouse event at the time of element being dragged
     */
    @Override
    public void dragDetected(MouseEvent event) {

        // create new clone
        assert getOnCloneCreated() != null;
        try {
            if (!isCloneable()) {
                getOnCloneCreated().accept(this);
            } else {
                getOnCloneCreated().accept(this.clone());
            }
        } catch (CloneNotSupportedException e) {
            System.err.println("operator does not support dragging / cloning");
        }

        Dragboard dragboard = getNode().startDragAndDrop(MOVE);
        dragboard.setDragView(getNode().snapshot(null, null));
        ClipboardContent content = new ClipboardContent();
        content.put(ANY_NODE, "operation");
        dragboard.setContent(content);
        event.consume();

    }

    /**
     * Custom clone method to create a clone from the
     * dragged UI element.
     *
     * @return - a cloned ReadImageOpUIElement
     * @throws CloneNotSupportedException x
     */
    public OperatorUIElement<Button, AnchorPane> clone() throws CloneNotSupportedException {

        super.clone();

        final OperatorUIElement<Button, AnchorPane> copy = new ReadImageOpUIElement(
                this.getOnCloneCreated(),
                this.getOnDragDone(),
                this.uiElementPropertiesPane
        );
        copy.setCloneable(false);
        copy.setPreviewOnly(false);
        copy.setAddedToOperatorsStack(false);
        copy.buildNode();
        copy.buildForm();

        return copy;

    }

    /**
     * Method to be triggered when user clicks on a UI element
     * in the build pane.
     * <p>
     * Usage - this can be used to populate the side pane when needed
     */
    public void onClicked() {
        this.uiElementPropertiesPane.setContent(this.getForm());
        System.out.println("now this element is enabled");
    }

    /**
     * Overridden method which builds an UI element.
     */
    @Override
    public void buildNode() {

        final Button button = new Button();

        button.setId(getStylingId());
        button.setText(super.getOperatorName());
        button.prefHeight(super.getHeight());
        button.setPrefWidth(super.getWidth());
        button.setOnDragDetected(this::dragDetected);

        // source
        button.setOnDragDone((DragEvent event) -> {
            if (event.isAccepted()) {
                getOnDragDone().accept(this);
            } else {
                getOnDragDone().accept(null);
            }
        });
        button.setOnMouseClicked((event) -> {
            if (!isPreviewOnly()) {
                this.onClicked();
            }
        });

        setNode(button);

    }

    @Override
    public void buildForm() {
        ReadImage operator = (ReadImage) ReadImageOpUIElement.this.getOperator();
        setForm(new ReadImgPropertiesForm(operator));
    }

}