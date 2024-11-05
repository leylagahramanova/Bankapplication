package com.Bank.BankApplication.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cardNumber;private String cardName;
    private String cardType;  // e.g., CREDIT, DEBIT
    private double balance;  // New field to store the balance
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    // Constructors
    public Card() {}
    public Card(String cardNumber,String cardName, String cardType, double balance,   Account account) {
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.cardName = cardName;
        this.balance = balance;
        this.account = account;
    }


    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    public String getCardType() {return cardType;}
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public Account getAccount() {
        return account;
    }
    public void setAccount(Account account) {
        this.account = account;
    }
}