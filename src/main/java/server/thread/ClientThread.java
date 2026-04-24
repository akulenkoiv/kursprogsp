package server.thread;

import dao.BudgetDAO;
import dao.impl.BudgetDAOImpl;
import enums.BudgetStatus;
import enums.RequestType;
import enums.ResponseStatus;
import models.dto.*;
import models.entities.*;
import models.tcp.Request;
import models.tcp.Response;
import services.BudgetService;
import services.TaxService;
import services.TransactionService;
import services.UserService;
import utility.GsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

public class ClientThread implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private UserService userService = new UserService(); //6 нет race condition (ош одновр доступ)
    private TransactionService transactionService = new TransactionService();
    private TaxService taxService = new TaxService();
    private BudgetService budgetService = new BudgetService();
    private User currentUser; //6 (каждый ClientThread свое поле <-)

    public ClientThread(Socket socket) throws IOException { // потоки ввода-вывода
        this.clientSocket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() { // JSON - Request (д) Response - JSON
        try {
            String line;
            while ((line = in.readLine()) != null) {
                Request request = GsonUtil.getGson().fromJson(line, Request.class);
                Response response = processRequest(request);
                out.println(GsonUtil.getGson().toJson(response));
                out.flush(); // принудительная отправка
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Response processRequest(Request request) {
        try {
            switch (request.getRequestType()) {
                case LOGIN:
                    LoginDTO loginDto = GsonUtil.getGson().fromJson(request.getPayload(), LoginDTO.class);
                    User authUser = userService.login(loginDto.getLogin(), loginDto.getPassword());
                    if (authUser != null) {
                        currentUser = authUser;
                        authUser.setPasswordHash("");
                        return new Response(ResponseStatus.OK, "Login successful", GsonUtil.getGson().toJson(authUser));
                    }
                    return new Response(ResponseStatus.ERROR, "Invalid credentials", null);

                case REGISTER:
                    RegisterDTO regDto = GsonUtil.getGson().fromJson(request.getPayload(), RegisterDTO.class);
                    User newUser = new User();
                    newUser.setLogin(regDto.getLogin());
                    newUser.setPasswordHash(regDto.getPassword());
                    newUser.setRoleId(regDto.getRoleId());
                    userService.saveEntity(newUser);
                    return new Response(ResponseStatus.OK, "User registered", null);

                case GET_INCOMES:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DateRangeDTO dateRange = GsonUtil.getGson().fromJson(request.getPayload(), DateRangeDTO.class);
                    List<Income> incomes = transactionService.getIncomes(currentUser.getId(), dateRange.getStart(), dateRange.getEnd());
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(incomes));

                case ADD_INCOME:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    Income income = GsonUtil.getGson().fromJson(request.getPayload(), Income.class);
                    transactionService.registerIncome(income.getDate(), income.getAmount(), income.getComment(), currentUser.getId(), income.getCategoryId(), income.getCounterpartyId());
                    return new Response(ResponseStatus.OK, "Income added", null);

                case UPDATE_INCOME:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    UpdateTransactionDTO updateIncomeDto = GsonUtil.getGson().fromJson(request.getPayload(), UpdateTransactionDTO.class);
                    transactionService.updateIncome(updateIncomeDto.getId(), updateIncomeDto.getDate(), updateIncomeDto.getAmount(), updateIncomeDto.getComment(), updateIncomeDto.getCategoryId(), updateIncomeDto.getCounterpartyId());
                    return new Response(ResponseStatus.OK, "Income updated", null);

                case DELETE_INCOME:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DeleteRequestDTO deleteIncomeDto = GsonUtil.getGson().fromJson(request.getPayload(), DeleteRequestDTO.class);
                    transactionService.deleteIncome(deleteIncomeDto.getId());
                    return new Response(ResponseStatus.OK, "Income deleted", null);

                case GET_EXPENSES:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DateRangeDTO expRange = GsonUtil.getGson().fromJson(request.getPayload(), DateRangeDTO.class);
                    List<Expense> expenses = transactionService.getExpenses(currentUser.getId(), expRange.getStart(), expRange.getEnd());
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(expenses));

                case ADD_EXPENSE:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    Expense expense = GsonUtil.getGson().fromJson(request.getPayload(), Expense.class);
                    transactionService.registerExpense(expense.getDate(), expense.getAmount(), expense.getComment(), currentUser.getId(), expense.getCategoryId(), expense.getCounterpartyId());
                    return new Response(ResponseStatus.OK, "Expense added", null);

                case UPDATE_EXPENSE:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    UpdateTransactionDTO updateExpenseDto = GsonUtil.getGson().fromJson(request.getPayload(), UpdateTransactionDTO.class);
                    transactionService.updateExpense(updateExpenseDto.getId(), updateExpenseDto.getDate(), updateExpenseDto.getAmount(), updateExpenseDto.getComment(), updateExpenseDto.getCategoryId(), updateExpenseDto.getCounterpartyId());
                    return new Response(ResponseStatus.OK, "Expense updated", null);

                case DELETE_EXPENSE:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DeleteRequestDTO deleteExpenseDto = GsonUtil.getGson().fromJson(request.getPayload(), DeleteRequestDTO.class);
                    transactionService.deleteExpense(deleteExpenseDto.getId());
                    return new Response(ResponseStatus.OK, "Expense deleted", null);

                case CALCULATE_TAX:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    TaxCalculationRequestDTO taxReq = GsonUtil.getGson().fromJson(request.getPayload(), TaxCalculationRequestDTO.class);
                    TaxPayment payment = taxService.calculateTax(currentUser.getId(), taxReq.getStartDate(), taxReq.getEndDate(), taxReq.getRegime());
                    return new Response(ResponseStatus.OK, "Tax calculated", GsonUtil.getGson().toJson(payment));

                case CALCULATE_PROFIT:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    ProfitCalculationRequestDTO profitReq = GsonUtil.getGson().fromJson(request.getPayload(), ProfitCalculationRequestDTO.class);
                    ProfitCalculationResponseDTO profitResult = transactionService.calculateProfit(currentUser.getId(), profitReq.getStartDate(), profitReq.getEndDate());
                    return new Response(ResponseStatus.OK, "Profit calculated", GsonUtil.getGson().toJson(profitResult));

                case GET_PROFIT_LOSS_REPORT:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DateRangeDTO reportRange = GsonUtil.getGson().fromJson(request.getPayload(), DateRangeDTO.class);
                    ProfitLossReportDTO report = transactionService.generateProfitLossReport(currentUser.getId(), reportRange.getStart(), reportRange.getEnd());
                    return new Response(ResponseStatus.OK, "Report generated", GsonUtil.getGson().toJson(report));

                case GET_BUDGETS:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(budgetService.getUserBudgets(currentUser.getId())));

                case ADD_BUDGET:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    Budget budgetAdd = GsonUtil.getGson().fromJson(request.getPayload(), Budget.class);
                    System.out.println("=== СЕРВЕР: ПОЛУЧЕН БЮДЖЕТ ===");
                    System.out.println("Type from JSON: " + budgetAdd.getType());
                    System.out.println("Income cat ID: " + budgetAdd.getIncomeCategoryId());
                    System.out.println("Expense cat ID: " + budgetAdd.getExpenseCategoryId());

                    budgetAdd.setUserId(currentUser.getId());
                    budgetAdd.setStatus(BudgetStatus.DRAFT);


                    if (!"INCOME".equals(budgetAdd.getType()) && !"EXPENSE".equals(budgetAdd.getType())) {
                        throw new IllegalArgumentException("Invalid budget type: " + budgetAdd.getType());
                    }

                    budgetService.createBudget(budgetAdd);
                    System.out.println("Бюджет сохранён с типом: " + budgetAdd.getType());
                    return new Response(ResponseStatus.OK, "Budget added", null);
                case UPDATE_BUDGET:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    Budget budgetUpd = GsonUtil.getGson().fromJson(request.getPayload(), Budget.class);
                    budgetService.updateBudget(budgetUpd);
                    return new Response(ResponseStatus.OK, "Budget updated", null);

                case DELETE_BUDGET:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DeleteRequestDTO delBudgetDto = GsonUtil.getGson().fromJson(request.getPayload(), DeleteRequestDTO.class);
                    budgetService.deleteBudget(delBudgetDto.getId());
                    return new Response(ResponseStatus.OK, "Budget deleted", null);

                case APPROVE_BUDGET:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DeleteRequestDTO appBudgetDto = GsonUtil.getGson().fromJson(request.getPayload(), DeleteRequestDTO.class);
                    budgetService.approveBudget(appBudgetDto.getId());
                    return new Response(ResponseStatus.OK, "Budget approved", null);

                case GET_BUDGET_REPORT:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DateRangeDTO budgetReportRange = GsonUtil.getGson().fromJson(request.getPayload(), DateRangeDTO.class);
                    List<BudgetReportDTO> budgetReports = budgetService.generateBudgetReport(currentUser.getId(), budgetReportRange.getStart());
                    return new Response(ResponseStatus.OK, "Budget report generated", GsonUtil.getGson().toJson(budgetReports));

                case GET_INCOME_CATS:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(transactionService.getIncomeCategories()));

                case ADD_INCOME_CAT:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    IncomeCategory incCat = GsonUtil.getGson().fromJson(request.getPayload(), IncomeCategory.class);
                    transactionService.saveIncomeCategory(incCat);
                    return new Response(ResponseStatus.OK, "Income category added", null);

                case UPDATE_INCOME_CAT:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    IncomeCategory updIncCat = GsonUtil.getGson().fromJson(request.getPayload(), IncomeCategory.class);
                    transactionService.updateIncomeCategory(updIncCat);
                    return new Response(ResponseStatus.OK, "Income category updated", null);

                case DELETE_INCOME_CAT:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DeleteRequestDTO delIncDto = GsonUtil.getGson().fromJson(request.getPayload(), DeleteRequestDTO.class);
                    transactionService.deleteIncomeCategory(delIncDto.getId());
                    return new Response(ResponseStatus.OK, "Income category deleted", null);

                case GET_EXPENSE_CATS:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(transactionService.getExpenseCategories()));

                case ADD_EXPENSE_CAT:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    ExpenseCategory expCat = GsonUtil.getGson().fromJson(request.getPayload(), ExpenseCategory.class);
                    transactionService.saveExpenseCategory(expCat);
                    return new Response(ResponseStatus.OK, "Expense category added", null);

                case UPDATE_EXPENSE_CAT:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    ExpenseCategory updExpCat = GsonUtil.getGson().fromJson(request.getPayload(), ExpenseCategory.class);
                    transactionService.updateExpenseCategory(updExpCat);
                    return new Response(ResponseStatus.OK, "Expense category updated", null);

                case DELETE_EXPENSE_CAT:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DeleteRequestDTO delExpDto = GsonUtil.getGson().fromJson(request.getPayload(), DeleteRequestDTO.class);
                    transactionService.deleteExpenseCategory(delExpDto.getId());
                    return new Response(ResponseStatus.OK, "Expense category deleted", null);

                case GET_COUNTERPARTIES:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(transactionService.getCounterparties()));

                case ADD_COUNTERPARTY:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    Counterparty cp = GsonUtil.getGson().fromJson(request.getPayload(), Counterparty.class);
                    transactionService.saveCounterparty(cp);
                    return new Response(ResponseStatus.OK, "Counterparty added", null);

                case UPDATE_COUNTERPARTY:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    Counterparty updCp = GsonUtil.getGson().fromJson(request.getPayload(), Counterparty.class);
                    transactionService.updateCounterparty(updCp);
                    return new Response(ResponseStatus.OK, "Counterparty updated", null);

                case DELETE_COUNTERPARTY:
                    if (currentUser == null) throw new SecurityException("Not logged in");
                    DeleteRequestDTO delCpDto = GsonUtil.getGson().fromJson(request.getPayload(), DeleteRequestDTO.class);
                    transactionService.deleteCounterparty(delCpDto.getId());
                    return new Response(ResponseStatus.OK, "Counterparty deleted", null);

                default:
                    return new Response(ResponseStatus.ERROR, "Unknown request type", null);
            }
        } catch (Exception e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }
}