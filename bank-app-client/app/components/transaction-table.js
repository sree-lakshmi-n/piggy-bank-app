import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';

export default class TransactionTableComponent extends Component {
  @service('auth') auth;
  @tracked transactions = this.auth.transactions;
  @tracked entriesnum = this.transactions.length;
  transactionTypes = ['All', 'Deposit', 'Withdrawal', 'Transfer'];
  @tracked fromDate = null;
  @tracked toDate = null;
  @tracked transactionType = 'all';

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

  compareBtwTwoDates = (date1, date2, date) => {
    new Date(date1) <= new Date(date) && new Date(date2) >= new Date(date);
  };

  @action handleFromDateFilter(event) {
    this.fromDate = event.target.value;
    console.log(this.fromDate, this.toDate);
    if (this.toDate == null) {
      this.transactions = this.auth.transactions.filter((transaction) => {
        return new Date(transaction.date) >= new Date(this.fromDate);
      });
    } else if (this.compareDates) {
      this.transactions = this.auth.transactions.filter((transaction) => {
        return (
          new Date(transaction.date) >= new Date(this.fromDate) &&
          new Date(transaction.date) <= new Date(this.toDate)
        );
      });
    }
    this.entriesnum = this.transactions.length;
  }
  @action handleToDateFilter(event) {
    this.toDate = event.target.value;
    console.log(this.fromDate, this.toDate);
    if (this.fromDate == null) {
      this.transactions = this.auth.transactions.filter((transaction) => {
        return new Date(transaction.date) <= new Date(this.toDate);
      });
    } else if (this.compareDates) {
      this.transactions = this.auth.transactions.filter((transaction) => {
        return (
          new Date(transaction.date) >= new Date(this.fromDate) &&
          new Date(transaction.date) <= new Date(this.toDate)
        );
      });
    }
    this.entriesnum = this.transactions.length;
  }

  @action handleTypeFilter(event) {
    this.transactionType =
      event.target.nextElementSibling.textContent.toLowerCase();
    console.log(this.transactionType);
    this.entriesnum = this.transactions.length;
  }
  // @action handleTypeFilter(event) {
  //   const transactionType =
  //     event.target.nextElementSibling.textContent.toLowerCase();
  //   if (transactionType === 'all') {
  //     this.transactions = this.auth.transactions;
  //   } else {
  //     this.transactions = this.auth.transactions.filter(
  //       (transaction) => transaction.transactionType === transactionType
  //     );
  //   }
  //   console.log(this.fromDate);
  //   console.log(this.toDate);
  //   this.entriesnum =
  //     this.transactions.length > 10 ? 10 : this.transactions.length;
  // }
}
