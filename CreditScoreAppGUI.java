import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreditScoreAppGUI extends JFrame {

    private static final String DATA_FILE = "credit_profiles.txt";

    // Form fields
    private JTextField nameField;
    private JTextField ageField;
    private JTextField incomeField;
    private JComboBox<EmploymentType> employmentBox;
    private JTextField loanAmountField;
    private JTextField yearsField;
    private JTextField rateField;
    private JTextField bankField;

    // Output area
    private JTextArea outputArea;

    private List<Applicant> applicants = new ArrayList<>();

    public CreditScoreAppGUI() {
        setTitle("Smart Credit Score Predictor - GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // Main layout
        setLayout(new BorderLayout());

        // ===== Form Panel =====
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(15);
        ageField = new JTextField(10);
        incomeField = new JTextField(10);
        employmentBox = new JComboBox<>(EmploymentType.values());
        loanAmountField = new JTextField(10);
        yearsField = new JTextField(10);
        rateField = new JTextField(10);
        bankField = new JTextField(15);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        formPanel.add(ageField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Monthly Income:"), gbc);
        gbc.gridx = 1;
        formPanel.add(incomeField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Employment Type:"), gbc);
        gbc.gridx = 1;
        formPanel.add(employmentBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Loan Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(loanAmountField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Repayment Years:"), gbc);
        gbc.gridx = 1;
        formPanel.add(yearsField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Yearly Interest Rate (e.g. 0.08):"), gbc);
        gbc.gridx = 1;
        formPanel.add(rateField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Bank Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(bankField, gbc);

        // ===== Buttons Panel =====
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton("Add Applicant");
        JButton viewButton = new JButton("View Applicants & Stats");
        JButton saveButton = new JButton("Save to File");
        JButton loadButton = new JButton("Load from File");
        JButton clearButton = new JButton("Clear Output");

        buttonsPanel.add(addButton);
        buttonsPanel.add(viewButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(loadButton);
        buttonsPanel.add(clearButton);

        // ===== Output Area =====
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Add panels to frame
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // ===== Action Listeners =====
        addButton.addActionListener(this::handleAddApplicant);
        viewButton.addActionListener(this::handleViewApplicants);
        saveButton.addActionListener(this::handleSaveToFile);
        loadButton.addActionListener(this::handleLoadFromFile);
        clearButton.addActionListener(e -> outputArea.setText(""));
    }

    // ===== Button Handlers =====

    private void handleAddApplicant(ActionEvent e) {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Name cannot be empty");
                return;
            }

            int age = Integer.parseInt(ageField.getText().trim());
            double income = Double.parseDouble(incomeField.getText().trim());
            EmploymentType type = (EmploymentType) employmentBox.getSelectedItem();
            double loanAmount = Double.parseDouble(loanAmountField.getText().trim());
            int years = Integer.parseInt(yearsField.getText().trim());
            double rate = Double.parseDouble(rateField.getText().trim());
            String bankName = bankField.getText().trim();
            if (bankName.isEmpty()) {
                showError("Bank name cannot be empty");
                return;
            }

            Applicant applicant;
            if (type == EmploymentType.SALARIED) {
                applicant = new SalariedApplicant(name, age, income, type, loanAmount, years, rate, bankName);
            } else if (type == EmploymentType.SELF_EMPLOYED) {
                applicant = new SelfEmployedApplicant(name, age, income, type, loanAmount, years, rate, bankName);
            } else {
                applicant = new StudentApplicant(name, age, income, type, loanAmount, years, rate, bankName);
            }

            applicants.add(applicant);
            outputArea.append("Applicant added: " + name + "\n");

        } catch (NumberFormatException ex) {
            showError("Please enter valid numeric values for age, income, loan, years, and rate.");
        }
    }

    private void handleViewApplicants(ActionEvent e) {
        if (applicants.isEmpty()) {
            outputArea.append("No applicants to display.\n");
            return;
        }

        outputArea.append("\n--- All Applicants ---\n");
        double[] dtiArray = new double[applicants.size()];

        for (int i = 0; i < applicants.size(); i++) {
            Applicant a = applicants.get(i);
            double dti = a.calculateDTI();
            dtiArray[i] = dti;

            RiskCategory risk = a.evaluateRisk();
            int flags = a.calculateRiskFlags();
            String binaryFlags = String.format("%3s", Integer.toBinaryString(flags)).replace(' ', '0');

            outputArea.append(a.basicDisplay() + "\n");
            outputArea.append(String.format("   DTI: %.3f | Risk: %-9s | Flags(bits): %s%n",
                    dti, risk, binaryFlags));
        }

        double min = dtiArray[0], max = dtiArray[0], sum = 0;
        for (double v : dtiArray) {
            if (v < min) min = v;
            if (v > max) max = v;
            sum += v;
        }
        double avg = sum / dtiArray.length;

        outputArea.append(String.format("%nDTI Statistics -> Min: %.3f  Max: %.3f  Avg: %.3f%n",
                min, max, avg));

        long excellentCount = applicants.stream()
                .filter(a -> a.evaluateRisk() == RiskCategory.EXCELLENT)
                .count();
        outputArea.append("Number of EXCELLENT profiles: " + excellentCount + "\n");
    }

    private void handleSaveToFile(ActionEvent e) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Applicant a : applicants) {
                bw.write(a.toCsv());
                bw.newLine();
            }
            outputArea.append("Applicants saved to " + DATA_FILE + "\n");
        } catch (IOException ex) {
            showError("Error while saving: " + ex.getMessage());
        }
    }

    private void handleLoadFromFile(ActionEvent e) {
        List<Applicant> loaded = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Applicant a = applicantFromCsv(line);
                if (a != null) {
                    loaded.add(a);
                }
            }
            applicants = loaded; // replace current list
            outputArea.append("Loaded " + applicants.size() + " applicants from " + DATA_FILE + "\n");
        } catch (IOException ex) {
            showError("Error while loading: " + ex.getMessage());
        }
    }

    // ===== Helper methods =====

    private Applicant applicantFromCsv(String line) {
        String[] p = line.split(",");
        if (p.length != 8) return null;

        try {
            String name = p[0];
            int age = Integer.parseInt(p[1]);
            double income = Double.parseDouble(p[2]);
            EmploymentType type = EmploymentType.valueOf(p[3]);
            double loanAmount = Double.parseDouble(p[4]);
            int years = Integer.parseInt(p[5]);
            double rate = Double.parseDouble(p[6]);
            String bankName = p[7];

            if (type == EmploymentType.SALARIED) {
                return new SalariedApplicant(name, age, income, type, loanAmount, years, rate, bankName);
            } else if (type == EmploymentType.SELF_EMPLOYED) {
                return new SelfEmployedApplicant(name, age, income, type, loanAmount, years, rate, bankName);
            } else {
                return new StudentApplicant(name, age, income, type, loanAmount, years, rate, bankName);
            }
        } catch (Exception ex) {
            // If any parsing error occurs, skip that line
            return null;
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ===== MAIN METHOD (GUI ENTRY POINT) =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CreditScoreAppGUI gui = new CreditScoreAppGUI();
            gui.setVisible(true);
        });
    }
}