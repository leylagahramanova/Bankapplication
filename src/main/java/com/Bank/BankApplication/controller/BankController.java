package com.Bank.BankApplication.controller;

import com.Bank.BankApplication.entity.Account;
import com.Bank.BankApplication.entity.Card;
import com.Bank.BankApplication.entity.Transaction;
import com.Bank.BankApplication.repository.AccountRepository;
import com.Bank.BankApplication.repository.CardRepository;
import com.Bank.BankApplication.repository.TransactionRepository;
import com.Bank.BankApplication.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class BankController {
    @Autowired
    private AccountService accountService;
    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private CardRepository cardRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account account = accountService.findAccountByUsername(username);

            model.addAttribute("account", account);
            model.addAttribute("cards", account.getCards());  // List of user's cards
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error";  // Show a generic error page or message
        }
        return "dashboard";
    }


    @GetMapping("/register")
    public String showRegistration() {
        return "register";
    }

    @PostMapping("/register")
    public String registerAccount(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam String cardNumber,
                                  @RequestParam String cardType,
                                  @RequestParam String cardName,
                                  @RequestParam double balance,
                                  Model model) {
        try {
            accountService.registerAccount(username, password,cardName, cardNumber, cardType, balance); // Updated to include card details
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @PostMapping("/add-card")
    public String addCard(@RequestParam String cardName,
                          @RequestParam String cardNumber,
                          @RequestParam String cardType,
                          @RequestParam double balance,
                          Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);

        // Create a new card instance with the provided details
        Card newCard = new Card(cardNumber, cardName, cardType, balance, null); // Account will be set in the service

        // Call the service method to add the card to the account
        accountService.addCardToAccount(account, newCard);

        return "redirect:/dashboard"; // Redirect to the dashboard or appropriate page
    }
    @PostMapping("/deposit")
    public String deposit(@RequestParam double amount, @RequestParam Long cardId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);
        Card card = accountService.findCardById(cardId);  // Fetch the correct card by ID
        // Perform deposit on the card
        accountService.deposit(account, card, amount);
        return "redirect:/dashboard";
    }
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam double amount, @RequestParam Long cardId, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);
        Card card = accountService.findCardById(cardId);

        try {
            accountService.withdraw(account,card, amount);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("account", account);
            model.addAttribute("cards", account.getCards());
            return "dashboard"; // Return to the dashboard with error
        }
        return "redirect:/dashboard"; // Redirect on success
    }
    @GetMapping("/transactions")
    public String transactionHistory(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);
        model.addAttribute("transactions", accountService.getTransactionHistory(account));
   return "transactions";
    }
    @PostMapping("/transfer")
    public String transferAmount(
            @RequestParam Long cardId,
            @RequestParam String accountUsername, // Ensure this matches HTML input name
            @RequestParam double amount,
            Model model
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account fromAccount = accountService.findAccountByUsername(username);
        Card fromCard = accountService.findCardById(cardId);

        if (amount <= 0) {
            model.addAttribute("error", "Amount must be greater than zero.");
            model.addAttribute("card", fromCard);
            return "dashboard";
        }

        try {
            accountService.transferAmount(fromAccount, fromCard, accountUsername, amount);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("card", fromCard);
            return "dashboard";
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/account/cards/username/{username}")
    @ResponseBody
    public List<Card> getCardsByAccountUsername(@PathVariable String username) {
        Account account = accountService.findAccountByUsername(username);
        return account.getCards();
    }
}