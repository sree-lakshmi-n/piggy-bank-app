import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';

export default class ProfileCardComponent extends Component {
  @service('auth') auth;
  @tracked custId = this.auth.currentCustId;
  @tracked accountNum = this.auth.accountNum;
  @tracked upiid = this.auth.upiId;
  @tracked name = this.auth.get('name');
  @tracked email = this.auth.get('email');
  @tracked phonenum = this.auth.get('phonenum');

  @tracked balance = 0.0;
  @tracked isBalanceShown = false;
  @action toggleBalanceShown() {
    this.isBalanceShown = !this.isBalanceShown;
    this.getBalance();
  }

  @action async getBalance() {
    const response = await fetch('http://localhost:8000/balance', {
      method: 'POST',
      headers: {
        accountnum: this.accountNum,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({}),
    });
    if (response.ok) {
      const json = await response.json();
      this.balance = json.message;
    } else {
      const json = await response.json();
      alert(json.message);
    }
  }
}
