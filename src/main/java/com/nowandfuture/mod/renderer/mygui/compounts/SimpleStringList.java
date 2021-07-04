package com.nowandfuture.mod.renderer.mygui.compounts;

import com.nowandfuture.mod.renderer.mygui.LayoutParameter;
import com.nowandfuture.mod.renderer.mygui.RootView;
import com.nowandfuture.mod.renderer.mygui.ViewGroup;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SimpleStringList extends MyAbstractList<MyAbstractList.ViewHolder> {

    public SimpleStringList(@Nonnull RootView rootView) {
        super(rootView);
    }

    public SimpleStringList(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    protected void onItemClicked(int index, int x, int y, int button) {
        super.onItemClicked(index, x, y, button);
    }

    @Override
    protected void drawSplitLine() {
    }

    public static class StringViewHolder extends ViewHolder {
        private TextView textView;

        public StringViewHolder(@Nonnull RootView rootView, MyAbstractList parent) {
            super(rootView, parent);
            init();
        }

        public StringViewHolder(@Nonnull RootView rootView, MyAbstractList parent, @Nonnull LayoutParameter layoutParameter) {
            super(rootView, parent, layoutParameter);
            init();
        }

        private void init() {
            textView = new TextView(getRoot(), this);
            textView.setWidth(80);
            textView.setHeight(12);
            textView.setX(0);
            textView.setY(0);
            textView.setClickable(false);
            addChild(textView);
        }

        public void setString(String string) {
            textView.setText(string);
        }
    }

    public static class StringAdapter extends Adapter<StringViewHolder> {

        private List<String> strings;

        public StringAdapter() {
            strings = new ArrayList<>();
        }

        public void setStrings(List<String> strings) {
            this.strings = strings;
        }

        @Override
        public int getSize() {
            return strings.size();
        }

        @Override
        public int getHeight() {
            return 10;
        }

        @Override
        public StringViewHolder createHolder(RootView rootView, MyAbstractList parent) {
            return new StringViewHolder(rootView, parent);
        }

        @Override
        public void handle(MyAbstractList list, StringViewHolder viewHolder, int index) {
            viewHolder.setString(strings.get(index));
        }
    }
}
