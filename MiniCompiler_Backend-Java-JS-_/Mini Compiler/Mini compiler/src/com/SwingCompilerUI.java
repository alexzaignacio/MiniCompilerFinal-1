package com;

import com.analyzer.LexicalAnalyzer;
import com.analyzer.SemanticAnalyzer;
import com.analyzer.SyntaxAnalyzer;
import com.model.Token;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class SwingCompilerUI {

    private static ArrayList<Token> tokens;
    private static final JTextArea codeArea = new JTextArea();
    private static final JTextArea resultArea = new JTextArea();

    private static boolean isDarkTheme = true;
    private static JFrame frame;
    private static JPanel header, buttonBar, labelPanel;
    private static JLabel titleLabel, themeToggle;
    private static JSeparator divider;

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
            g2.setColor(completed ? baseColor.darker().darker().darker() : baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {}
    }

    private static final CurvyButton lexicalBtn = new CurvyButton("Lexical<br>Analysis", new Color(255, 225, 0));
    private static final CurvyButton syntaxBtn  = new CurvyButton("Syntax<br>Analysis",  new Color(255, 225, 0));
    private static final CurvyButton semanticBtn = new CurvyButton("Semantic<br>Analysis", new Color(255, 225, 0));

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(SwingCompilerUI::createGUI);
    }

    private static void applyTheme() {
        if (frame == null) return;

        Color bg = isDarkTheme ? Color.BLACK : new Color(252, 252, 252);
        Color fg = isDarkTheme ? Color.WHITE : Color.BLACK;
        Color gold = new Color(255, 225, 0);
        Color headerBg = isDarkTheme ? Color.BLACK : Color.WHITE;
        Color buttonBarBg = isDarkTheme ? Color.BLACK : new Color(240, 245, 250);

        codeArea.setBackground(bg);
        codeArea.setForeground(fg);
        codeArea.setCaretColor(fg);
        resultArea.setBackground(bg);
        resultArea.setForeground(isDarkTheme ? Color.CYAN : new Color(0, 120, 215));

        if (header != null) header.setBackground(headerBg);
        if (buttonBar != null) buttonBar.setBackground(buttonBarBg);
        if (labelPanel != null) labelPanel.setBackground(buttonBarBg);
        if (titleLabel != null) titleLabel.setForeground(gold);
        if (divider != null) divider.setForeground(gold);
        if (themeToggle != null) {
            themeToggle.setText(isDarkTheme ? "Light" : "Dark");
            themeToggle.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
        }

        frame.getContentPane().setBackground(isDarkTheme ? new Color(30, 30, 30) : Color.WHITE);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private static void createGUI() {
        frame = new JFrame("Thaeu Compiler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1450, 850);
        frame.setMinimumSize(new Dimension(1000, 600));

        // === HEADER ===
        header = new JPanel(new BorderLayout());
        header.setBackground(Color.BLACK);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        titleLabel = new JLabel("Thaeu Compiler");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 225, 0));
        header.add(titleLabel, BorderLayout.WEST);

        themeToggle = new JLabel("Light");
        themeToggle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        themeToggle.setForeground(Color.WHITE);
        themeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeToggle.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {  // Fixed line
                isDarkTheme = !isDarkTheme;
                applyTheme();
            }
        });
        header.add(themeToggle, BorderLayout.EAST);

        // === DIVIDER ===
        divider = new JSeparator();
        divider.setForeground(new Color(255, 225, 0));

        // === BUTTON BAR ===
        buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        buttonBar.setBackground(Color.BLACK);

        CurvyButton openBtn  = new CurvyButton("Open<br>File", new Color(255, 225, 0));
        CurvyButton clearBtn = new CurvyButton("Clear",      new Color(255, 0, 0));

        lexicalBtn.setEnabled(false);
        syntaxBtn.setEnabled(false);
        semanticBtn.setEnabled(false);

        buttonBar.add(openBtn);
        buttonBar.add(lexicalBtn);
        buttonBar.add(syntaxBtn);
        buttonBar.add(semanticBtn);
        buttonBar.add(clearBtn);

        // === LABEL PANEL ===
        labelPanel = new JPanel(new GridLayout(1, 2));
        labelPanel.setBackground(Color.BLACK);
        labelPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel sourceLabel = new JLabel("Source Code", SwingConstants.CENTER);
        sourceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sourceLabel.setForeground(new Color(255, 225, 0));

        JLabel resultLabel = new JLabel("Result Output", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel.setForeground(new Color(255, 225, 0));

        labelPanel.add(sourceLabel);
        labelPanel.add(resultLabel);

        // === LINE NUMBERS ===
        JTextArea lineNumbers = new JTextArea("1");
        lineNumbers.setBackground(new Color(40, 40, 40));
        lineNumbers.setForeground(new Color(150, 150, 150));
        lineNumbers.setEditable(false);
        lineNumbers.setFont(new Font("Courier", Font.PLAIN, 15));

        codeArea.setFont(new Font("Courier", Font.PLAIN, 15));
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateLineNumbers(); }
            public void removeUpdate(DocumentEvent e) { updateLineNumbers(); }
            public void changedUpdate(DocumentEvent e) {}
            private void updateLineNumbers() {
                StringBuilder sb = new StringBuilder();
                int lines = codeArea.getLineCount();
                for (int i = 1; i <= lines; i++) sb.append(i).append('\n');
                lineNumbers.setText(sb.toString());
            }
        });

        // === SOURCE CODE PANEL WITH PADDING ===
        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.setBackground(isDarkTheme ? Color.BLACK : new Color(252, 252, 252));
        codePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        codePanel.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        JScrollPane codeScrollPane = new JScrollPane(codePanel);
        codeScrollPane.setRowHeaderView(lineNumbers);
        codeScrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 225, 0), 3));

        // === RESULT PANEL WITH PADDING ===
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(isDarkTheme ? Color.BLACK : new Color(252, 252, 252));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JScrollPane resultScrollPane = new JScrollPane(resultPanel);
        resultScrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 225, 0), 3));

        // === SPLIT PANE ===
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                codeScrollPane, resultScrollPane);
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);

        // === FINAL LAYOUT ===
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(divider, BorderLayout.CENTER);
        topSection.add(buttonBar, BorderLayout.SOUTH);

        JPanel middleSection = new JPanel(new BorderLayout());
        middleSection.add(topSection, BorderLayout.NORTH);
        middleSection.add(labelPanel, BorderLayout.SOUTH);

        frame.setLayout(new BorderLayout());
        frame.add(middleSection, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);

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

        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateEnable(); }
            public void removeUpdate(DocumentEvent e) { updateEnable(); }
            public void changedUpdate(DocumentEvent e) {}
            private void updateEnable() {
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
