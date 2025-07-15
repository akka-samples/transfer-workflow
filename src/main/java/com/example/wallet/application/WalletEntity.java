package com.example.wallet.application;

import static akka.Done.done;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import com.example.wallet.domain.Wallet;
import com.example.wallet.domain.WalletEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("wallet")
public class WalletEntity extends EventSourcedEntity<Wallet, WalletEvent> {

  private static final Logger logger = LoggerFactory.getLogger(WalletEntity.class);

  @Override
  public Wallet applyEvent(WalletEvent event) {
    return switch (event) {
      case WalletEvent.Created c -> new Wallet(eventContext().entityId(), c.initialBalance());
      case WalletEvent.Withdrawn w -> currentState().withdraw(w.amount());
      case WalletEvent.Deposited d -> currentState().deposit(d.amount());
    };
  }

  public Effect<Done> create(int initialBalance) { // <1>
    if (currentState() != null) {
      return effects().error("Wallet already exists");
    } else {
      return effects()
        .persist(new WalletEvent.Created(initialBalance))
        .thenReply(__ -> done());
    }
  }

  public Effect<Done> withdraw(int amount) { // <2>
    if (currentState() == null) {
      return effects().error("Wallet does not exist");
    } else if (currentState().balance() < amount) {
      return effects().error("Insufficient balance");
    } else {
      logger.info("Withdraw walletId: [{}] amount -{}", currentState().id(), amount);
      return effects().persist(new WalletEvent.Withdrawn(amount)).thenReply(__ -> done());
    }
  }

  public Effect<Done> deposit(int amount) { // <3>
    logger.info("Deposit walletId: [{}] amount +{}", currentState().id(), amount);
    if (currentState() == null) {
      return effects().error("Wallet does not exist");
    } else {
      return effects().persist(new WalletEvent.Deposited(amount)).thenReply(__ -> done());
    }
  }

  public Effect<Integer> get() { // <4>
    if (currentState() == null) {
      return effects().error("Wallet does not exist");
    } else {
      return effects().reply(currentState().balance());
    }
  }
}
