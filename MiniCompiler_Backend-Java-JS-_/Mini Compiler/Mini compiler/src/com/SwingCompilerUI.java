package com;

import com.analyzer.LexicalAnalyzer;
import com.analyzer.SemanticAnalyzer;
import com.analyzer.SyntaxAnalyzer;
import com.model.Token;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class SwingCompilerUI {

    private static ArrayList<Token> tokens;
    private static final JTextArea codeArea = new JTextArea();
    private static final JTextArea resultArea = new JTextArea();

    // Theme state
    private static boolean isDarkTheme = true;
    private static JFrame frame;

    // UI components for theme updates
    private static JPanel header;
    private static JPanel buttonBar;
    private static JLabel titleLabel;
    private static JSeparator divider;
    private static JLabel themeToggle;

    // Custom Curvy Button
    private static class CurvyButton extends JButton {
        private final Color baseColor;
        private boolean completed = false;

        public CurvyButton(String text, Color baseColor) {
            this.baseColor = baseColor;
            setLayout(new BorderLayout());
            JLabel label = new JLabel(
                    "<html><center><b><font color='black'>" + text + "</font></b></center></html>",
                    SwingConstants.CENTER
            );
            label.setFont(new Font("Segoe UI", Font.BOLD, 15));
            add(label, BorderLayout.CENTER);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(180, 70));
        }

        public void setCompleted(boolean c) {
            completed = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(completed ? baseColor.darker().darker() : baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {}
    }

    private static final CurvyButton lexicalBtn = new CurvyButton("Lexical Analysis", new Color(255, 225, 0));
    private static final CurvyButton syntaxBtn  = new CurvyButton("Syntax Analysis",  new Color(255, 225, 0));
    private static final CurvyButton semanticBtn = new CurvyButton("Semantic Analysis", new Color(255, 225, 0));

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(SwingCompilerUI::createGUI);
    }

    private static Border createGlowBorder(String title) {
        Color glow = new Color(255, 225, 0);
        Border line = BorderFactory.createLineBorder(glow, 2);
        Border titled = BorderFactory.createTitledBorder(line, title, 0, 0,
                new Font("Arial", Font.BOLD, 14), glow);
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10), titled);
    }

    // Safe theme updater — only touches components that exist
    private static void applyTheme() {
        if (frame == null) return;

        Color bg = isDarkTheme ? Color.BLACK : new Color(252, 252, 252);
        Color fg = isDarkTheme ? Color.WHITE : Color.BLACK;
        Color resultFg = isDarkTheme ? Color.CYAN : new Color(0, 120, 215);
        Color headerBg = isDarkTheme ? Color.BLACK : Color.WHITE;
        Color titleFg = new Color(255, 225, 0);
        Color toggleFg = isDarkTheme ? Color.WHITE : Color.BLACK;
        Color buttonBarBg = isDarkTheme ? Color.BLACK : new Color(240, 245, 250);

        codeArea.setBackground(bg);
        codeArea.setForeground(fg);
        codeArea.setCaretColor(fg);
        resultArea.setBackground(bg);
        resultArea.setForeground(resultFg);

        codeArea.setBorder(createGlowBorder(" Source Code "));
        resultArea.setBorder(createGlowBorder(" Result Output "));

        if (header != null) {
            header.setBackground(headerBg);
        }
        if (buttonBar != null) {
            buttonBar.setBackground(buttonBarBg);
        }
        if (titleLabel != null) {
            titleLabel.setForeground(titleFg);
        }
        if (divider != null) {
            divider.setForeground(new Color(255, 225, 0));
        }
        if (themeToggle != null) {
            themeToggle.setForeground(toggleFg);
        }

        frame.getContentPane().setBackground(isDarkTheme ? new Color(30, 30, 30) : Color.WHITE);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private static void createGUI() {
        frame = new JFrame("Prism Compiler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1450, 850);
        frame.setMinimumSize(new Dimension(1000, 600));

        // === HEADER ===
        header = new JPanel(new BorderLayout());
        header.setBackground(isDarkTheme ? Color.BLACK : Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        titleLabel = new JLabel("Prism Compiler");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 225, 0));
        header.add(titleLabel, BorderLayout.WEST);

        themeToggle = new JLabel(isDarkTheme ? "Light" : "Dark");
        themeToggle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        themeToggle.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
        themeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeToggle.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                isDarkTheme = !isDarkTheme;
                themeToggle.setText(isDarkTheme ? "Light" : "Dark");
                themeToggle.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
                applyTheme();
            }
        });
        header.add(themeToggle, BorderLayout.EAST);

        // Divider
        divider = new JSeparator();
        divider.setForeground(new Color(255, 225, 0));

        // === BUTTON BAR ===
        buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        buttonBar.setBackground(isDarkTheme ? Color.BLACK : new Color(240, 245, 250));

        CurvyButton openBtn  = new CurvyButton("Open File", new Color(255, 225, 0));
        CurvyButton clearBtn = new CurvyButton("Clear",      new Color(255, 0, 0));

        lexicalBtn.setEnabled(false);
        syntaxBtn.setEnabled(false);
        semanticBtn.setEnabled(false);

        buttonBar.add(openBtn);
        buttonBar.add(lexicalBtn);
        buttonBar.add(syntaxBtn);
        buttonBar.add(semanticBtn);
        buttonBar.add(clearBtn);

        // === TEXT AREAS ===
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Courier", Font.PLAIN, 15));
        codeArea.setFont(new Font("Courier", Font.PLAIN, 15));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(resultArea),
                new JScrollPane(codeArea));
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.5);

        // === FINAL LAYOUT ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(divider, BorderLayout.CENTER);
        topPanel.add(buttonBar, BorderLayout.SOUTH);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);

        // Apply theme only AFTER everything is added
        applyTheme();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // === ACTIONS ===
        openBtn.addActionListener(e -> openFile(frame));
        clearBtn.addActionListener(e -> {
            codeArea.setText("");
            resultArea.setText("");
            tokens = null;
            resetButtons();
            lexicalBtn.setCompleted(false);
            syntaxBtn.setCompleted(false);
            semanticBtn.setCompleted(false);
        });

        lexicalBtn.addActionListener(e -> runLexical());
        syntaxBtn.addActionListener(e -> runSyntax());
        semanticBtn.addActionListener(e -> runSemantic());

        codeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            private void update() {
                lexicalBtn.setEnabled(!codeArea.getText().trim().isEmpty());
            }
        });
    }

    private static void openFile(JFrame frame) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                codeArea.setText(Files.readString(fc.getSelectedFile().toPath()));
                resultArea.setText("File opened: " + fc.getSelectedFile().getName() + "\n\n");
                resetButtons();
                lexicalBtn.setCompleted(false);
                syntaxBtn.setCompleted(false);
                semanticBtn.setCompleted(false);
            } catch (Exception ex) {
                resultArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }
    }

    private static void runLexical() {
        resultArea.append("\n=== Running Lexical Analysis ===\n\n");
        try {
            tokens = LexicalAnalyzer.tokenize(codeArea.getText());
            if (!LexicalAnalyzer.isValidLexically(tokens)) {
                resultArea.append("Lexical analysis FAILED!\nUnknown tokens found.\n\n");
                lexicalBtn.setCompleted(false);
                return;
            }
            resultArea.append("Lexical Analysis Completed.\nTokens: " + tokens.size() + "\n\n");
            syntaxBtn.setEnabled(true);
            lexicalBtn.setCompleted(true);
        } catch (Exception ex) {
            resultArea.append("Lexical Error: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }

    private static void runSyntax() {
        resultArea.append("\n=== Running Syntax Analysis ===\n\n");
        try {
            if (!SyntaxAnalyzer.analyze(tokens)) {
                resultArea.append("Syntax analysis FAILED!\nInvalid syntax.\n\n");
                syntaxBtn.setCompleted(false);
                return;
            }
            resultArea.append("Syntax Analysis Completed.\n\n");
            semanticBtn.setEnabled(true);
            syntaxBtn.setCompleted(true);
        } catch (Exception ex) {
            resultArea.append("Syntax Error: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }

    private static void runSemantic() {
        resultArea.append("\n=== Running Semantic Analysis ===\n\n");
        try {
            if (!SemanticAnalyzer.analyze(tokens)) {
                resultArea.append("Semantic analysis FAILED!\nType mismatch or duplicate var.\n\n");
                semanticBtn.setCompleted(false);
                return;
            }
            resultArea.append("Semantic Analysis Completed.\n\n");
            resultArea.append("ALL ANALYSES PASSED! COMPILATION SUCCESSFUL!\n\n");
            semanticBtn.setCompleted(true);
        } catch (Exception ex) {
            resultArea.append("Semantic Error: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }

    private static void resetButtons() {
        lexicalBtn.setEnabled(!codeArea.getText().trim().isEmpty());
        syntaxBtn.setEnabled(false);
        semanticBtn.setEnabled(false);
    }
}
