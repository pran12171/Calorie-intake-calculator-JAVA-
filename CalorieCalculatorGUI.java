import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class User {
    private String name;
    private int age;
    private String gender;
    private double height;
    private double weight;
    private String activityLevel;

    public User(String name, int age, String gender, double height, double weight, String activityLevel) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.activityLevel = activityLevel;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public String getActivityLevel() { return activityLevel; }
}

class CalorieEngine {
    public static double calculateBMR(User user) {
        if (user.getGender().equalsIgnoreCase("Male")) {
            return (10 * user.getWeight()) + (6.25 * user.getHeight()) - (5 * user.getAge()) + 5;
        } else {
            return (10 * user.getWeight()) + (6.25 * user.getHeight()) - (5 * user.getAge()) - 161;
        }
    }

    private static double multiplier(String act) {
        switch (act) {
            case "Sedentary (little or no exercise)": return 1.2;
            case "Lightly Active (1-3 days/week)": return 1.375;
            case "Moderately Active (3-5 days/week)": return 1.55;
            case "Very Active (6-7 days/week)": return 1.725;
            case "Extremely Active (physical job/training)": return 1.9;
            default: return 1.2;
        }
    }

    public static double calculateTDEE(User user) {
        return calculateBMR(user) * multiplier(user.getActivityLevel());
    }

    public static int getWeightLossCalories(double tdee) { return (int)Math.round(tdee - 500); }
    public static int getWeightGainCalories(double tdee) { return (int)Math.round(tdee + 300); }
    public static int getMaintenanceCalories(double tdee) { return (int)Math.round(tdee); }
}

public class CalorieCalculatorGUI extends JFrame {
    private JTextField nameField, ageField, heightField, weightField;
    private JComboBox<String> genderBox, activityBox;
    private JButton calculateButton, clearButton, saveButton, historyButton, darkModeButton;
    private JTextArea resultsArea;

    private static final String DATA_FILE = "saved_data.txt";
    private boolean darkMode = false;

    private String[] activityLevels = {
        "Sedentary (little or no exercise)",
        "Lightly Active (1-3 days/week)",
        "Moderately Active (3-5 days/week)",
        "Very Active (6-7 days/week)",
        "Extremely Active (physical job/training)"
    };

    // Tabbed Panel Components
    private JTabbedPane tabbedPane;
    private JPanel resultsPanel, barChartPanel, lineChartPanel;
    private int lastTDEE = 0;
    private List<Integer> tdeeHistory = new ArrayList<>();

    public CalorieCalculatorGUI() {
        setTitle("Calorie Intake Calculator");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        createHeaderPanel();
        createInputPanel();
        createButtonPanel();
        createTabbedResultsPanel();
        loadLastSavedEntry();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createHeaderPanel() {
        JPanel header = new JPanel();
        header.setBackground(new Color(41,128,185));
        header.setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel("Calorie Intake Calculator");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Mifflin-St Jeor Equation Based Calculator");
        sub.setForeground(Color.LIGHT_GRAY);

        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(sub);

        add(header, BorderLayout.NORTH);
    }

    private void createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20,30,10,30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,5,8,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, 0, "Name:", nameField = new JTextField(20));
        addField(panel, gbc, 1, "Age:", ageField = new JTextField(20));

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Gender:"), gbc);
        genderBox = new JComboBox<>(new String[]{"Male","Female"});
        gbc.gridx = 1;
        panel.add(genderBox, gbc);

        addField(panel, gbc, 3, "Height (cm):", heightField = new JTextField(20));
        addField(panel, gbc, 4, "Weight (kg):", weightField = new JTextField(20));

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Activity Level:"), gbc);
        activityBox = new JComboBox<>(activityLevels);
        gbc.gridx = 1;
        panel.add(activityBox, gbc);

        add(panel, BorderLayout.CENTER);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(Color.WHITE);

        calculateButton = createButton("Calculate", new Color(46,204,113));
        clearButton     = createButton("Clear", new Color(231,76,60));
        saveButton      = createButton("Save", new Color(52,152,219));
        historyButton   = createButton("History", new Color(155,89,182));
        darkModeButton  = createButton("Dark Mode", new Color(52,73,94));

        calculateButton.addActionListener(e -> calculateCalories());
        clearButton.addActionListener(e -> clearFields());
        saveButton.addActionListener(e -> saveToFile());
        historyButton.addActionListener(e -> openSearchableHistory());
        darkModeButton.addActionListener(e -> toggleDarkMode());

        panel.add(calculateButton);
        panel.add(clearButton);
        panel.add(saveButton);
        panel.add(historyButton);
        panel.add(darkModeButton);

        add(panel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(140,40));
        
        // Make buttons EXTREMELY bright and vivid
        int r = Math.min(255, color.getRed() + 100);
        int g = Math.min(255, color.getGreen() + 100);
        int b = Math.min(255, color.getBlue() + 100);
        Color veryBrightColor = new Color(r, g, b);
        
        // Critical: Set these properties to ensure full visibility
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBackground(veryBrightColor);
        btn.setForeground(Color.BLACK);  // Changed to BLACK for better contrast
        btn.setFont(new Font("Arial", Font.BOLD, 16));  // Larger font
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(color.darker(), 3));  // Thick border
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(veryBrightColor.brighter());
                btn.setFont(new Font("Arial", Font.BOLD, 17));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(veryBrightColor);
                btn.setFont(new Font("Arial", Font.BOLD, 16));
            }
        });
        
        return btn;
    }

    private void createTabbedResultsPanel() {
        tabbedPane = new JTabbedPane();

        resultsPanel = new JPanel(new BorderLayout());
        resultsArea = new JTextArea(15,50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced",Font.PLAIN,13));
        resultsPanel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        barChartPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart(g);
            }
        };
        lineChartPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawLineChart(g);
            }
        };

        tabbedPane.add("Results", resultsPanel);
        tabbedPane.add("Bar Chart", barChartPanel);
        tabbedPane.add("Line Chart", lineChartPanel);

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new EmptyBorder(0,30,20,30));
        container.add(tabbedPane, BorderLayout.CENTER);

        add(container, BorderLayout.EAST);
    }

    private int lastBMR, lastMaintenance, lastLoss, lastGain;

    private void calculateCalories() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showError("Enter name"); return; }
            int age = Integer.parseInt(ageField.getText());
            double height = Double.parseDouble(heightField.getText());
            double weight = Double.parseDouble(weightField.getText());
            String gender = (String)genderBox.getSelectedItem();
            String act = (String)activityBox.getSelectedItem();

            User u = new User(name, age, gender, height, weight, act);
            double bmr = CalorieEngine.calculateBMR(u);
            double tdee = CalorieEngine.calculateTDEE(u);
            int maintenance = CalorieEngine.getMaintenanceCalories(tdee);
            int loss = CalorieEngine.getWeightLossCalories(tdee);
            int gain = CalorieEngine.getWeightGainCalories(tdee);

            lastBMR = (int)bmr;
            lastMaintenance = maintenance;
            lastLoss = loss;
            lastGain = gain;
            lastTDEE = maintenance;
            tdeeHistory.add(maintenance);

            String text =
                "═══════════════════════════════════════\n" +
                "  PERSONALIZED CALORIE REPORT\n" +
                "═══════════════════════════════════════\n\n" +
                "Name: " + name + "\n" +
                "Age: " + age + " | Gender: " + gender + "\n" +
                "Height: " + height + " cm | Weight: " + weight + " kg\n\n" +
                "BMR: " + (int)bmr + " cal/day\n" +
                "TDEE: " + maintenance + " cal/day\n\n" +
                "Weight Loss: " + loss + " cal/day\n" +
                "Maintenance: " + maintenance + " cal/day\n" +
                "Weight Gain: " + gain + " cal/day\n" +
                "\n═══════════════════════════════════════";

            resultsArea.setText(text);
            barChartPanel.repaint();
            lineChartPanel.repaint();

        } catch (Exception e) {
            showError("Invalid input.");
        }
    }

    private void drawBarChart(Graphics g) {
        int width = barChartPanel.getWidth() - 40;
        int height = barChartPanel.getHeight() - 40;
        int max = Math.max(Math.max(lastBMR,lastMaintenance), Math.max(lastLoss,lastGain));

        if(max==0) return;
        int barWidth = width/5;

        g.setColor(Color.BLUE); g.fillRect(20, height-(lastBMR*height/max), barWidth, lastBMR*height/max);
        g.setColor(Color.GREEN); g.fillRect(40+barWidth, height-(lastMaintenance*height/max), barWidth, lastMaintenance*height/max);
        g.setColor(Color.RED); g.fillRect(60+2*barWidth, height-(lastLoss*height/max), barWidth, lastLoss*height/max);
        g.setColor(Color.ORANGE); g.fillRect(80+3*barWidth, height-(lastGain*height/max), barWidth, lastGain*height/max);

        g.setColor(Color.BLACK);
        g.drawString("BMR", 20, height+15);
        g.drawString("TDEE", 40+barWidth, height+15);
        g.drawString("Loss", 60+2*barWidth, height+15);
        g.drawString("Gain", 80+3*barWidth, height+15);
    }

    private void drawLineChart(Graphics g) {
        if(tdeeHistory.size()<2) return;
        int w = lineChartPanel.getWidth()-40;
        int h = lineChartPanel.getHeight()-40;
        int max = tdeeHistory.stream().max(Integer::compareTo).orElse(1);
        int n = tdeeHistory.size();

        g.setColor(Color.BLUE);
        for(int i=0;i<n-1;i++) {
            int x1 = 20 + i*w/(n-1);
            int y1 = h - tdeeHistory.get(i)*h/max;
            int x2 = 20 + (i+1)*w/(n-1);
            int y2 = h - tdeeHistory.get(i+1)*h/max;
            g.drawLine(x1, y1, x2, y2);
        }
    }

    private void saveToFile() {
        if (resultsArea.getText().isEmpty()) { showError("Calculate first."); return; }
        try(FileWriter fw = new FileWriter(DATA_FILE,true)) {
            fw.write("\n\n=== ENTRY SAVED: "+LocalDateTime.now()+" ===\n");
            fw.write(resultsArea.getText());
            fw.write("\n----------------------------------------\n");
            JOptionPane.showMessageDialog(this,"Saved!");
        } catch (Exception e) { showError("Save failed."); }
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        Color bg = darkMode ? Color.DARK_GRAY : Color.WHITE;
        Color fg = darkMode ? Color.WHITE : Color.BLACK;
        getContentPane().setBackground(bg);
        for (Component c : getContentPane().getComponents()) setColors(c,bg,fg);
        resultsArea.setBackground(bg); resultsArea.setForeground(fg);
        barChartPanel.setBackground(bg); lineChartPanel.setBackground(bg);
        repaint();
    }

    private void setColors(Component c, Color bg, Color fg) {
        if(c instanceof JPanel) { c.setBackground(bg); for(Component child:((JPanel)c).getComponents()) setColors(child,bg,fg);}
        else if(c instanceof JTextArea || c instanceof JTextField || c instanceof JComboBox) { c.setBackground(bg); c.setForeground(fg);}
    }

    private void loadLastSavedEntry() {
        File f = new File(DATA_FILE);
        if(!f.exists()) return;
        try {
            List<String> lines = Files.readAllLines(f.toPath());
            StringBuilder last = new StringBuilder();
            boolean found=false;
            for(String line:lines) {
                if(line.startsWith("=== ENTRY SAVED:")) {found=true; last=new StringBuilder();}
                if(found) last.append(line).append("\n");
            }
            resultsArea.setText(last.toString());
        } catch(Exception e){}
    }

    private void clearFields() {
        nameField.setText(""); ageField.setText("");
        heightField.setText(""); weightField.setText("");
        genderBox.setSelectedIndex(0); activityBox.setSelectedIndex(0);
        resultsArea.setText("");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);
    }

    private void openSearchableHistory() {
        JFrame frame = new JFrame("Searchable History");
        frame.setSize(800,600);
        frame.setLayout(new BorderLayout());

        JTextField searchField = new JTextField();
        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced",Font.PLAIN,14));
        JScrollPane scroll = new JScrollPane(historyArea);

        String allData = loadAllHistory();
        historyArea.setText(allData);

        searchField.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){filter();}
            public void removeUpdate(DocumentEvent e){filter();}
            public void changedUpdate(DocumentEvent e){filter();}
            private void filter(){
                String q = searchField.getText().toLowerCase();
                String[] entries = allData.split("=== ENTRY SAVED:");
                StringBuilder filtered = new StringBuilder();
                for(String entry:entries){
                    if(entry.toLowerCase().contains(q))
                        filtered.append("=== ENTRY SAVED:").append(entry);
                }
                historyArea.setText(filtered.length()==0?"No matching records.":filtered.toString());
            }
        });

        frame.add(searchField, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private String loadAllHistory() {
        try { return new String(Files.readAllBytes(new File(DATA_FILE).toPath())); }
        catch(Exception e) { return "No history available."; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalorieCalculatorGUI());
    }
}