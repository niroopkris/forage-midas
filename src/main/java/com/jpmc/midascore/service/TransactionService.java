package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import  com.jpmc.midascore.foundation.Transaction;
import org.springframework.web.client.RestTemplate;

@Service
public class TransactionService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    TransactionRepository transactionRepository;

    RestTemplate restTemplate;

    public TransactionService(RestTemplateBuilder builder) {
        System.out.println("Inside TransactionService Constructor");
        this.restTemplate = builder.build();
    }

    public void validate(Transaction transaction)
    {
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender == null || recipient == null ||  sender.getBalance() < transaction.getAmount()) {
            System.out.println("Invalid transaction from " + sender + " to " + recipient);
            return;
        }

        Incentive incentive = restTemplate.postForObject("http://localhost:8080/incentive", transaction, Incentive.class);
        float incentiveAmt = 0;

        if (incentive != null) {
            incentiveAmt = incentive.getAmount();
        }

        //TransactionRecord record = new TransactionRecord();
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmt);
        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord(transaction.getAmount(), incentiveAmt, sender, recipient);
        transactionRepository.save(record);
        System.out.println("Valid transaction occurred. From " + sender + " to " + recipient);
    }
}
