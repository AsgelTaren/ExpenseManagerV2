package net.app;

import java.util.Collection;
import java.util.List;

import net.transaction.Transaction;

public interface WorkablePanel {

	public List<Transaction> getWorkedOnTransactions();

	public void addTransactionsToWork(Collection<Transaction> data);

	public void refresh();

}