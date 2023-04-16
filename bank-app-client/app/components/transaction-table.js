import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { Evented } from '@ember/object/evented';

export default class TransactionTableComponent extends Component {
  @service('auth') auth;
  @tracked transactions = this.auth.transactions;
  @tracked entriesnum = this.transactions.length;
  transactionTypes = ['All', 'Deposit', 'Withdraw', 'Transfer'];
  @tracked fromDate = null;
  @tracked toDate = null;

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
    console.log(this.fromDate);
    console.log(this.toDate);
  }

  @action handleEntriesNumFilter(event) {
    let num = event.target.valueAsNumber || 0;
    num =
      num < 0
        ? 0
        : num > this.transactions.length
        ? this.transactions.length
        : num;
    console.log(num);
    this.entriesnum = num;
  }
  formatDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  compareDates = () => {
    new Date(this.fromDate) <= new Date(this.toDate);
  };

  @action handleFromDateFilter(event) {
    this.fromDate = event.target.value;
    console.log(this.fromDate);
  }
  @action handleToDateFilter(event) {
    this.toDate = event.target.value;
    console.log(this.toDate);
  }
}
