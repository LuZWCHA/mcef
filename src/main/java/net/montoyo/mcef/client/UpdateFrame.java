package net.montoyo.mcef.client;

import net.montoyo.mcef.utilities.IProgressListener;
import net.montoyo.mcef.utilities.Log;
import net.montoyo.mcef.utilities.Util;

import javax.swing.*;
import java.awt.*;

public class UpdateFrame extends JFrame implements IProgressListener {
    
    private JLabel label = new JLabel("Preparing...");
    private JProgressBar pbar = new JProgressBar();

    public UpdateFrame() {
        setTitle("Minecraft ChromiumEF");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel lpane = new JPanel();
        lpane.setLayout(new BoxLayout(lpane, BoxLayout.LINE_AXIS));
        lpane.add(label);
        lpane.add(Box.createHorizontalGlue());
        
        Dimension dim = new Dimension(5, 5);
        JPanel pane = new JPanel();
        
        pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        pane.add(lpane);
        pane.add(new Box.Filler(dim, dim, dim));
        pane.add(pbar);
        
        setContentPane(pane);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch(Throwable t) {
            Log.info("Note: couldn't set system look & feel.");
        }
        
        setVisible(true);
        
        dim = new Dimension(50, 26);
        pbar.setMinimumSize(dim);
        pbar.setPreferredSize(dim);
        
        setMinimumSize(new Dimension(540, 90));
        pack();
    }

    @Override
    public void onError(String task, Throwable d) {

    }

    @Override
    public void onProgressed(double d) {
        int val = (int) Util.clamp(d, 0.d, 100.d);
        pbar.setValue(val);
    }

    @Override
    public void onTaskChanged(String name) {
        Log.info("Task changed to \"%s\"", name);
        label.setText(name);
    }

    @Override
    public void onProgressEnd() {
        dispose();
    }

}
