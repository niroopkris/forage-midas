package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import  com.jpmc.midascore.foundation.Transaction;

@Service
public class TransactionService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    TransactionRepository transactionRepository;

    public void validate(Transaction transaction)
    {

        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender == null || recipient == null ||  sender.getBalance() < transaction.getAmount()) {
            System.out.println("Invalid transaction from " + sender + " to " + recipient);
            return;
        }


        //TransactionRecord record = new TransactionRecord();
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());
        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord(transaction.getAmount(), sender, recipient);
        transactionRepository.save(record);
        System.out.println("Valid transaction occurred. From " + sender + " to " + recipient);
    }
}
