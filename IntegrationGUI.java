package montecarlo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

public class IntegrationGUI extends JFrame {
    private JComboBox<String> functionSelector;
    private JTextField aField, bField, speedupField;
    private JLabel functionLabel, intervalLabel, timeSeqLabel, timeParLabel, speedupLabel, resultLabel;
    private FunctionPlotPanel graphPanel;
    private JPanel centerPanel;

    public IntegrationGUI() {
        setTitle("Monte Carlo Integration");
        setIconImage(new ImageIcon(getClass().getResource("/montecarlo/icon.png")).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Font font = new Font("SansSerif", Font.BOLD, 16);

        JLabel fLabel = new JLabel("Choose Function:");
        functionSelector = new JComboBox<>(new String[]{
            "sin(x)", "x^2", "e^(-x^2)", "cos(x)", "log(x + 1)", "sqrt(x)"
        });
        functionSelector.setMaximumSize(new Dimension(200, 30));

        JLabel intervalLabel = new JLabel("Interval:");
        JPanel intervalPanel = new JPanel();
        intervalPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel leftBracket = new JLabel("[");
        aField = new JTextField("0", 4);
        JLabel comma = new JLabel(",");
        bField = new JTextField("3.14", 4);
        JLabel rightBracket = new JLabel("]");
        intervalPanel.add(leftBracket);
        intervalPanel.add(aField);
        intervalPanel.add(comma);
        intervalPanel.add(bField);
        intervalPanel.add(rightBracket);

        JLabel sLabel = new JLabel("Desired Speed-up:");
        speedupField = new JTextField("2.0", 4);
        speedupField.setMaximumSize(new Dimension(80, 30));

        JButton runButton = new JButton("Run Integration");
        runButton.setMaximumSize(new Dimension(200, 30));

        fLabel.setFont(font);
        functionSelector.setFont(font);
        intervalLabel.setFont(font);
        aField.setFont(font);
        bField.setFont(font);
        sLabel.setFont(font);
        speedupField.setFont(font);
        runButton.setFont(font);

        fLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        functionSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
        intervalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        intervalPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        speedupField.setAlignmentX(Component.CENTER_ALIGNMENT);
        runButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        inputPanel.add(fLabel);
        inputPanel.add(functionSelector);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(intervalLabel);
        inputPanel.add(intervalPanel);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(sLabel);
        inputPanel.add(speedupField);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(runButton);

        add(inputPanel, BorderLayout.CENTER);

        runButton.addActionListener(e -> {
            try {
                String functionName = (String) functionSelector.getSelectedItem();
                double a = Double.parseDouble(aField.getText());
                double b = Double.parseDouble(bField.getText());
                double targetSpeedup = Double.parseDouble(speedupField.getText());

                getContentPane().removeAll();
                showResultLayout();
                runIntegration(functionName, a, b, targetSpeedup);
                revalidate();
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    public void showResultLayout() {
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        graphPanel = new FunctionPlotPanel();
        graphPanel.setPreferredSize(new Dimension(760, 300));
        centerPanel.add(graphPanel);

        Font font = new Font("SansSerif", Font.BOLD, 16);

        functionLabel = new JLabel("f(x) = ");
        intervalLabel = new JLabel("Interval: [a, b]");
        timeSeqLabel = new JLabel("Time (Sequential) = ");
        timeParLabel = new JLabel("Time (Parallel) = ");
        speedupLabel = new JLabel("Speed-up = ");
        resultLabel = new JLabel("Estimated Integral = ");

        functionLabel.setFont(font);
        intervalLabel.setFont(font);
        timeSeqLabel.setFont(font);
        timeParLabel.setFont(font);
        speedupLabel.setFont(font);
        resultLabel.setFont(font);

        functionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        intervalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeSeqLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeParLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        speedupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(functionLabel);
        centerPanel.add(intervalLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(timeSeqLabel);
        centerPanel.add(timeParLabel);
        centerPanel.add(speedupLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(resultLabel);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void runIntegration(String functionName, double a, double b, double targetSpeedup) {
        try {
            Function f = switch (functionName) {
                case "x^2" -> FunctionsLibrary.squareFunction();
                case "e^(-x^2)" -> FunctionsLibrary.expNegSquare();
                case "cos(x)" -> x -> Math.cos(x);
                case "log(x + 1)" -> x -> Math.log(x + 1);
                case "sqrt(x)" -> x -> Math.sqrt(x);
                default -> FunctionsLibrary.sinFunction();
            };

            int samples = 10_000_000;
            long t1 = System.nanoTime();
            double resultSeq = Integrator.integrateSequential(f, a, b, samples);
            long t2 = System.nanoTime();
            double timeSeq = (t2 - t1) / 1e6;

            int bestThreads = 1;
            double bestSpeedup = 0;
            double resultPar = 0;
            double timePar = 0;

            for (int threads = 1; threads <= Runtime.getRuntime().availableProcessors(); threads++) {
                long pt1 = System.nanoTime();
                double tempResultPar = Integrator.integrateParallel(f, a, b, samples, threads);
                long pt2 = System.nanoTime();
                double tempTimePar = (pt2 - pt1) / 1e6;
                double tempSpeedup = timeSeq / tempTimePar;

                if (tempSpeedup >= targetSpeedup) {
                    bestThreads = threads;
                    resultPar = tempResultPar;
                    timePar = tempTimePar;
                    break;
                }

                if (tempSpeedup > bestSpeedup) {
                    bestThreads = threads;
                    resultPar = tempResultPar;
                    timePar = tempTimePar;
                    bestSpeedup = tempSpeedup;
                }
            }

            double speedup = timeSeq / timePar;

            functionLabel.setText("f(x) = " + functionName);
            intervalLabel.setText(String.format("Interval: [%.2f, %.2f]", a, b));
            timeSeqLabel.setText(String.format("Time (Sequential) = %.2f ms", timeSeq));
            timeParLabel.setText(String.format("Time (Parallel) = %.2f ms", timePar));
            speedupLabel.setText(String.format("Speed-up = %.2f×", speedup));
            String sciResult = String.format("%.3e", resultPar).replace("e", " × 10^");
            resultLabel.setText("Estimated Integral = " + sciResult);



            if (speedup < targetSpeedup) {
                JOptionPane.showMessageDialog(this,
                        String.format("⚠ Actual speed-up (%.2f×) is less than requested (%.2f×).\nTry increasing the interval or sample size.", speedup, targetSpeedup),
                        "Performance Warning", JOptionPane.WARNING_MESSAGE);
            }

            graphPanel.setFunction(f, a, b);

        } catch (InterruptedException | ExecutionException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(IntegrationGUI::new);
    }
}
