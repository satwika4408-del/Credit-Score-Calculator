import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ===== ENUMS =====
enum EmploymentType {
    SALARIED, SELF_EMPLOYED, STUDENT
}

enum RiskCategory {
    EXCELLENT, GOOD, FAIR, POOR
}

// ===== UTILITY CLASS =====
class CreditUtils {

    // Simple interest
    public static double calculateTotalAmountSimple(double loanAmount,
                                                    int years,
                                                    double yearlyInterestRate) {
        return loanAmount + (loanAmount * yearlyInterestRate * years);
    }

    // Recursive compound calculation (recursion example)
    public static double calculateTotalAmountCompound(double loanAmount,
                                                      int years,
                                                      double yearlyInterestRate) {
        if (years == 0) {
            return loanAmount;
        }
        double nextAmount = loanAmount * (1 + yearlyInterestRate);
        return calculateTotalAmountCompound(nextAmount, years - 1, yearlyInterestRate);
    }

    // Classify by Debt-to-Income ratio
    public static RiskCategory classifyByDTI(double dti) {
        if (dti <= 0.20) {
            return RiskCategory.EXCELLENT;
        } else if (dti <= 0.35) {
            return RiskCategory.GOOD;
        } else if (dti <= 0.50) {
            return RiskCategory.FAIR;
        } else {
            return RiskCategory.POOR;
        }
    }

    // Bitwise risk flags
    // bit0 – high DTI, bit1 – low income, bit2 – risky age
    public static int calculateRiskFlags(double dti, double income, int age) {
        int flags = 0;
        if (dti > 0.40) flags |= 1;          // 0001
        if (income < 25000) flags |= 2;      // 0010
        if (age < 21 || age > 60) flags |= 4;// 0100
        return flags;
    }
}

// ===== INTERFACE =====
interface CreditScorable {
    RiskCategory evaluateRisk();
}

// ===== ABSTRACT BASE CLASS =====
abstract class Applicant implements CreditScorable {

    protected String name;
    protected int age;
    protected double monthlyIncome;
    protected EmploymentType employmentType;
    protected double loanAmount;
    protected int years;
    protected double yearlyInterestRate;
    protected String bankName;

    public Applicant(String name, int age, double monthlyIncome,
                     EmploymentType employmentType, double loanAmount,
                     int years, double yearlyInterestRate, String bankName) {
        this.name = name;
        this.age = age;
        this.monthlyIncome = monthlyIncome;
        this.employmentType = employmentType;
        this.loanAmount = loanAmount;
        this.years = years;
        this.yearlyInterestRate = yearlyInterestRate;
        this.bankName = bankName;
    }

    // Debt-to-income ratio
    public double calculateDTI() {
        double totalPayable = CreditUtils
                .calculateTotalAmountSimple(loanAmount, years, yearlyInterestRate);
        double monthlyEmi = totalPayable / (years * 12.0);
        return monthlyEmi / monthlyIncome;
    }

    public int calculateRiskFlags() {
        return CreditUtils.calculateRiskFlags(calculateDTI(), monthlyIncome, age);
    }

    public String toCsv() {
        return name + "," + age + "," + monthlyIncome + "," + employmentType +
               "," + loanAmount + "," + years + "," + yearlyInterestRate + "," + bankName;
    }

    public String basicDisplay() {
        return String.format("Name: %-10s | Bank: %-10s | Loan: %.2f | Years: %d | Rate: %.2f%%",
                name, bankName, loanAmount, years, yearlyInterestRate * 100);
    }
}

// ===== SUBCLASSES (POLYMORPHISM) =====
class SalariedApplicant extends Applicant {
    public SalariedApplicant(String name, int age, double monthlyIncome,
                             EmploymentType employmentType, double loanAmount,
                             int years, double yearlyInterestRate, String bankName) {
        super(name, age, monthlyIncome, employmentType, loanAmount, years,
              yearlyInterestRate, bankName);
    }

    @Override
    public RiskCategory evaluateRisk() {
        double dti = calculateDTI();
        return CreditUtils.classifyByDTI(dti);
    }
}

class SelfEmployedApplicant extends Applicant {
    public SelfEmployedApplicant(String name, int age, double monthlyIncome,
                                 EmploymentType employmentType, double loanAmount,
                                 int years, double yearlyInterestRate, String bankName) {
        super(name, age, monthlyIncome, employmentType, loanAmount, years,
              yearlyInterestRate, bankName);
    }

    @Override
    public RiskCategory evaluateRisk() {
        double dti = calculateDTI() * 1.1; // a bit more risky
        return CreditUtils.classifyByDTI(dti);
    }
}

class StudentApplicant extends Applicant {
    public StudentApplicant(String name, int age, double monthlyIncome,
                            EmploymentType employmentType, double loanAmount,
                            int years, double yearlyInterestRate, String bankName) {
        super(name, age, monthlyIncome, employmentType, loanAmount, years,
              yearlyInterestRate, bankName);
    }

    @Override
    public RiskCategory evaluateRisk() {
        double dti = calculateDTI() * 1.2; // more conservative
        return CreditUtils.classifyByDTI(dti);
    }
}

// ===== MAIN APP CLASS =====
public class CreditScoreApp {

    private static final String DATA_FILE = "credit_profiles.txt";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Applicant> applicants = new ArrayList<>();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = safeReadInt(sc, "Enter your choice: ");

            switch (choice) {
                case 1:
                    addApplicant(sc, applicants);
                    break;
                case 2:
                    displayApplicants(applicants);
                    break;
                case 3:
                    saveToFile(applicants);
                    break;
                case 4:
                    applicants = loadFromFile();
                    break;
                case 5:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        sc.close();
        System.out.println("Thank you for using Smart Credit Score Predictor.");
    }

    private static void printMenu() {
        System.out.println("\n===== Smart Credit Score Predictor =====");
        System.out.println("1. Add Applicant Profile");
        System.out.println("2. View All Applicants & Statistics");
        System.out.println("3. Save Applicants to File");
        System.out.println("4. Load Applicants from File");
        System.out.println("5. Exit");
    }

    // ---------- Input helpers with exception handling ----------
    private static int safeReadInt(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static double safeReadDouble(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            try {
                return Double.parseDouble(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static EmploymentType readEmploymentType(Scanner sc) {
        while (true) {
            System.out.println("Employment Type: 1.Salaried  2.Self-Employed  3.Student");
            int choice = safeReadInt(sc, "Choose (1-3): ");
            switch (choice) {
                case 1:
                    return EmploymentType.SALARIED;
                case 2:
                    return EmploymentType.SELF_EMPLOYED;
                case 3:
                    return EmploymentType.STUDENT;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }

    // ---------- Core functionality ----------
    private static void addApplicant(Scanner sc, List<Applicant> applicants) {
        System.out.println("\n--- Add Applicant ---");
        System.out.print("Name: ");
        String name = sc.nextLine();

        int age = safeReadInt(sc, "Age: ");
        double income = safeReadDouble(sc, "Monthly Income: ");
        EmploymentType type = readEmploymentType(sc);
        double loanAmount = safeReadDouble(sc, "Loan Amount: ");
        int years = safeReadInt(sc, "Repayment Period (years): ");
        double rate = safeReadDouble(sc,
                "Yearly Interest Rate (decimal, e.g., 0.08 for 8%): ");
        System.out.print("Bank Name: ");
        String bankName = sc.nextLine();

        Applicant applicant;
        if (type == EmploymentType.SALARIED) {
            applicant = new SalariedApplicant(
                    name, age, income, type, loanAmount, years, rate, bankName);
        } else if (type == EmploymentType.SELF_EMPLOYED) {
            applicant = new SelfEmployedApplicant(
                    name, age, income, type, loanAmount, years, rate, bankName);
        } else {
            applicant = new StudentApplicant(
                    name, age, income, type, loanAmount, years, rate, bankName);
        }

        applicants.add(applicant);
        System.out.println("Applicant added successfully.");
    }

    // Uses array + simple analysis
    private static void displayApplicants(List<Applicant> applicants) {
        if (applicants.isEmpty()) {
            System.out.println("No applicants to display.");
            return;
        }

        System.out.println("\n--- All Applicants ---");
        double[] dtiArray = new double[applicants.size()];

        for (int i = 0; i < applicants.size(); i++) {
            Applicant a = applicants.get(i);
            double dti = a.calculateDTI();
            dtiArray[i] = dti;

            RiskCategory risk = a.evaluateRisk();
            int flags = a.calculateRiskFlags();
            String binaryFlags = String.format("%3s", Integer.toBinaryString(flags))
                    .replace(' ', '0');

            System.out.println(a.basicDisplay());
            System.out.printf("   DTI: %.3f | Risk: %-9s | Flags(bits): %s%n",
                    dti, risk, binaryFlags);
        }

        double min = dtiArray[0], max = dtiArray[0], sum = 0;
        for (double v : dtiArray) {
            if (v < min) min = v;
            if (v > max) max = v;
            sum += v;
        }
        double avg = sum / dtiArray.length;

        System.out.printf("%nDTI Statistics -> Min: %.3f  Max: %.3f  Avg: %.3f%n",
                min, max, avg);

        long excellentCount = applicants.stream()
                .filter(a -> a.evaluateRisk() == RiskCategory.EXCELLENT)
                .count();
        System.out.println("Number of EXCELLENT profiles: " + excellentCount);
    }

    // ---------- File I/O ----------
    private static void saveToFile(List<Applicant> applicants) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Applicant a : applicants) {
                bw.write(a.toCsv());
                bw.newLine();
            }
            System.out.println("Applicants saved to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Error while saving: " + e.getMessage());
        }
    }

    private static List<Applicant> loadFromFile() {
        List<Applicant> applicants = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Applicant a = applicantFromCsv(line);
                if (a != null) {
                    applicants.add(a);
                }
            }
            System.out.println("Loaded " + applicants.size() +
                               " applicants from " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Error while loading: " + e.getMessage());
        }
        return applicants;
    }

    private static Applicant applicantFromCsv(String line) {
        String[] p = line.split(",");
        if (p.length != 8) return null;

        String name = p[0];
        int age = Integer.parseInt(p[1]);
        double income = Double.parseDouble(p[2]);
        EmploymentType type = EmploymentType.valueOf(p[3]);
        double loanAmount = Double.parseDouble(p[4]);
        int years = Integer.parseInt(p[5]);
        double rate = Double.parseDouble(p[6]);
        String bankName = p[7];

        if (type == EmploymentType.SALARIED) {
            return new SalariedApplicant(name, age, income, type,
                    loanAmount, years, rate, bankName);
        } else if (type == EmploymentType.SELF_EMPLOYED) {
            return new SelfEmployedApplicant(name, age, income, type,
                    loanAmount, years, rate, bankName);
        } else {
            return new StudentApplicant(name, age, income, type,
                    loanAmount, years, rate, bankName);
        }
    }
}