package expenseListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


class Expense {


    private Date date;
    private double amount;
    private String category;
    private String description;

    public Expense(Date date, double amount, String category, String description) {
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.description = description;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Date: " + date + ", Amount: " + amount + ", Category: " + category + ", Description: " + description;
    }
}

public class ExpenseTrackerApp {


    private List<Expense> expenses = new ArrayList<>();


    private String[] categories = {"Food", "Travel", "Shopping","Fuel","General","Entertainment"};

    private JFrame frame;
    private JTextField dateField;
    private JTextField amountField;
    private JComboBox<String> categoryComboBox;
    private JTextArea descriptionArea;
    private JTextArea expenseListArea;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ExpenseTrackerApp() {

        createAndShowGUI();

    }

    private void createAndShowGUI() {
        frame = new JFrame("Expense Tracker App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(7, 2, 10, 10));

        frame.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        frame.add(dateField);

        frame.add(new JLabel("Amount:"));
        amountField = new JTextField();
        frame.add(amountField);

        frame.add(new JLabel("Category:"));
        categoryComboBox = new JComboBox<>(categories);
        frame.add(categoryComboBox);

        frame.add(new JLabel("Description:"));
        descriptionArea = new JTextArea(3, 20);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        frame.add(scrollPane);

        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addExpense();
            }
        });
        frame.add(addButton);

        // View Expenses Button
        JButton viewExpensesButton = new JButton("View Expenses");
        viewExpensesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewExpensesFromDatabase(); //
            }
        });
        frame.add(viewExpensesButton);

        // View Categories Button
        JButton viewCategoriesButton = new JButton("View Categories");
        viewCategoriesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewCategories();
            }
        });
        frame.add(viewCategoriesButton);

        //DeleteAllEntries Button
        JButton deleteAllButton = new JButton("Delete All Entries");
        deleteAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAllEntries();
            }
        });
        frame.add(deleteAllButton);

        expenseListArea = new JTextArea();

        frame.pack();
        frame.setVisible(true);
    }

    private void addExpense() {
        String dateString = dateField.getText();
        double amount = Double.parseDouble(amountField.getText());
        String category = categoryComboBox.getSelectedItem().toString();
        String description = descriptionArea.getText();
        if(amount<1)
        {
            showMessage("Invalid Amount!");
            amountField.setText(null);
            amountField.grabFocus();
        }
        else {
            try {
                Date date = dateFormat.parse(dateString);
                Expense expense = new Expense(date, amount, category, description);
                expenses.add(expense);
//            showMessage("Expense recorded successfully.");
                insertExpenseIntoDatabase(expense);
            } catch (ParseException ex) {
                showMessage("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }
    private void viewExpenses() {
        StringBuilder sb = new StringBuilder("Expenses:\n");
        for (Expense expense : expenses) {
            sb.append(expense).append("\n");
        }
        showExpenseDialog(sb.toString());
        expenseListArea.setText(sb.toString());
    }

    private void viewCategories() {
        StringBuilder sb = new StringBuilder("Categories:\n");
        for (String category : categories) {
            sb.append("- ").append(category).append("\n");
        }
        showCategoryDialog(sb.toString());
        expenseListArea.setText(sb.toString());
    }


    private void deleteAllEntries() {
        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete all entries?", "Confirm Delete All", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            deleteAllEntriesFromDatabase();
            expenses.clear();
            expenseListArea.setText(null);
            //showMessage("All entries have been deleted.");
        }
    }

    private void deleteAllEntriesFromDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/New", "root", "12345")) {
            String sql = "DELETE FROM Expense";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                showMessage("All entries have been deleted.");
            }
            else {
                showMessage("No entries found in the database.");
            }
        } catch (SQLException e) {
            showMessage("An error occurred while deleting all entries from the database.");
            e.printStackTrace();
        }
    }

    // Initialize the expenseListArea

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    private void insertExpenseIntoDatabase(Expense expense) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/New", "root", "12345")) {
            String sql = "INSERT INTO Expense (ExDATE, Amount, categ, Descrip) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setDate(1, new java.sql.Date(expense.getDate().getTime()));
            preparedStatement.setDouble(2, expense.getAmount());
            preparedStatement.setString(3, expense.getCategory());
            preparedStatement.setString(4, expense.getDescription());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                showMessage("Expense recorded Successfully.");
            } else {
                showMessage("Expense recording failed.");
            }

        } catch (SQLException e) {
            showMessage("An error occurred while inserting the expense into the database.");
            e.printStackTrace();
        }
    }
    private void viewExpensesFromDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/New", "root", "12345")) {
            String sql = "SELECT ExDATE, Amount, categ,Descrip FROM Expense";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

             expenses.clear(); //For Clear Current Expenses List

            StringBuilder sb = new StringBuilder("Total Expenses:\n");
            while (resultSet.next()) {
                Date date = resultSet.getDate("ExDATE");
                double amount = resultSet.getDouble("Amount");
                String category = resultSet.getString("categ");
                String description = resultSet.getString("Descrip");

//                Expense expense = new Expense(date, amount, category, description);
                Expense expense = new Expense(date, amount, category, description);
                sb.append(expense).append("\n");
            }
            //viewExpenses();
            showExpenseDialog(sb.toString());

        } catch (SQLException e) {
            showMessage("An error occurred while fetching expenses from the database.");
            e.printStackTrace();
        }


    }

    private void showExpenseDialog(String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        JOptionPane.showMessageDialog(frame, scrollPane, "View Expenses", JOptionPane.PLAIN_MESSAGE);
    }
    private void showCategoryDialog(String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        JOptionPane.showMessageDialog(frame, scrollPane, "View Categories", JOptionPane.PLAIN_MESSAGE);
    }


    public static void main(String[] args) {

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connect=DriverManager.getConnection("jdbc:mysql://localhost:3306/New","root","12345");
            Statement stmt=connect.createStatement();
            //String sql = "Insert into Expense" + "(ExDATE,Amount,categ,Descrip)";

            System.out.println("Connected");
            connect.close();
        }
        catch(Exception e){ System.out.println(e);}

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ExpenseTrackerApp();
            }
        });
    }


}
