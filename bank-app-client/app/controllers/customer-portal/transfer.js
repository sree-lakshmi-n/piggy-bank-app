import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';

export default class CustomerPortalDepositController extends Controller {
  @service('auth') auth;
  @service router;
  @service('regex-check') regex;

  @tracked isUpiIdValid = true;
  @tracked isAmountValid = true;

  @action
  validateFormElements(fieldName, event) {
    if (fieldName === 'upiId') {
      this.isUpiIdValid = this.regex.get('upiRegex').test(event.target.value);
      console.log(this.isUpiIdValid);
    } else if (fieldName === 'amount') {
      this.isAmountValid = this.regex
        .get('amountRegex')
        .test(event.target.value);
      console.log(this.isAmountValid);
    }
  }

  @action async transferAmount(event) {
    const custId = this.auth.get('currentCustId');
    const accountNum = this.auth.get('accountNum');
    const upiId = this.auth.get('upiId');
    console.log(event.target);
    const element = event.target.closest('.form');
    let amount = element.querySelector('.input-amount').value;
    let recipientUpiId = element.querySelector('.input-recipient-upiid').value;
    console.log(`${custId} ${amount} ${upiId} ${recipientUpiId}`);
    if (
      this.isUpiIdValid &&
      this.isAmountValid &&
      upiId != '' &&
      parseInt(amount) > 0 &&
      parseInt(amount) <= this.auth.maxTransactionAmount
    ) {
      const response = await fetch('http://localhost:8000/transfer', {
        method: 'POST',
        headers: {
          upiid: upiId,
          amount: amount,
          recipientupiid: recipientUpiId,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({}),
      });

      if (response.ok) {
        const json = await response.json();
        alert(json.message);
        element.querySelector('.input-amount').value = '';
        element.querySelector('.input-recipient-upiid').value = '';
        await this.auth.getTransactionTable();
      } else {
        const json = await response.json();
        alert(json.message);
      }
    } else if (parseInt(amount) > this.auth.maxTransactionAmount) {
      alert(
        `Amount entered is large. Amount should be atmost ${this.auth.maxTransactionAmount}`
      );
    } else {
      alert('Enter valid details');
    }
  }
}
