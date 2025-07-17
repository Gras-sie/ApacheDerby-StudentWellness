package com.wellness.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A custom component for star rating input.
 * Allows users to select a rating from 1 to 5 stars.
 */
public class StarRatingComponent extends JPanel {
    
    private static final int STAR_COUNT = 5;
    private static final int STAR_SIZE = 30;
    private static final Color STAR_COLOR = new Color(255, 215, 0); // Gold color for stars
    
    private int rating = 0;
    private final JLabel[] stars;
    private boolean editable = true;
    
    /**
     * Creates a new star rating component.
     */
    public StarRatingComponent() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        setOpaque(false);
        
        stars = new JLabel[STAR_COUNT];
        
        for (int i = 0; i < STAR_COUNT; i++) {
            final int starValue = i + 1;
            stars[i] = createStarLabel();
            
            stars[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (editable) {
                        setRating(starValue);
                    }
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (editable) {
                        highlightStars(starValue);
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (editable) {
                        updateStars();
                    }
                }
            });
            
            add(stars[i]);
        }
        
        updateStars();
    }
    
    private JLabel createStarLabel() {
        JLabel star = new JLabel("☆");
        star.setFont(new Font("SansSerif", Font.PLAIN, STAR_SIZE));
        star.setForeground(Color.GRAY);
        star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return star;
    }
    
    private void highlightStars(int upTo) {
        for (int i = 0; i < STAR_COUNT; i++) {
            if (i < upTo) {
                stars[i].setText("★");
                stars[i].setForeground(STAR_COLOR);
            } else {
                stars[i].setText("☆");
                stars[i].setForeground(Color.GRAY);
            }
        }
    }
    
    private void updateStars() {
        highlightStars(rating);
    }
    
    /**
     * Sets the current rating.
     * 
     * @param rating The rating to set (1-5)
     * @throws IllegalArgumentException if rating is not between 1 and 5
     */
    public void setRating(int rating) {
        if (rating < 0 || rating > STAR_COUNT) {
            throw new IllegalArgumentException("Rating must be between 0 and " + STAR_COUNT);
        }
        this.rating = rating;
        updateStars();
    }
    
    /**
     * Gets the current rating.
     * 
     * @return The current rating (0-5)
     */
    public int getRating() {
        return rating;
    }
    
    /**
     * Sets whether the component is editable.
     * 
     * @param editable true to allow user interaction, false to make it read-only
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        for (JLabel star : stars) {
            star.setCursor(editable ? 
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : 
                Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Checks if the component is editable.
     * 
     * @return true if the component is editable, false otherwise
     */
    public boolean isEditable() {
        return editable;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setEditable(enabled);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
            STAR_COUNT * (STAR_SIZE + 4), 
            STAR_SIZE + 4
        );
    }
    
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
