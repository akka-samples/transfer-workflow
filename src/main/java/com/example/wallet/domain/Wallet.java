package com.example.wallet.domain;

public record Wallet(String id, int balance) {

  public Wallet withdraw(int amount) {
    return new Wallet(id, balance - amount);
  }

  public Wallet deposit(int amount) {
    return new Wallet(id, balance + amount);
  }
}
