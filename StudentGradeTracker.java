import java.util.ArrayList;
import java.util.Scanner;

class Student {
    private String name;
    private double grade;

    public Student(String name, double grade) {
        this.name = name;
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public double getGrade() {
        return grade;
    }
}

public class StudentGradeTracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Student> students = new ArrayList<>();

        System.out.println("===== Student Grade Tracker =====");
        System.out.println("Enter student details. Type 'exit' as name to finish.");

        // Keep taking input until user types "exit"
        while (true) {
            System.out.print("\nEnter Student Name (or type 'exit' to stop): ");
            String name = scanner.nextLine();

            if (name.equalsIgnoreCase("exit")) {
                break; // stop input
            }

            System.out.print("Enter Grade: ");
            double grade = scanner.nextDouble();
            scanner.nextLine(); // clear buffer

            students.add(new Student(name, grade));
        }

        // If no students entered
        if (students.isEmpty()) {
            System.out.println("\nNo students entered. Exiting...");
            return;
        }

        // Calculate stats
        double total = 0;
        double highest = Double.MIN_VALUE;
        double lowest = Double.MAX_VALUE;
        String topStudent = "";
        String lowStudent = "";

        for (Student s : students) {
            total += s.getGrade();

            if (s.getGrade() > highest) {
                highest = s.getGrade();
                topStudent = s.getName();
            }
            if (s.getGrade() < lowest) {
                lowest = s.getGrade();
                lowStudent = s.getName();
            }
        }

        double average = total / students.size();

        // Display Report
        System.out.println("\n===== Grade Summary Report =====");
        for (Student s : students) {
            System.out.println(s.getName() + " -> " + s.getGrade());
        }
        System.out.println("--------------------------------");
        System.out.printf("Average Score: %.2f%n", average);
        System.out.println("Highest Score: " + highest + " ( " + topStudent + " )");
        System.out.println("Lowest Score : " + lowest + " ( " + lowStudent + " )");
        System.out.println("================================");

        scanner.close();
    }
}
