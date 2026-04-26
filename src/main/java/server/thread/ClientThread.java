package server.thread;

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
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;

    private final UserService userService = new UserService();
    private final TransactionService transactionService = new TransactionService();
    private final TaxService taxService = new TaxService();
    private final BudgetService budgetService = new BudgetService();
    private User currentUser;

    public ClientThread(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                Response response = null;
                try {
                    Request request = GsonUtil.getGson().fromJson(line, Request.class);
                    response = processRequest(request);
                } catch (Throwable e) {
                    System.err.println("CRITICAL Error processing request: " + e.getMessage());
                    e.printStackTrace();
                    response = new Response(ResponseStatus.ERROR, "Server error: " + e.getMessage(), null);
                } finally {
                    if (response != null) {
                        try {
                            out.println(GsonUtil.getGson().toJson(response));
                            out.flush();
                        } catch (Exception e) {
                            System.err.println("Failed to send response: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (!e.getMessage().contains("Connection reset") && !e.getMessage().contains("Socket closed")) {
                System.err.println("Client I/O error: " + e.getMessage());
            }
        } finally {
            closeConnection();
        }
    }

    private Response processRequest(Request request) {
        try {
            RequestType type = request.getRequestType();
            String payload = request.getPayload();

            if (type == RequestType.LOGIN) {
                LoginDTO loginDto = GsonUtil.getGson().fromJson(payload, LoginDTO.class);
                User authUser = userService.login(loginDto.getLogin(), loginDto.getPassword());
                if (authUser != null) {
                    currentUser = authUser;
                    authUser.setPasswordHash("");
                    return new Response(ResponseStatus.OK, "Login successful", GsonUtil.getGson().toJson(authUser));
                }
                return new Response(ResponseStatus.ERROR, "Invalid credentials", null);
            }

            if (currentUser == null) {
                return new Response(ResponseStatus.ERROR, "User not authorized", null);
            }

            switch (type) {
                case GET_USERS:
                    if (!utility.RoleChecker.canManageUsers(currentUser))
                        return new Response(ResponseStatus.ERROR, "Insufficient permissions", null);
                    List<User> users = userService.getUsersForManager(currentUser);
                    users.forEach(u -> u.setPasswordHash(""));
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(users));

                case ADD_USER:
                    if (!utility.RoleChecker.canManageUsers(currentUser))
                        return new Response(ResponseStatus.ERROR, "Insufficient permissions", null);
                    User newUser = GsonUtil.getGson().fromJson(payload, User.class);
                    userService.saveEntity(newUser);
                    newUser.setPasswordHash("");
                    return new Response(ResponseStatus.OK, "User added", GsonUtil.getGson().toJson(newUser));

                case DELETE_USER:
                    if (!utility.RoleChecker.canManageUsers(currentUser))
                        return new Response(ResponseStatus.ERROR, "Insufficient permissions", null);
                    DeleteRequestDTO delUserDto = GsonUtil.getGson().fromJson(payload, DeleteRequestDTO.class);
                    if (delUserDto.getId() == currentUser.getId())
                        return new Response(ResponseStatus.ERROR, "Cannot delete self", null);
                    userService.deleteEntity(delUserDto.getId());
                    return new Response(ResponseStatus.OK, "User deleted", null);

                case REGISTER:
                    if (!utility.RoleChecker.canRegisterUsers(currentUser))
                        return new Response(ResponseStatus.ERROR, "Insufficient permissions", null);
                    RegisterDTO regDto = GsonUtil.getGson().fromJson(payload, RegisterDTO.class);
                    User registeredUser = new User();
                    registeredUser.setLogin(regDto.getLogin());
                    registeredUser.setPasswordHash(regDto.getPassword());
                    registeredUser.setRoleId(regDto.getRoleId());
                    userService.saveEntity(registeredUser);
                    registeredUser.setPasswordHash("");
                    return new Response(ResponseStatus.OK, "User registered", GsonUtil.getGson().toJson(registeredUser));

                case GET_INCOMES:
                    DateRangeDTO dateRange = GsonUtil.getGson().fromJson(payload, DateRangeDTO.class);
                    List<Income> incomes = transactionService.getIncomes(currentUser.getId(), dateRange.getStart(), dateRange.getEnd());
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(incomes));

                case ADD_INCOME:
                    Income income = GsonUtil.getGson().fromJson(payload, Income.class);
                    transactionService.registerIncome(income.getDate(), income.getAmount(), income.getComment(), currentUser.getId(), income.getCategoryId(), income.getCounterpartyId());
                    return new Response(ResponseStatus.OK, "Income added", null);

                case UPDATE_INCOME:
                    UpdateTransactionDTO updateIncomeDto = GsonUtil.getGson().fromJson(payload, UpdateTransactionDTO.class);
                    transactionService.updateIncome(updateIncomeDto.getId(), updateIncomeDto.getDate(), updateIncomeDto.getAmount(), updateIncomeDto.getComment(), updateIncomeDto.getCategoryId(), updateIncomeDto.getCounterpartyId());
                    return new Response(ResponseStatus.OK, "Income updated", null);

                case DELETE_INCOME:
                    DeleteRequestDTO deleteIncomeDto = GsonUtil.getGson().fromJson(payload, DeleteRequestDTO.class);
                    transactionService.deleteIncome(deleteIncomeDto.getId());
                    return new Response(ResponseStatus.OK, "Income deleted", null);

                case GET_EXPENSES:
                    DateRangeDTO expRange = GsonUtil.getGson().fromJson(payload, DateRangeDTO.class);
                    List<Expense> expenses = transactionService.getExpenses(currentUser.getId(), expRange.getStart(), expRange.getEnd());
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(expenses));

                case ADD_EXPENSE:
                    Expense expense = GsonUtil.getGson().fromJson(payload, Expense.class);
                    transactionService.registerExpense(expense.getDate(), expense.getAmount(), expense.getComment(), currentUser.getId(), expense.getCategoryId(), expense.getCounterpartyId());
                    return new Response(ResponseStatus.OK, "Expense added", null);

                case UPDATE_EXPENSE:
                    UpdateTransactionDTO updateExpenseDto = GsonUtil.getGson().fromJson(payload, UpdateTransactionDTO.class);
                    transactionService.updateExpense(updateExpenseDto.getId(), updateExpenseDto.getDate(), updateExpenseDto.getAmount(), updateExpenseDto.getComment(), updateExpenseDto.getCategoryId(), updateExpenseDto.getCounterpartyId());
                    return new Response(ResponseStatus.OK, "Expense updated", null);

                case DELETE_EXPENSE:
                    DeleteRequestDTO deleteExpenseDto = GsonUtil.getGson().fromJson(payload, DeleteRequestDTO.class);
                    transactionService.deleteExpense(deleteExpenseDto.getId());
                    return new Response(ResponseStatus.OK, "Expense deleted", null);

                case CALCULATE_TAX:
                    TaxCalculationRequestDTO taxReq = GsonUtil.getGson().fromJson(payload, TaxCalculationRequestDTO.class);
                    TaxPayment payment = taxService.calculateTax(currentUser.getId(), taxReq.getStartDate(), taxReq.getEndDate(), taxReq.getRegime());
                    return new Response(ResponseStatus.OK, "Tax calculated", GsonUtil.getGson().toJson(payment));

                case CALCULATE_PROFIT:
                    ProfitCalculationRequestDTO profitReq = GsonUtil.getGson().fromJson(payload, ProfitCalculationRequestDTO.class);
                    ProfitCalculationResponseDTO profitResult = transactionService.calculateProfit(currentUser.getId(), profitReq.getStartDate(), profitReq.getEndDate());
                    return new Response(ResponseStatus.OK, "Profit calculated", GsonUtil.getGson().toJson(profitResult));

                case GET_PROFIT_LOSS_REPORT:
                    DateRangeDTO reportRange = GsonUtil.getGson().fromJson(payload, DateRangeDTO.class);
                    ProfitLossReportDTO report = transactionService.generateProfitLossReport(currentUser.getId(), reportRange.getStart(), reportRange.getEnd());
                    return new Response(ResponseStatus.OK, "Report generated", GsonUtil.getGson().toJson(report));

                case GET_BUDGETS:
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(budgetService.getUserBudgets(currentUser.getId())));

                case ADD_BUDGET:
                    BudgetDTO budgetDto = GsonUtil.getGson().fromJson(payload, BudgetDTO.class);
                    Budget budgetEntity = new Budget();

                    budgetEntity.setPeriod(budgetDto.getPeriod());
                    budgetEntity.setType(budgetDto.getType());
                    budgetEntity.setPlannedAmount(budgetDto.getPlannedAmount());
                    budgetEntity.setStatus(budgetDto.getStatus() != null ?
                            BudgetStatus.valueOf(budgetDto.getStatus()) : BudgetStatus.DRAFT);

                    budgetEntity.setUserId(currentUser.getId());

                    if ("INCOME".equals(budgetDto.getType())) {
                        budgetEntity.setIncomeCategoryId(budgetDto.getCategoryId());
                        budgetEntity.setExpenseCategoryId(null);
                    } else {
                        budgetEntity.setExpenseCategoryId(budgetDto.getCategoryId());
                        budgetEntity.setIncomeCategoryId(null);
                    }

                    if (!"INCOME".equals(budgetEntity.getType()) && !"EXPENSE".equals(budgetEntity.getType())) {
                        throw new IllegalArgumentException("Invalid budget type: " + budgetEntity.getType());
                    }

                    budgetService.createBudget(budgetEntity);
                    return new Response(ResponseStatus.OK, "Budget added", null);

                case UPDATE_BUDGET:
                    BudgetDTO updateDto = GsonUtil.getGson().fromJson(payload, BudgetDTO.class);

                    if (updateDto.getId() == null) {
                        throw new IllegalArgumentException("Budget ID is required for update operation");
                    }

                    Budget updateEntity = new Budget();
                    updateEntity.setId(updateDto.getId());
                    updateEntity.setPeriod(updateDto.getPeriod());
                    updateEntity.setType(updateDto.getType());
                    updateEntity.setPlannedAmount(updateDto.getPlannedAmount());
                    updateEntity.setStatus(updateDto.getStatus() != null ?
                            BudgetStatus.valueOf(updateDto.getStatus()) : BudgetStatus.DRAFT);

                    if ("INCOME".equals(updateDto.getType())) {
                        updateEntity.setIncomeCategoryId(updateDto.getCategoryId());
                        updateEntity.setExpenseCategoryId(null);
                    } else {
                        updateEntity.setExpenseCategoryId(updateDto.getCategoryId());
                        updateEntity.setIncomeCategoryId(null);
                    }

                    budgetService.updateBudget(updateEntity);
                    return new Response(ResponseStatus.OK, "Budget updated", null);

                case DELETE_BUDGET:
                    DeleteRequestDTO delBudgetDto = GsonUtil.getGson().fromJson(payload, DeleteRequestDTO.class);
                    budgetService.deleteBudget(delBudgetDto.getId());
                    return new Response(ResponseStatus.OK, "Budget deleted", null);

                case APPROVE_BUDGET:
                    DeleteRequestDTO appBudgetDto = GsonUtil.getGson().fromJson(payload, DeleteRequestDTO.class);
                    budgetService.approveBudget(appBudgetDto.getId());
                    return new Response(ResponseStatus.OK, "Budget approved", null);

                case GET_BUDGET_REPORT:
                    DateRangeDTO budgetReportRange = GsonUtil.getGson().fromJson(payload, DateRangeDTO.class);
                    System.out.println("[Server] GET_BUDGET_REPORT: userId=" + currentUser.getId() + ", period=" + budgetReportRange.getStart());

                    List<BudgetReportDTO> budgetReports = budgetService.generateBudgetReport(
                            currentUser.getId(),
                            budgetReportRange.getStart()
                    );

                    System.out.println("[Server] Найдено бюджетов: " + budgetReports.size());
                    return new Response(ResponseStatus.OK, "Budget report generated", GsonUtil.getGson().toJson(budgetReports));

                case GET_INCOME_CATS:
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(transactionService.getIncomeCategories()));

                case ADD_INCOME_CAT:
                    IncomeCategory incCat = GsonUtil.getGson().fromJson(payload, IncomeCategory.class);
                    transactionService.saveIncomeCategory(incCat);
                    return new Response(ResponseStatus.OK, "Income category added", null);

                case UPDATE_INCOME_CAT:
                    IncomeCategory updIncCat = GsonUtil.getGson().fromJson(payload, IncomeCategory.class);
                    transactionService.updateIncomeCategory(updIncCat);
                    return new Response(ResponseStatus.OK, "Income category updated", null);

                case DELETE_INCOME_CAT:
                    DeleteRequestDTO delIncDto = GsonUtil.getGson().fromJson(payload, DeleteRequestDTO.class);
                    transactionService.deleteIncomeCategory(delIncDto.getId());
                    return new Response(ResponseStatus.OK, "Income category deleted", null);

                case GET_EXPENSE_CATS:
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(transactionService.getExpenseCategories()));

                case ADD_EXPENSE_CAT:
                    ExpenseCategory expCat = GsonUtil.getGson().fromJson(payload, ExpenseCategory.class);
                    transactionService.saveExpenseCategory(expCat);
                    return new Response(ResponseStatus.OK, "Expense category added", null);

                case UPDATE_EXPENSE_CAT:
                    ExpenseCategory updExpCat = GsonUtil.getGson().fromJson(payload, ExpenseCategory.class);
                    transactionService.updateExpenseCategory(updExpCat);
                    return new Response(ResponseStatus.OK, "Expense category updated", null);

                case DELETE_EXPENSE_CAT:
                    DeleteRequestDTO delExpDto = GsonUtil.getGson().fromJson(payload, DeleteRequestDTO.class);
                    transactionService.deleteExpenseCategory(delExpDto.getId());
                    return new Response(ResponseStatus.OK, "Expense category deleted", null);

                case GET_COUNTERPARTIES:
                    return new Response(ResponseStatus.OK, "Success", GsonUtil.getGson().toJson(transactionService.getCounterparties()));

                case ADD_COUNTERPARTY:
                    Counterparty cp = GsonUtil.getGson().fromJson(payload, Counterparty.class);
                    transactionService.saveCounterparty(cp);
                    return new Response(ResponseStatus.OK, "Counterparty added", null);

                case UPDATE_COUNTERPARTY:
                    Counterparty updCp = GsonUtil.getGson().fromJson(payload, Counterparty.class);
                    transactionService.updateCounterparty(updCp);
                    return new Response(ResponseStatus.OK, "Counterparty updated", null);

                case DELETE_COUNTERPARTY:
                    DeleteRequestDTO delCpDto = GsonUtil.getGson().fromJson(payload, DeleteRequestDTO.class);
                    transactionService.deleteCounterparty(delCpDto.getId());
                    return new Response(ResponseStatus.OK, "Counterparty deleted", null);

                default:
                    System.err.println("Unknown request type received: " + type);
                    return new Response(ResponseStatus.ERROR, "Unknown request type: " + type, null);
            }
        } catch (Exception e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }

    private void closeConnection() {
        try {
            if (out != null) {
                out.flush();
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.shutdownInput();
                clientSocket.shutdownOutput();
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}