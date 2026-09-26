package algeo.gui.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * Menyesuaikan tampilan komponen Swing bawaan (scrollbar, dropdown, slider) dengan tema aplikasi.
 */
public final class ThemedUI {

    private ThemedUI() {}

    // ===================== scrollbar =====================

    public static void gayakan(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new ScrollBarPink());
        scroll.getHorizontalScrollBar().setUI(new ScrollBarPink());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 12));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(12, 12));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getHorizontalScrollBar().setOpaque(false);
    }

    /** Scrollbar tanpa tombol panah, pegangan pink bulat. */
    private static class ScrollBarPink extends BasicScrollBarUI {

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return tombolKosong();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return tombolKosong();
        }

        private static JButton tombolKosong() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.haluskan(g2);
            g2.setColor(new Color(251, 201, 220, 150));
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.haluskan(g2);
            g2.setColor(isThumbRollover() ? Theme.PINK_GARIS : Theme.PINK_TUA);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    // ===================== dropdown =====================

    public static void gayakan(JComboBox<?> combo) {
        combo.setUI(new ComboPink());
        combo.setFont(Theme.BIASA_TEBAL);
        combo.setForeground(Theme.TEKS);
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(Theme.PINK_GARIS, 2));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean dipilih, boolean fokus) {
                super.getListCellRendererComponent(list, value, index, dipilih, fokus);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                setFont(Theme.BIASA_TEBAL);
                setBackground(dipilih ? Theme.PINK : Color.WHITE);
                setForeground(dipilih ? Color.WHITE : Theme.TEKS);
                return this;
            }
        });
    }

    /** Dropdown dengan tombol panah kotak pink. */
    private static class ComboPink extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    Theme.haluskan(g2);
                    g2.setColor(getModel().isRollover() ? Theme.PINK_TUA : Theme.PINK);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.setColor(Color.WHITE);
                    g2.fillPolygon(new int[] {cx - 5, cx + 5, cx}, new int[] {cy - 2, cy - 2, cy + 4}, 3);
                    g2.dispose();
                }
            };
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setRolloverEnabled(true);
            b.setPreferredSize(new Dimension(24, 24));
            return b;
        }
    }

    // ===================== slider =====================

    public static void gayakan(JSlider slider) {
        slider.setUI(new SliderPink(slider));
        slider.setOpaque(false);
        slider.setPreferredSize(new Dimension(150, 26));
    }

    /** Slider dengan jalur pink dan pegangan bintang kuning. */
    private static class SliderPink extends BasicSliderUI {

        SliderPink(JSlider s) {
            super(s);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(20, 20);
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.haluskan(g2);
            int y = trackRect.y + trackRect.height / 2 - 4;
            g2.setColor(Theme.PINK_MUDA);
            g2.fillRoundRect(trackRect.x, y, trackRect.width, 8, 8, 8);
            g2.setColor(Theme.PINK_TUA);
            g2.fillRoundRect(trackRect.x, y, thumbRect.x + thumbRect.width / 2 - trackRect.x, 8, 8, 8);
            g2.dispose();
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.haluskan(g2);
            Shape s = Theme.bintang(thumbRect.getCenterX(), thumbRect.getCenterY(), 10, 4.5);
            g2.setColor(Theme.KUNING);
            g2.fill(s);
            g2.setColor(Theme.KUNING_TUA);
            g2.draw(s);
            g2.dispose();
        }

        @Override
        public void paintFocus(Graphics g) {
            // tidak menggambar kotak fokus bawaan
        }
    }
}
