package com.Bank.BankApplication.service;

import com.Bank.BankApplication.entity.Account;
import com.Bank.BankApplication.entity.Card;
import com.Bank.BankApplication.entity.Transaction;
import com.Bank.BankApplication.repository.AccountRepository;
import com.Bank.BankApplication.repository.CardRepository;
import com.Bank.BankApplication.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Account findAccountByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found!"));
    }

    public Account registerAccount(String username, String password, String cardName, String cardNumber, String cardType, double balance) {
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists.");
        }

        // Create and save the account
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        Account savedAccount = accountRepository.save(account);

        // Create and save the card
        Card card = new Card( cardNumber,cardName,cardType, balance, savedAccount); // Create a new card with account association
        cardRepository.save(card);

        return savedAccount;
    }
    public Optional<Account> getAccount(Long id) {
        return accountRepository.findById(id);
    }

    public Card deposit(Account account, Card card, double amount) {
        // Ensure the amount is greater than 0
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than 0");
        }
        // Update the card balance
        card.setBalance(card.getBalance() + amount); // Use simple addition for double
        Transaction transaction = new Transaction(
                amount,
                "Deposit",
                LocalDateTime.now(),
                account // Use account directly if you need to link it to the transaction
        );
        transactionRepository.save(transaction);
        return cardRepository.save(card); // Save the updated card
    }
    public Card withdraw(Account account, Card card, double amount) {
        // Ensure the amount is greater than 0
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than 0");
        }
        if (card.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds");
        }
        card.setBalance(card.getBalance() - amount);

        // Record the transaction
        Transaction transaction = new Transaction(
                amount,
                "Withdrawal",
                LocalDateTime.now(),
                account  // Link to the account associated with this card
        );
        transactionRepository.save(transaction);

        return cardRepository.save(card); // Save the updated card balance
    }

    public List<Transaction> getTransactionHistory(Account account) {
        return transactionRepository.findByAccountId(account.getId());
    }

    public Collection<? extends SimpleGrantedAuthority> authorities() {
        return Arrays.asList(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = findAccountByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("Username or Password not found");
        }
        return account;  // Account implements UserDetails
    }
    public void transferAmount(Account fromAccount, Card fromCard, String toUsername, double amount) {
        // Check if the sender has enough balance
        if (fromCard.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds on the card");
        }

        // Find the recipient's account by username
        Account toAccount = accountRepository.findByUsername(toUsername)
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        // Choose a specific card from the recipient's account or modify this logic as needed
        Card toCard = toAccount.getCards().get(0); // Using the first card as an example

        // Deduct the amount from the sender's card and add it to the recipient's card
        fromCard.setBalance(fromCard.getBalance() - amount);
        toCard.setBalance(toCard.getBalance() + amount);

        // Save the updated card balances
        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // Record the transactions for both sender and recipient
        Transaction debitTransaction = new Transaction(amount, "Transfer Out to card " + toCard.getCardNumber(), LocalDateTime.now(), fromAccount);
        Transaction creditTransaction = new Transaction(amount, "Transfer In from card " + fromCard.getCardNumber(), LocalDateTime.now(), toAccount);
        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);
    }
    public Card findCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(()->new RuntimeException("Card with ID " + cardId + " not found!"));
    }
    public void addCardToAccount(Account account, Card newCard) {
        if (account != null && newCard != null) {
            // Set the account for the new card
            newCard.setAccount(account); // Establish the relationship

            // Save the new card directly
            cardRepository.save(newCard);

            // Optionally, you can add the card to the account's list
            account.addCard(newCard); // This will help maintain the list in the Account

            // Persist changes to the account, if necessary
            accountRepository.save(account); // This step might not be needed if cascading is set
        } else {
            throw new IllegalArgumentException("Account or newCard cannot be null");
        }
    }
}
