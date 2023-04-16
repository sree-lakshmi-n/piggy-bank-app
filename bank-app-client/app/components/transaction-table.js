import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';

export default class TransactionTableComponent extends Component {
  @service('auth') auth;
  @tracked transactions = this.auth.transactions;
  transactionTypes = ['All', 'Deposit', 'Withdraw', 'Transfer'];

  @action hi() {
    console.log(this.transactions);
    console.log(this.auth.transactions);
  }

  @action handleTypeFilter(event) {
    const transactionType =
      event.target.nextElementSibling.textContent.toLowerCase();
    if (transactionType === 'all') {
      this.transactions = this.auth.transactions;
    } else {
      this.transactions = this.auth.transactions.filter(
        (transaction) => transaction.transactionType === transactionType
      );
    }
  }
}
