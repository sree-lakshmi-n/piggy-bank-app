import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';

export default class CustomerPortalDepositController extends Controller {
  @service('auth') auth;
  @service router;
  @service('regex-check') regex;

  @tracked isAmountValid = true;

  @action
  validateFormElements(fieldName, event) {
    if (fieldName === 'amount') {
      this.isAmountValid = this.regex
        .get('amountRegex')
        .test(event.target.value);
      console.log(this.isAmountValid);
    }
  }

  @action async withdrawAmount(event) {
    const custId = this.auth.get('currentCustId');
    const accountNum = this.auth.get('accountNum');
    console.log(event.target);
    const element = event.target.closest('.form');
    let amount = element.querySelector('.input-amount').value;
    console.log(`${custId} ${amount} ${accountNum}`);
    if (this.isAmountValid && amount != null) {
      const response = await fetch('http://localhost:8000/withdraw', {
        method: 'POST',
        headers: {
          accountNum: accountNum,
          amount: amount,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({}),
      });

      if (response.ok) {
        const json = await response.json();
        alert(json.message);
        element.querySelector('.input-amount').value = '';
      } else {
        const json = await response.json();
        alert(json.message);
      }
    } else {
      console.log('Enter valid data');
    }
  }
}
