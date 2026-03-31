package dev.alexco.minecraft.blaze2d.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import dev.alexco.minecraft.blaze2d.menu.button.Button;
import dev.alexco.minecraft.blaze2d.menu.button.TextField;

import java.util.ArrayList;
import java.util.List;

public class GridLayout {
    private final List<GridElement> elements = new ArrayList<>();
    private final int columns;
    private final float elementWidth;
    private final float elementHeight;
    private final float horizontalSpacing;
    private final float verticalSpacing;
    private final float topMargin;
    private final float scale;

    public GridLayout(int columns, float elementWidth, float elementHeight,
                      float horizontalSpacing, float verticalSpacing,
                      float topMargin, float scale) {
        this.columns = columns;
        this.elementWidth = elementWidth;
        this.elementHeight = elementHeight;
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;
        this.topMargin = topMargin;
        this.scale = scale;
    }

    public void addButton(Button button) {
        elements.add(button);
        updateLayout();
    }

    public void addTextField(TextField textField) {
        elements.add(textField);
        updateLayout();
    }

    public void addElement(GridElement element) {
        elements.add(element);
        updateLayout();
    }

    /**
     * Recomputes grid cell positions and sizes based on current screen dimensions.
     */
    private void updateLayout() {
        if (elements.isEmpty()) return;

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        float totalGridWidth = (columns * elementWidth * scale) + ((columns - 1) * horizontalSpacing);
        float startX = (screenWidth - totalGridWidth) / 2f;

        int rows = (int) Math.ceil((double) elements.size() / columns);
        float startY = screenHeight - topMargin - (elementHeight * scale);

        for (int i = 0; i < elements.size(); i++) {
            int row = i / columns;
            int col = i % columns;

            float x = startX + (col * (elementWidth * scale + horizontalSpacing));
            float y = startY - (row * (elementHeight * scale + verticalSpacing));

            GridElement element = elements.get(i);
            element.setPosition(x, y);
            element.setSize(elementWidth * scale, elementHeight * scale);
        }
    }

    /**
     * Renders all registered grid elements with their specific widget renderer.
     */
    public void render(SpriteBatch batch, BitmapFont font) {
        updateLayout();

        for (GridElement element : elements) {
            if (element instanceof Button) {
                ((Button) element).render(batch, font);
            } else if (element instanceof TextField) {
                ((TextField) element).render(batch, font);
            }
        }
    }

    /**
     * Renders tooltip overlays for text fields currently in the grid.
     */
    public void renderTooltips(SpriteBatch batch, BitmapFont font) {
        for (GridElement element : elements) {
            if (element instanceof TextField) {
                ((TextField) element).renderTooltip(batch, font);
            }
        }
    }

    public void renderLabels(SpriteBatch batch, BitmapFont font) {
        for (GridElement element : elements) {
            if (element instanceof TextField) {
                ((TextField) element).renderLabel(batch, font);
            }
        }
    }

    public void update() {
        for (GridElement element : elements) {
            element.update();
        }
    }

    /**
     * Advances cursor blink and text-field local state each frame.
     */
    public void updateTextFields(float delta) {
        for (GridElement element : elements) {
            if (element instanceof TextField) {
                ((TextField) element).update(delta);
            }
        }
    }

    public void dispose() {
        elements.clear();
    }

    public List<Button> getButtons() {
        List<Button> buttons = new ArrayList<>();
        for (GridElement element : elements) {
            if (element instanceof Button) {
                buttons.add((Button) element);
            }
        }
        return buttons;
    }

    public List<TextField> getTextFields() {
        List<TextField> textFields = new ArrayList<>();
        for (GridElement element : elements) {
            if (element instanceof TextField) {
                textFields.add((TextField) element);
            }
        }
        return textFields;
    }

    public List<GridElement> getElements() {
        return new ArrayList<>(elements);
    }

    public void clear() {
        elements.clear();
    }
}
