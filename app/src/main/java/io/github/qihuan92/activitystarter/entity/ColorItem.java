package io.github.qihuan92.activitystarter.entity;

/**
 * ColorItem
 *
 * @author qi
 * @since 2021/8/17
 */
public class ColorItem {
    private String color;
    private boolean isSelected;

    public ColorItem() {
    }

    public ColorItem(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
