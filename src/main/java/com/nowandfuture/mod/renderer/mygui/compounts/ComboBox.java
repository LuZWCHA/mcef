package com.nowandfuture.mod.renderer.mygui.compounts;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.nowandfuture.mod.renderer.mygui.RootView;
import com.nowandfuture.mod.renderer.mygui.ViewGroup;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ComboBox extends ViewGroup {

    private MyLabel label;
    private SimpleStringList simpleStringList;
    private SimpleStringList.StringAdapter stringAdapter = new SimpleStringList.StringAdapter();
    private List<String> contents;
    private int selectIndex = -1;

    private int labelHeight = 10;
    private int listHeight = 10;

    private Consumer<Integer> onItemClicked;

    public ComboBox(RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        setup();
    }

    public ComboBox(@Nonnull RootView rootView) {
        super(rootView);
        setup();
    }

    private void setup() {
        label = new MyLabel(getRoot().getFontRenderer(), 0, 0, getWidth(), labelHeight, colorInt(255, 255, 255, 255));
        simpleStringList = new SimpleStringList(getRoot(), this);
        addChild(simpleStringList);

        contents = new ArrayList<>();

        simpleStringList.bind(stringAdapter);
        simpleStringList.setVisible(false);

        simpleStringList.setOnItemClick(new MyAbstractList.OnItemClickedListener() {
            @Override
            public void onItemClicked(MyAbstractList view, int index, int button) {
                setIndex(index);
                closeList();
                if (onItemClicked != null) {
                    onItemClicked.accept(index);
                }
            }
        });
        setClipping(false);
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }

    @Override
    protected void onLoad() {
        simpleStringList.setX(0);
        simpleStringList.setY(labelHeight);
        simpleStringList.setHeight(listHeight);

        stringAdapter.setStrings(contents);

        label.setX(0);
        label.setY(0);
        label.setHeight(labelHeight);
        label.setBackColor(colorInt(20, 20, 20, 200));
        label.setBorderWidth(2);
        label.setBorderColor(colorInt(80, 80, 80, 255));
        label.setHorizontalCenter(true);

        setHeight(labelHeight + listHeight);
        super.onLoad();
    }

    @Override
    protected void onLayout(int parentWidth, int parentHeight) {

    }

    public void setIndex(int index) {
        if (index >= 0 && index < contents.size()) {
            selectIndex = index;
            label.setFirst(contents.get(index));
        }
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public void setShowString(String string) {
        label.setFirst(string);
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        label.draw(stack, mouseX, mouseY, partialTicks);
    }

    public void showList() {
        simpleStringList.setVisible(true);
        this.setHeight(listHeight + labelHeight);
    }

    public void closeList() {
        simpleStringList.setVisible(false);
        this.setHeight(labelHeight);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        if (isLabelClicked(mouseX, mouseY)) {
            if (!simpleStringList.isVisible())
                this.showList();
            else
                this.closeList();
            return true;
        }
        return false;
    }

    private boolean isLabelClicked(int mouseX, int mouseY) {
        return mouseY <= labelHeight;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return onClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return isLabelClicked(mouseX, mouseY);
    }

    //don't use it as far as possible
    @Override
    public void setHeight(int height) {
        super.setHeight(height);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        label.setWidth(width);
        simpleStringList.setWidth(width);
    }

    public void setLabelHeight(int labelHeight) {
        this.labelHeight = labelHeight;
        label.setHeight(labelHeight);
        simpleStringList.setY(labelHeight);
        if (simpleStringList.isVisible())
            setHeight(labelHeight + listHeight);
        else
            setHeight(labelHeight);
    }

    public void setListHeight(int listHeight) {
        this.listHeight = listHeight;
        simpleStringList.setHeight(listHeight);
        if (simpleStringList.isVisible())
            setHeight(labelHeight + listHeight);
        else
            setHeight(labelHeight);
    }

    public void setOnItemClicked(Consumer<Integer> onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

    public void setLabelColor(int back, int border) {
        label.setBackColor(back);
        label.setBorderColor(border);
    }
}
