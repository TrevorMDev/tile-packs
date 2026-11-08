package com.tilepacks.ui.panel.editor;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.*;

import lombok.Getter;

class PreviewButton extends JButton
{
    private static final int CHECKER_SIZE = 10;

    @Getter
    private Color color;

    PreviewButton()
    {
        setContentAreaFilled(false);
    }

    @Override
    public void updateUI()
    {
        super.updateUI();
        setContentAreaFilled(false);
    }

    @Override
    public boolean isOpaque()
    {
        return false;
    }

    void setColor(Color c)
    {
        this.color = c;
        this.setText(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
        // text is white on dark colors
        this.setForeground((color.getRed() + color.getGreen() + color.getBlue() > 384) || color.getAlpha() < 130
                ? Color.BLACK : Color.WHITE);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        if (color.getAlpha() != 255)
        {
            for (int x = 0; x < getWidth(); x += CHECKER_SIZE)
            {
                for (int y = 0; y < getHeight(); y += CHECKER_SIZE)
                {
                    int val = (x / CHECKER_SIZE + y / CHECKER_SIZE) % 2;
                    g.setColor(val == 0 ? Color.LIGHT_GRAY : Color.WHITE);
                    g.fillRect(x, y, CHECKER_SIZE, CHECKER_SIZE);
                }
            }
        }

        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());

        super.paintComponent(g);
    }
}
