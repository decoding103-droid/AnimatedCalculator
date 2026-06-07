import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Timer;
import java.util.TimerTask;

public class AdditionCalculator extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    static final Color BG_OUTER   = new Color(0xC8C4BE);
    static final Color BODY_TOP   = new Color(0x606060);
    static final Color BODY_BOT   = new Color(0x383838);
    static final Color DISP_BG    = new Color(0x111111);
    static final Color BTN_GREEN  = new Color(0x5DBB4A);
    static final Color BTN_YELLOW = new Color(0xF0C040);
    static final Color BTN_PINK   = new Color(0xE0629A);
    static final Color BTN_BLUE   = new Color(0x4FA8D0);
    static final Color BTN_ORANGE = new Color(0xF07840);
    static final Color BTN_RED    = new Color(0xD94040);

    private String inputA = "";
    private String inputB = "";
    private boolean enteringB = false;

    private FlipDisplay dispA, dispB, dispResult;
    private JLabel statusLbl;

    public AdditionCalculator() {
        setTitle("+ Addition Machine");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // ── Outer desktop panel ───────────────────────────────────────────
        JPanel desktop = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,new Color(0xD0CCC6),0,getHeight(),new Color(0xB8B4AE)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        desktop.setPreferredSize(new Dimension(500, 660));

        // ── Main calculator body ──────────────────────────────────────────
        JPanel body = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // shadow
                for(int i=6;i>0;i--){
                    g2.setColor(new Color(0,0,0,12));
                    g2.fillRoundRect(i,i,w-i+4,h-i+4,22,22);
                }
                // body fill
                g2.setPaint(new GradientPaint(0,0,BODY_TOP,0,h,BODY_BOT));
                g2.fillRoundRect(0,0,w-4,h-4,20,20);
                // top sheen
                g2.setPaint(new GradientPaint(0,0,new Color(255,255,255,50),0,h/4,new Color(255,255,255,0)));
                g2.fillRoundRect(0,0,w-4,h/4,20,20);
                // screws
                int[][] sc = {{12,12},{w-24,12},{12,h-24},{w-24,h-24}};
                for(int[] s:sc){
                    g2.setColor(new Color(0x202020)); g2.fillOval(s[0],s[1],10,10);
                    g2.setColor(new Color(255,255,255,50)); g2.fillOval(s[0]+2,s[1]+2,4,4);
                    g2.setColor(new Color(0,0,0,80)); g2.drawOval(s[0],s[1],9,9);
                }
            }
        };
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setPreferredSize(new Dimension(420, 600));
        body.setBorder(BorderFactory.createEmptyBorder(28,28,28,28));

        // ── Display row A + B ─────────────────────────────────────────────
        dispA      = new FlipDisplay(4, "A");
        dispB      = new FlipDisplay(4, "B");
        dispResult = new FlipDisplay(5, "SUM");

        JPanel dispRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        dispRow.setOpaque(false);
        dispRow.setMaximumSize(new Dimension(380, 80));

        JLabel plusLbl = styledLabel("+", 28, BTN_YELLOW);
        dispRow.add(dispA);
        dispRow.add(plusLbl);
        dispRow.add(dispB);

        // ── Equals row ────────────────────────────────────────────────────
        JPanel eqRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        eqRow.setOpaque(false);
        eqRow.setMaximumSize(new Dimension(380, 26));
        JLabel eqBar = styledLabel("────────────────", 12, new Color(100,100,100));
        JLabel eqSign = styledLabel("  =  ", 20, new Color(160,160,160));
        eqRow.add(eqBar);
        eqRow.add(eqSign);

        // ── Result display row ────────────────────────────────────────────
        JPanel resRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        resRow.setOpaque(false);
        resRow.setMaximumSize(new Dimension(380, 90));
        resRow.add(dispResult);

        // ── Status label ──────────────────────────────────────────────────
        statusLbl = styledLabel("Enter first number", 12, new Color(160,160,160));
        statusLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusRow.setOpaque(false);
        statusRow.setMaximumSize(new Dimension(380,30));
        statusRow.add(statusLbl);

        // ── Keypad ────────────────────────────────────────────────────────
        JPanel keypad = buildKeypad();
        keypad.setMaximumSize(new Dimension(380, 280));
        keypad.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Assemble body ─────────────────────────────────────────────────
        body.add(Box.createVerticalStrut(10));
        body.add(dispRow);
        body.add(Box.createVerticalStrut(6));
        body.add(eqRow);
        body.add(Box.createVerticalStrut(4));
        body.add(resRow);
        body.add(Box.createVerticalStrut(4));
        body.add(statusRow);
        body.add(Box.createVerticalStrut(12));
        body.add(keypad);
        body.add(Box.createVerticalGlue());

        desktop.add(body);
        setContentPane(desktop);
        pack();
        setLocationRelativeTo(null);
        updateStatus();
        setVisible(true);
    }

    // ── Keypad builder ────────────────────────────────────────────────────────
    private JPanel buildKeypad() {
        JPanel kp = new JPanel(new GridBagLayout());
        kp.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.fill   = GridBagConstraints.BOTH;
        c.ipadx  = 10; c.ipady = 8;

        Color[] colors = {BTN_BLUE,BTN_GREEN,BTN_YELLOW,BTN_PINK,BTN_ORANGE};

        String[][] grid = {
                {"7","8","9","C"},
                {"4","5","6","+"},
                {"1","2","3","="},
                {null,"0",null,"="}
        };
        Color[][] gridColors = {
                {BTN_BLUE,   BTN_GREEN,  BTN_YELLOW, BTN_RED},
                {BTN_PINK,   BTN_ORANGE, BTN_BLUE,   BTN_YELLOW},
                {BTN_GREEN,  BTN_PINK,   BTN_ORANGE, BTN_GREEN},
                {null,       BTN_BLUE,   null,        BTN_GREEN}
        };

        // Place digit buttons
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                String lbl = grid[row][col];
                Color  clr = gridColors[row][col];
                c.gridx = col; c.gridy = row;
                c.gridwidth = 1; c.gridheight = 1;
                KeyButton btn = new KeyButton(lbl, clr);
                btn.setPreferredSize(new Dimension(72, 58));
                final String d = lbl;
                if (lbl.matches("[0-9]")) btn.addActionListener(e -> onDigit(d));
                else if (lbl.equals("C"))  btn.addActionListener(e -> onClear());
                else if (lbl.equals("+"))  btn.addActionListener(e -> onNext());
                else if (lbl.equals("="))  btn.addActionListener(e -> onEquals());
                kp.add(btn, c);
            }
        }
        // 0 key (col 1, row 3)
        c.gridx=1; c.gridy=3; c.gridwidth=1; c.gridheight=1;
        KeyButton btn0 = new KeyButton("0", BTN_BLUE);
        btn0.setPreferredSize(new Dimension(72,58));
        btn0.addActionListener(e -> onDigit("0"));
        kp.add(btn0, c);

        // = key tall (col 3, rows 2-3) — override row 2 = and add spanning one
        // Remove old = from row2,col3 by not adding and re-adding spanning
        // We need to redo: rows 0-2 cols 0-3 normally except = is rows 2-3 col 3
        // Simpler: rebuild with explicit placement
        kp.removeAll();
        String[]  lbls   = {"7","8","9","C", "4","5","6","+", "1","2","3", "0"};
        Color[]   clrs   = {BTN_BLUE,BTN_GREEN,BTN_YELLOW,BTN_RED,
                BTN_PINK,BTN_ORANGE,BTN_BLUE,BTN_YELLOW,
                BTN_GREEN,BTN_PINK,BTN_ORANGE, BTN_BLUE};
        int[][]   positions = {
                {0,0},{1,0},{2,0},{3,0},
                {0,1},{1,1},{2,1},{3,1},
                {0,2},{1,2},{2,2},
                {1,3}
        };
        for (int i=0;i<lbls.length;i++) {
            c.gridx=positions[i][0]; c.gridy=positions[i][1];
            c.gridwidth=1; c.gridheight=1;
            KeyButton btn = new KeyButton(lbls[i], clrs[i]);
            btn.setPreferredSize(new Dimension(72,58));
            final String d = lbls[i];
            if (d.matches("[0-9]")) btn.addActionListener(e2 -> onDigit(d));
            else if (d.equals("C"))  btn.addActionListener(e2 -> onClear());
            else if (d.equals("+"))  btn.addActionListener(e2 -> onNext());
            kp.add(btn, c);
        }
        // = button spans rows 2-3
        c.gridx=3; c.gridy=2; c.gridwidth=1; c.gridheight=2;
        KeyButton eqBtn = new KeyButton("=", BTN_GREEN);
        eqBtn.setPreferredSize(new Dimension(72, 130));
        eqBtn.addActionListener(e2 -> onEquals());
        kp.add(eqBtn, c);

        return kp;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────
    private void onDigit(String d) {
        if (!enteringB) {
            if (inputA.length() < 4) { inputA += d; dispA.setValue(inputA); }
        } else {
            if (inputB.length() < 4) { inputB += d; dispB.setValue(inputB); }
        }
        updateStatus();
    }
    private void onNext() {
        if (!inputA.isEmpty() && !enteringB) { enteringB = true; updateStatus(); }
    }
    private void onEquals() {
        if (!inputA.isEmpty() && !inputB.isEmpty()) {
            int sum = Integer.parseInt(inputA) + Integer.parseInt(inputB);
            dispResult.animateValue(String.valueOf(sum));
            statusLbl.setText(inputA + " + " + inputB + " = " + sum);
            statusLbl.setForeground(BTN_GREEN.brighter());
        }
    }
    private void onClear() {
        inputA=""; inputB=""; enteringB=false;
        dispA.setValue(""); dispB.setValue(""); dispResult.setValue("");
        statusLbl.setForeground(new Color(160,160,160));
        updateStatus();
    }
    private void updateStatus() {
        if (!enteringB) { statusLbl.setText("Enter first number, then press  +"); statusLbl.setForeground(new Color(160,160,160)); }
        else            { statusLbl.setText("Enter second number, then press  ="); statusLbl.setForeground(BTN_YELLOW.darker()); }
    }

    private JLabel styledLabel(String t, int sz, Color c) {
        JLabel l = new JLabel(t); l.setFont(new Font("Courier New", Font.BOLD, sz)); l.setForeground(c); return l;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch(Exception ignored){}
        SwingUtilities.invokeLater(AdditionCalculator::new);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // FlipDisplay
    // ═════════════════════════════════════════════════════════════════════════
    static class FlipDisplay extends JComponent {
        private final int  numDigits;
        private final String tag;
        private String[]  chars;
        private boolean[] flipping;
        private int[]     flipStep;
        private Timer     timer;

        FlipDisplay(int n, String tag) {
            this.numDigits = n; this.tag = tag;
            chars    = new String[n]; flipping = new boolean[n]; flipStep = new int[n];
            for(int i=0;i<n;i++) chars[i]=" ";
            setPreferredSize(new Dimension(n*34+6, 72));
            setMinimumSize(getPreferredSize());
        }
        void setValue(String v) {
            for(int i=0;i<numDigits;i++){
                int ci = i-(numDigits-v.length());
                chars[i]=(ci>=0&&ci<v.length())?String.valueOf(v.charAt(ci)):" ";
            }
            repaint();
        }
        void animateValue(String v) {
            if(timer!=null) timer.cancel();
            timer = new Timer();
            final int[] step={0};
            timer.scheduleAtFixedRate(new TimerTask(){
                @Override public void run(){
                    step[0]++;
                    for(int i=0;i<numDigits;i++){
                        int ci=i-(numDigits-v.length());
                        int settle=i*3+10;
                        if(step[0]>settle){
                            chars[i]=(ci>=0&&ci<v.length())?String.valueOf(v.charAt(ci)):" ";
                            flipping[i]=false;
                        } else {
                            chars[i]=String.valueOf((char)('0'+(int)(Math.random()*10)));
                            flipping[i]=true; flipStep[i]=(flipStep[i]+1)%4;
                        }
                    }
                    SwingUtilities.invokeLater(FlipDisplay.this::repaint);
                    if(step[0]>numDigits*3+14) cancel();
                }
            },0,55);
        }
        @Override protected void paintComponent(Graphics g0){
            Graphics2D g=(Graphics2D)g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int cellW=32, cellH=52, startX=3, startY=16, gap=2;

            // outer bezel
            g.setColor(new Color(0x1A1A1A));
            g.fillRoundRect(0,10,w,h-10,12,12);
            g.setColor(new Color(0x404040));
            g.drawRoundRect(0,10,w-1,h-11,12,12);

            // label tab
            int tw=numDigits*34; FontMetrics fm0=g.getFontMetrics(new Font("Courier New",Font.BOLD,9));
            g.setColor(new Color(0x3A3A3A));
            g.fillRoundRect(w/2-fm0.stringWidth(tag)/2-6, 0, fm0.stringWidth(tag)+12, 16, 5,5);
            g.setFont(new Font("Courier New",Font.BOLD,9));
            g.setColor(new Color(0xBBBBBB));
            g.drawString(tag, w/2-fm0.stringWidth(tag)/2, 12);

            // cells
            Font digitFont = new Font("Courier New", Font.BOLD, cellH-10);
            g.setFont(digitFont);
            FontMetrics fm = g.getFontMetrics();

            for(int i=0;i<numDigits;i++){
                int cx=startX+i*(cellW+gap), cy=startY;
                // cell background
                g.setColor(new Color(0x0D0D0D));
                g.fillRoundRect(cx,cy,cellW,cellH,5,5);
                // centre divider line
                g.setColor(new Color(0,0,0,150));
                g.fillRect(cx,cy+cellH/2-1,cellW,2);

                String ch=chars[i];
                int tx=cx+(cellW-fm.stringWidth(ch))/2;
                int ty=cy+(cellH+fm.getAscent()-fm.getDescent())/2-1;

                if(flipping[i]&&flipStep[i]>0){
                    // top half squish
                    Shape clip=g.getClip();
                    g.setClip(cx,cy,cellW,cellH/2);
                    g.setColor(new Color(0xDDDDDD));
                    AffineTransform at=g.getTransform();
                    double sy=1.0-flipStep[i]*0.22;
                    g.translate(cx+cellW/2, cy+cellH/2);
                    g.scale(1,Math.max(0.05,sy));
                    g.drawString(ch, -fm.stringWidth(ch)/2, -cellH/2+fm.getAscent()-1);
                    g.setTransform(at);
                    g.setClip(clip);
                    // bottom half static
                    g.setClip(cx,cy+cellH/2,cellW,cellH/2);
                    g.setColor(new Color(0xAAAAAA));
                    g.drawString(ch,tx,ty);
                    g.setClip(clip);
                } else {
                    g.setColor(new Color(0xEEEEEE));
                    g.drawString(ch,tx,ty);
                    // subtle highlight
                    g.setColor(new Color(255,255,255,25));
                    g.drawString(ch,tx-1,ty-1);
                }
                // top shine strip
                g.setPaint(new GradientPaint(cx,cy,new Color(255,255,255,18),cx,cy+cellH/3,new Color(255,255,255,0)));
                g.fillRoundRect(cx,cy,cellW,cellH/3,4,4);
            }
            g.dispose();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // KeyButton
    // ═════════════════════════════════════════════════════════════════════════
    static class KeyButton extends JButton {
        private final Color base;
        private float hover=0f;
        private boolean down=false;
        private Timer ht;

        KeyButton(String lbl, Color base){
            super(lbl); this.base=base;
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setFont(new Font("Courier New",Font.BOLD,20));
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){animHover(true);}
                @Override public void mouseExited (MouseEvent e){animHover(false);}
                @Override public void mousePressed(MouseEvent e){down=true; repaint();}
                @Override public void mouseReleased(MouseEvent e){down=false; repaint();}
            });
        }
        private void animHover(boolean in){
            if(ht!=null) ht.cancel(); ht=new Timer();
            ht.scheduleAtFixedRate(new TimerTask(){
                @Override public void run(){
                    hover=in?Math.min(1f,hover+0.15f):Math.max(0f,hover-0.15f);
                    SwingUtilities.invokeLater(KeyButton.this::repaint);
                    if(in&&hover>=1f||!in&&hover<=0f) cancel();
                }
            },0,25);
        }
        @Override protected void paintComponent(Graphics g0){
            Graphics2D g=(Graphics2D)g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            int lift=down?0:5;

            // bottom shadow face (gives 3D depth)
            if(!down){
                g.setColor(base.darker().darker());
                g.fillRoundRect(0,lift,w-2,h-lift,14,14);
            }
            // top face gradient
            Color top=blend(base.brighter(),new Color(255,255,255,120),(int)(hover*80));
            g.setPaint(new GradientPaint(0,lift,top,0,lift+h*2/3,base));
            g.fillRoundRect(0,lift-2,w-2,h-lift,14,14);
            // sheen
            g.setPaint(new GradientPaint(0,lift,new Color(255,255,255,70),0,lift+h/3,new Color(255,255,255,0)));
            g.fillRoundRect(2,lift,w-6,(h-lift)/2,10,10);

            // text
            FontMetrics fm=g.getFontMetrics(getFont());
            String t=getText();
            int tx=(w-fm.stringWidth(t))/2, ty=lift+(h-lift-fm.getHeight())/2+fm.getAscent();
            g.setFont(getFont());
            g.setColor(new Color(0,0,0,60)); g.drawString(t,tx+1,ty+1);
            g.setColor(Color.WHITE);         g.drawString(t,tx,ty);
            g.dispose();
        }
        private Color blend(Color a, Color b, int alpha){
            return new Color(
                    Math.min(255,a.getRed()  +(b.getRed()  -a.getRed())  *alpha/255),
                    Math.min(255,a.getGreen()+(b.getGreen()-a.getGreen())*alpha/255),
                    Math.min(255,a.getBlue() +(b.getBlue() -a.getBlue()) *alpha/255)
            );
        }
    }
}